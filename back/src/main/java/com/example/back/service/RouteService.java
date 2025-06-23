package com.example.back.service;

import com.example.back.domain.TimeTable;
import com.example.back.dto.ResultDTO;
import com.example.back.dto.RouteIdSetDTO;
import com.example.back.dto.bus.arrive.BusArriveProcessDTO;
import com.example.back.dto.bus.detail.BusDetailProcessDTO;
import com.example.back.dto.route.*;
import com.example.back.dto.subway.arrive.SubwayArriveProcessDTO;
import com.google.common.cache.Cache;
import com.google.gson.JsonSyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import com.google.common.cache.CacheBuilder;

// 경로 처리
@Service
public class RouteService{

    @Autowired
    private RouteCalService routeCalService;

    @Autowired
    private SubwayProcessService subwayProcessService;

    @Autowired
    private SubwayService subwayService;

    @Autowired
    private BusService busService;

    @Autowired
    private APIService apiService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    // 비동기 경로 상세 정보 조회
    private final Cache<RouteProcessDTO.Path, ResultDTO> resultCache = CacheBuilder.newBuilder()
            .maximumSize(100) // Maximum number of entries in the cache
            .expireAfterWrite(10, TimeUnit.MINUTES) // Cache expiration time
            .build();

    private ResultDTO getCachedResult(RouteProcessDTO.Path path) {
        return resultCache.getIfPresent(path);
    }

    private void cacheResult(RouteProcessDTO.Path path, ResultDTO result) {
        resultCache.put(path, result);
    }

    // path별 비동기 처리
    private CompletableFuture<List<ResultDTO>> asyncRouteDetails(List<RouteProcessDTO.Path> paths) {
        // result 함수를 CompletableFuture로 감싸고, executorService를 사용하여 병렬로 처리
        List<CompletableFuture<ResultDTO>> futureList = paths.stream()
                .map(path -> CompletableFuture.supplyAsync(() -> result(path), executorService))
                .collect(Collectors.toList());
        System.out.println("현재 활성화된 스레드 수: " + Thread.activeCount());
        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(v -> futureList.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    // 경로 상세 정보 조회
    public List<ResultDTO> fetchAndProcessRoutes(RouteDTO routeDTO) {
        try {
            RouteProcessDTO.SearchPath parsedResponse = apiService.fetchAndProcessRoutes(routeDTO);

            if (parsedResponse != null && parsedResponse.getResult() != null) {
                List<RouteProcessDTO.Path> paths = parsedResponse.getResult().getPath();

                if (paths != null) {
                    List<RouteProcessDTO.Path> sortedPaths = paths.stream()
                            .map(path -> new AbstractMap.SimpleEntry<>(path, routeCalService.calculateRouteScore(path)))
                            .sorted(Map.Entry.comparingByValue())
                            .limit(3)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    // 캐싱된 결과 확인
                    List<ResultDTO> cachedResults = sortedPaths.stream()
                            .map(this::getCachedResult)
                            .filter(Objects::nonNull) // 캐시에 존재하는 값만 필터링
                            .collect(Collectors.toList());

                    // 캐시에 있는 데이터가 3개 이상이면 API 호출 없이 반환
                    if (cachedResults.size() >= 3) {
                        System.out.println("캐시에서 결과 반환!");
                        return cachedResults;
                    }

                    // 캐시에 없는 데이터를 조회하여 추가
                    CompletableFuture<List<ResultDTO>> futureResult = asyncRouteDetails(sortedPaths);
                    List<ResultDTO> newResults = futureResult.join();

                    // 캐시에 저장
                    for (int i = 0; i < sortedPaths.size(); i++) {
                        cacheResult(sortedPaths.get(i), newResults.get(i));
                    }
                    return newResults;
                }
            }
            return Collections.emptyList();
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // 지하철 도착 정보 처리
    public CompletableFuture<Map.Entry<Integer, Map<String, Object>>> processTrafficType1Async(RouteProcessDTO.SubPath subPath, int index) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> timeAndDayType;
            String predictTime1String = "도착 정보 없음";
            String predictTime2String = "도착 정보 없음";
            // 첫번째 역 이름
            String startName = subPath.getStartName();
            // 마지막 역 이름
            String endName = subPath.getEndName();
            // 노선 정보
            int subwayCode = subPath.getLane().get(0).getSubwayCode();

            String subwayLineName = subPath.getLane().get(0).getName();
            // 노선 방향
            String direction = subPath.getWayCode() == 1 ? "상행" : "하행";

            // 시작 주소
            double startX = subPath.getStartX();
            double startY = subPath.getStartY();
            // 상세 정보
            List<RouteProcessDTO.Station> stations = subPath.getPassStopList().getStations();

            // 역 이름 리스트
            List<String> transferStations = new ArrayList<>();
            for (RouteProcessDTO.Station station : stations) {
                transferStations.add(station.getStationName());
            }

            String secondStation = transferStations.size() > 1 ? transferStations.get(1) : null;
            // 결과 데이터를 저장할 Map 생성
            Map<String, Object> dataMap = new HashMap<>();

            String[] arrivals;
            // 모든 지하철 노선 별 구간 소요 시간 작성 필요
            // 실시간 도착정보 제공x 지하철 시
            int convertedCode = subwayService.convertSubwayCode(subwayCode);
            int [] betweenTime = null;

            // 데이터베이스 처리 여부
            if (subwayService.DBCode(subwayCode)) {
                List<TimeTable> predictTimeTable = subwayProcessService.fetchPredictedTimes(subwayCode, startName, direction);
                arrivals = subwayProcessService.processPredictedArrivals(predictTimeTable);
            } else {
                // 실시간 도착정보 처리
                List< SubwayArriveProcessDTO.RealtimeArrival> matchedArrivals = subwayProcessService.processSeoulSubway(convertedCode, startName, endName, direction, secondStation);
                arrivals =  subwayProcessService.handleArrivalQueue(matchedArrivals, startName, convertedCode, direction);
            }

            // `arrivals`에서 예측 도착 시간 분리
            if (arrivals != null && arrivals.length > 0) {
                predictTime1String = arrivals[0]; // predictTime1에 첫 번째 할당
            }
            if (arrivals != null && arrivals.length > 1) {
                predictTime2String = arrivals[1]; // predictTime2에 두 번째 할당
            }

            // 결과를 Map으로 저장
            dataMap.put("transportLocalID", subwayCode);
            dataMap.put("subwayLineName", subwayLineName );
            dataMap.put("startX",startX);
            dataMap.put("startY",startY);
            dataMap.put("trainDirection", direction);
            dataMap.put("transferStations",transferStations);
            dataMap.put("predictTime1", predictTime1String);
            dataMap.put("predictTime2", predictTime2String);
            return new AbstractMap.SimpleEntry<>(index, dataMap);
        }, executorService);
    }

    // 버스 도착 정보 처리
    private CompletableFuture<Map.Entry<Integer, Map<String, Object>>> processTrafficType2Async(RouteProcessDTO.SubPath subPath, int index) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int busID = subPath.getLane().get(0).getBusID();

                int startID = subPath.getStartID();

                int endID = subPath.getEndID();

                Double startX = subPath.getStartX();
                Double startY = subPath.getStartY();
                int busLocalBlID = subPath.getLane().get(0).getBusLocalBlID();
                int startLocalStationID = subPath.getStartLocalStationID();
                Map<String, Object> dataMap = new HashMap<>();
                String predictTime1 = null;
                String predictTime2 = null;

                    // fetchBusLaneDetail 함수 호출 -> jsonParser 함수 호출
                BusDetailProcessDTO.BusLaneDetail busDetail = apiService.fetchBusLaneDetail(busID);

                // compareStationIDs 함수 호출
                List<Integer> stationInfos = busService.compareStationIDs(busDetail, startID, endID);

                // 결과 처리
                int startStationInfo = stationInfos.get(0);
                int endStationInfo = stationInfos.get(1);

                List<RouteProcessDTO.Station> stations = subPath.getPassStopList().getStations();
                // 결과를 Map으로 저장

                dataMap.put("TransportLocalID", busLocalBlID);
                List<Integer> stationRoute = new ArrayList<>();
                List<String> transferStations = new ArrayList<>();

                for (RouteProcessDTO.Station station : stations) {
                    stationRoute.add(station.getLocalStationID());
                    transferStations.add(station.getStationName());
                }

                //if(index <= 2) {
                    // 도착 정보 조회
                    List<BusArriveProcessDTO.arriveDetail> arriveDetails = List.of(apiService.fetchAndBusArrive(startLocalStationID, busLocalBlID, startStationInfo));

                    if (arriveDetails != null && arriveDetails.get(0).getMsgBody() != null && arriveDetails.get(0).getMsgBody().getItemList() != null) {
                        BusArriveProcessDTO.Item firstItem = arriveDetails.get(0).getMsgBody().getItemList().get(0);
                        predictTime1 = firstItem.getArrmsg1();
                        predictTime2 = firstItem.getArrmsg2();
                    } else {
                        predictTime1 = "데이터 없음";
                        predictTime2 = "데이터 없음";
                    }
                //}

                dataMap.put("startX",startX);
                dataMap.put("startY",startY);
                dataMap.put("stationInfo", stationRoute);
                dataMap.put("transferStations", transferStations);
                dataMap.put("startStationInfo", startStationInfo);
                dataMap.put("endStationInfo", endStationInfo);
                dataMap.put("predictTime1", predictTime1);
                dataMap.put("predictTime2", predictTime2);
                return new AbstractMap.SimpleEntry<>(index, dataMap);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }, executorService);
    }

    // path별 결과 처리
    // path반환 값
    // totalTime : path.info
    // transitCount : path.info
    // mainTransitType : path.pathType (1: 지하철, 2: 버스, 3: 버스+지하철) 순서 나열
    // pathTransitType : path.subPath.trafficType  (1: 지하철, 2: 버스, 3: 도보) 순서 나열
    // TransitTypeNo : path.subPathList (도보, 버스, 지하철 번호) 을 문자열 리스트로 저장
    // predictTimes1, predictTimes2, routeIds : trafficType 2, 3에 대한 detailTrans 구조체 처리
    private ResultDTO result(RouteProcessDTO.Path path) {
        // path별 pathType, info, subPathList 처리
        int pathType = path.getPathType();
        RouteProcessDTO.Info info = path.getInfo();
        List<RouteProcessDTO.SubPath> subPathList = path.getSubPath();

        // ResultDTO 객체 생성
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setTotalTime(info.getTotalTime());
        resultDTO.setTransitCount(
                info.getBusTransitCount() + info.getSubwayTransitCount()
        );
        resultDTO.setMainTransitType(routeCalService.mapTransitType(pathType));
        resultDTO.setTransitTypeNo(routeCalService.mapDetailedTrans(subPathList));
        resultDTO.setPathTransitType(routeCalService.mapPath(subPathList));
        // 비동기 처리
        List<CompletableFuture<Map.Entry<Integer, Map<String, Object>>>> futureList = new ArrayList<>();

        for (int i = 0; i < subPathList.size(); i++) {
            RouteProcessDTO.SubPath subPath = subPathList.get(i);
            int index = i; // 현재 인덱스 저장

            if (subPath.getTrafficType() == 1) {
                CompletableFuture<Map.Entry<Integer, Map<String, Object>>> future = processTrafficType1Async(subPath, index);
                futureList.add(future);
            }
            else if (subPath.getTrafficType() == 2) {
                CompletableFuture<Map.Entry<Integer, Map<String, Object>>> future = processTrafficType2Async(subPath, index);
                futureList.add(future);
            }
        }
        // 모든 비동기 작업이 완료될 때까지 기다림
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();

        // 비동기 작업 결과 처리 (인덱스를 사용하여 원래 순서 유지)
        futureList.forEach(future -> {
            try {
                Map.Entry<Integer, Map<String, Object>> result = future.get();
                // index 추출
                int subPathIndex = result.getKey();
                RouteProcessDTO.SubPath processedSubPath = subPathList.get(subPathIndex);
                Map<String, Object> dataMap = result.getValue();

                // subPath 데이터를 ResultDTO에 설정
                if (processedSubPath.getTrafficType() == 2) {
                    RouteIdSetDTO routeIdSetDTO = new RouteIdSetDTO();
                    routeIdSetDTO.setTransportLocalID((Integer) dataMap.get("TransportLocalID"));
                    routeIdSetDTO.setStartX((Double) dataMap.get("startX"));
                    routeIdSetDTO.setStartY((Double) dataMap.get("startY"));
                    routeIdSetDTO.setStartStationInfo((Integer) dataMap.get("startStationInfo"));
                    routeIdSetDTO.setEndStationInfo((Integer) dataMap.get("endStationInfo"));
                    routeIdSetDTO.getTransferStations().addAll((List<String>) dataMap.get("transferStations"));
                    routeIdSetDTO.setStationInfo((List<Integer>) dataMap.get("stationInfo"));
                    routeIdSetDTO.setPredictTimes1((String) dataMap.get("predictTime1"));
                    routeIdSetDTO.setPredictTimes2((String) dataMap.get("predictTime2"));
                    resultDTO.getRouteIds().add(routeIdSetDTO);
                    // 교통 유형이 2인 경우

                }else if(processedSubPath.getTrafficType() == 1){
                    RouteIdSetDTO routeIdSetDTO = new RouteIdSetDTO();
                    routeIdSetDTO.setTransportLocalID((Integer) dataMap.get("transportLocalID"));
                    routeIdSetDTO.setSubwayLineName((String) dataMap.get("subwayLineName"));
                    routeIdSetDTO.setStartX((Double) dataMap.get("startX"));
                    routeIdSetDTO.setStartY((Double) dataMap.get("startY"));
                    routeIdSetDTO.setTrainDirection((String) dataMap.get("trainDirection"));
                    routeIdSetDTO.getTransferStations().addAll((List<String>) dataMap.get("transferStations"));
                    routeIdSetDTO.setPredictTimes1((String) dataMap.get("predictTime1"));
                    routeIdSetDTO.setPredictTimes2((String) dataMap.get("predictTime2"));
                    resultDTO.getRouteIds().add(routeIdSetDTO);
                    // 교통 유형 3일 경우 처리
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        return resultDTO;
    }
}