package com.example.back.service;

import com.example.back.dto.ResultDTO;
import com.example.back.dto.RouteIdSetDTO;
import com.example.back.dto.bus.arrive.BusArriveProcessDTO;
import com.example.back.dto.bus.detail.BusDetailProcessDTO;
import com.example.back.dto.route.*;
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

                    String predictTime1String;
                    String predictTime2String = null;
                    // 첫번째 역 이름
                    String startName = subPath.getStartName();
                    // 마지막 역 이름
                    String endName = subPath.getEndName();
                    // 노선 정보
                    int subwayCode = subPath.getLane().get(0).getSubwayCode();
                    // 노선 이름
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
                    // 도착정보 존재 x 처리
                    if(subwayService.convertSubwayCodeForTime(convertedCode)){
                        betweenTime = subwayProcessService.getBetweenTimes(convertedCode, startName, endName, direction);
                    }
                    // if (index < 2) {
                    // 데이터베이스 처리 여부
                    if (subwayService.DBCode(subwayCode)) {
                        arrivals = subwayProcessService.fetchPredictedTimes(subwayCode, startName);
                    }else {
                        // 실시간 도착정보 처리
                            arrivals = subwayProcessService.processSeoulSubway(convertedCode, startName, endName, direction, secondStation);
                    }
                    //}
//
//                        switch (city) {
//                            case 10 -> arrivals = subwayProcessService.processSeoulSubway(subwayCode, startName, endName, direction, secondStation);
//                            case 20, 30, 40, 50 -> arrivals = subwayProcessService.fetchPredictedTimes(subwayCode, startName);
//                            default -> {
//                                System.out.println("Unknown subway code: " + subwayCode);
//                                arrivals = new String[]{null, null}; // 오류 방지를 위해 빈 배열 반환
//                            }
//                        }


            // `arrivals`에서 예측 도착 시간 분리
                        predictTime1String = (arrivals.length > 0) ? arrivals[0] : null;
                        predictTime1String = (arrivals.length > 1) ? arrivals[1] : null;
                    //}
//            // 지하철 도시 코드 확인
//                if(index < 2){ // 인덱스 0, 1에 대해서만 처리
//                    System.out.println("index" + index);
//                    // 도시코드 추출
//                    Map<Integer, Integer> subwayCodeToCity = subwayService.getSubwayCodeToCityMapping();
//                    int city = subwayCodeToCity.getOrDefault(subwayCode, 0);
//                    if (city == 10) {
//                        // 시간표 기준 지하철
//                        if (subwayService.isSpecialSeoulCode(subwayCode)) {
//                            // 시간 확인
//                            timeAndDayType = subwayService.getTimeAndDayType(subwayCode);
//                            Time seoulTime = (Time) timeAndDayType.get("seoulTime");
//                            String currentDayType = (String) timeAndDayType.get("currentDayType");
//                            // 시간표 기준 도착정보 확인
//                            predictTimeList = databaseService.findNextSubwayArrivals(subwayCode, startName, seoulTime, currentDayType);
//                        } else {
//                            // 오디세이 -> 서울 지하철 노선 코드 변환
//                            int convertedSubwayCode = subwayService.convertSubwayCode(subwayCode);
//                            // 역 이름 예외 처리
//                            if (startName.equals("서울역")) {
//                                startName = "서울";
//                            }
//                            else if (startName.equals("평택지제")){
//                                startName = "지제";
//                            }
//                            // 역 ID 조회
//                            int statn_id = databaseService.findStationId(convertedSubwayCode, startName);
//
//                            // 실시간 도착 정보 조회
//                            List<SubwayArriveProcessDTO.RealtimeArrival> realtimeArrivals = null;
//                            try {
//                                //서울 지하철 도착 정보 조회
//                                realtimeArrivals = apiService.fetchAndSubwayArrive(startName).getRealtimeArrivalList();
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//                            // 도착 정보가 null이 아니고 비어있지 않은 경우
//                            if (realtimeArrivals != null && !realtimeArrivals.isEmpty()) {
//                                // 도착 정보 필터링
//                                List<SubwayArriveProcessDTO.RealtimeArrival> directionFilteredArrivals = new ArrayList<>();
//                                // 도착 정보 리스트
//                                List<SubwayArriveProcessDTO.RealtimeArrival> preliminaryArrivals = new ArrayList<>();
//
//                                // 원하는 방향의 도착 정보 필터링
//                                for (SubwayArriveProcessDTO.RealtimeArrival arrival : realtimeArrivals) {
//                                    // 역 ID 동일 여부 확인
//                                    if (Integer.parseInt(arrival.getStatnId()) == statn_id) {
//                                        // 사용자가 원하는 방향인지 확인
//                                        if (arrival.getUpdnLine().equals(direction)) {
//                                            // 같은 노선 여부 확인
//                                            String trainLineNm = arrival.getTrainLineNm();
//                                            // 노선 이름이 null이 아닌 경우
//                                            if (trainLineNm != null) {
//                                                // 노선 - 기준 방향으로 분리
//                                                String[] parts = trainLineNm.split(" - ");
//                                                // 노선 종착역 정보
//                                                String destination = parts[0].replace("행", "").trim();
//                                                // 노선 다음역 정보
//                                                String nextStation = parts[1].replace("방면", "").trim(); // 정확한 역 이름 추출
//                                                // 다음역이 null이 아닌 경우
//                                                if (parts.length > 1 &&  nextStation.equals(secondStation)) {
//                                                    // 지하철 코드에 따른 처리
//                                                    if (convertedSubwayCode == 1001) {
//                                                        // FindNetwork로 이전
//                                                        int result = subwayService.compareRoutes(convertedSubwayCode,startName,endName,startName,destination);
//                                                        if (result == 1){
//                                                            // 조건에 맞는 데이터를 리스트에 추가
//                                                            directionFilteredArrivals.add(arrival);
//                                                        }else if(result == 2){
//                                                            preliminaryArrivals.add(arrival);
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//
//                                // 도착 정보 처리 순서 지정
//                                if (!directionFilteredArrivals.isEmpty()) {
//                                    PriorityQueue<SubwayArriveProcessDTO.RealtimeArrival> arrival1Queue = new PriorityQueue<>(
//                                            (a, b) -> {
//                                                int result = compareByArvlCd(a, b);
//                                                if (result != 0) {
//                                                    lastCriterion = CompareCriterion.ARVL_CD;
//                                                    return result;
//                                                }
//
//                                                result = compareByBarvlDt(a, b);
//                                                if (result != 0) {
//                                                    lastCriterion = CompareCriterion.BARVL_DT;
//                                                    return result;
//                                                }
//
//                                                result = compareByNthStation(a, b);
//                                                if (result != 0) {
//                                                    lastCriterion = CompareCriterion.NTH_STATION;
//                                                    return result;
//                                                }
//
//                                                lastCriterion = CompareCriterion.NONE; // 동일한 경우
//                                                return 0;
//                                            }
//                                    );
//
//                                    // 같은 방향 종착역 포함 우선 순위 큐 처리
//                                    arrival1Queue.addAll(directionFilteredArrivals);
//                                    // 처리 순서 별 도착 정보 처리 및 출력
//                                    if (!arrival1Queue.isEmpty()) {
//                                        // 가장 가까운 도착 정보
//                                        SubwayArriveProcessDTO.RealtimeArrival firstArrival = arrival1Queue.poll();
//                                        // 도착정보 처리
//                                        predictTime1String = handleArrivalByCriterion(firstArrival,startName,convertedSubwayCode, direction);
//                                        // 두 번째 가장 가까운 도착 정보
//                                        if (!arrival1Queue.isEmpty()) {
//                                            SubwayArriveProcessDTO.RealtimeArrival secondArrival = arrival1Queue.poll();
//                                            predictTime2String = handleArrivalByCriterion(secondArrival,startName, convertedSubwayCode, direction);
//                                        } else {
//                                            System.out.println("두 번째 도착 정보가 없습니다.");
//                                        }
//                                    } else {
//                                        System.out.println("조건에 맞는 도착 정보가 없습니다.");
//                                    }
//                                } else {
//                                    // 같은 방향 환승 가능성 포함 우선순위 큐 처리
//                                    PriorityQueue<SubwayArriveProcessDTO.RealtimeArrival> arrival2Queue = new PriorityQueue<>(
//                                            // 도착 정보 처리 순서 지정
//                                            (a, b) -> {
//                                                int result = compareByArvlCd(a, b);
//                                                if (result != 0) return result;
//
//                                                result = compareByBarvlDt(a, b);
//                                                if (result != 0) return result;
//
//                                                result = compareByNthStation(a, b);
//                                                return result;
//                                            }
//                                    );
//
//                                    //
//                                    arrival2Queue.addAll(preliminaryArrivals);
//
//                                    if (!arrival2Queue.isEmpty()) {
//                                        SubwayArriveProcessDTO.RealtimeArrival firstArrival = arrival2Queue.poll();
//                                        predictTime1String = handleArrivalByCriterion(firstArrival,startName, convertedSubwayCode, direction);
//                                        if (!arrival2Queue.isEmpty()) {
//                                            SubwayArriveProcessDTO.RealtimeArrival secondArrival = arrival2Queue.poll();
//                                            predictTime2String = handleArrivalByCriterion(secondArrival, startName, convertedSubwayCode, direction);
//                                        } else {
//                                            System.out.println("두 번째 도착 정보가 없습니다.");
//                                        }
//                                    } else {
//                                        System.out.println("조건에 맞는 도착 정보가 없습니다.");
//                                    }
//                                }
//                            }
//                        }
//                    } else if (city == 20 || city == 30 || city == 40 || city == 50) { // 부산, 대구, 광주, 대전
//                        timeAndDayType = subwayService.getTimeAndDayType(subwayCode);
//                        Time seoulTime = (Time) timeAndDayType.get("seoulTime");
//                        String currentDayType = (String) timeAndDayType.get("currentDayType");
//                        predictTimeList = databaseService.findNextSubwayArrivals(subwayCode, startName, seoulTime, currentDayType);
//                    } else {
//                        System.out.println("Unknown subway code: " + subwayCode);
//                    }
//
//                    // 결과 처리 (도착 시간 추출)
//                    Time Time1;
//                    Time Time2;
//
//                    if (predictTimeList != null && !predictTimeList.isEmpty()) {
//                        Time1 = predictTimeList.size() > 0 ? predictTimeList.get(0).getArrival_Time() : null;
//                        Time2 = predictTimeList.size() > 1 ? predictTimeList.get(1).getArrival_Time() : null;
//
//                        Time seoulTime = subwayService.getSeoulCurrentTime();
//
//                        if (Time1 != null) {
//                            LocalTime arrivalTime1 = Time1.toLocalTime();
//                            long predictTime1 = java.time.Duration.between(seoulTime.toLocalTime(), arrivalTime1).toMinutes();
//
//                            // predictTime1을 String으로 변환
//                            predictTime1String = predictTime1 + "분 후";
//                        }
//                        if (Time2 != null) {
//                            LocalTime arrivalTime2 = Time2.toLocalTime();
//                            long predictTime2 = java.time.Duration.between(seoulTime.toLocalTime(), arrivalTime2).toMinutes();
//                            // predictTime2를 String으로 변환
//                            predictTime2String = predictTime2 + "분 후";
//                        }
//                    }
//                }
            // 결과를 Map으로 저장
            dataMap.put("transportLocalID", subwayCode);
            dataMap.put("subwayLineName", subwayLineName );
            dataMap.put("startX",startX);
            dataMap.put("startY",startY);
            dataMap.put("trainDirection", direction);
            dataMap.put("transferStations",transferStations);
            dataMap.put("betweenTime", betweenTime);
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

                //if(index < 2) {
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