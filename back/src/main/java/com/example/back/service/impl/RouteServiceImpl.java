package com.example.back.service.impl;

import com.example.back.api.BusApi;
import com.example.back.api.RouteApi;
import com.example.back.dto.ResultDTO;
import com.example.back.dto.RouteIdSetDTO;
import com.example.back.dto.bus.arrive.BusArriveProcessDTO;
import com.example.back.dto.bus.detail.BusDetailProcessDTO;
import com.example.back.dto.route.*;
import com.example.back.service.RouteService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Service
public class RouteServiceImpl implements RouteService {
    private final String ODsayBaseURL = "https://api.odsay.com/v1/api/";
    private final String SeoulBaseURL = "http://ws.bus.go.kr/api/rest/";


    @Value("${ODsay.apikey}")
    private String Odsay_apiKey;

    @Value(("${Public_Bus_APIKEY}"))
    private String Seoul_apiKey;

    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final Retrofit OdsayRetrofit = new Retrofit.Builder()
            .baseUrl(ODsayBaseURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    private final Retrofit SeoulRetrofit = new Retrofit.Builder()
            .baseUrl(SeoulBaseURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    private final RouteApi routeApi = OdsayRetrofit.create(RouteApi.class);
    private final BusApi busApi = SeoulRetrofit.create(BusApi.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private double calculateRouteScore(RouteProcessDTO.Path path) {
        RouteProcessDTO.Info info = path.getInfo();

        int totalTransfers = info.getBusTransitCount() + info.getSubwayTransitCount();
        double transferScore = totalTransfers > 0 ? Math.pow(1.5, totalTransfers) : 1.0;

        double timeScore = (info.getTotalTime() / 60.0) * 1.0;
        double busWeight = totalTransfers > 0 ? (double) info.getBusTransitCount() / totalTransfers : 0.0;

        double walkFactor = (info.getTotalWalk() / 1000.0) * 1.0;

        return (0.4 * transferScore) +
                (0.3 * timeScore) +
                (0.2 * busWeight) +
                (0.1 * walkFactor);
    }

    @Override
    public List<ResultDTO> fetchAndProcessRoutes(RouteDTO routeDTO) {
        try {
            Call<ResponseBody> call = routeApi.searchPubTransPathT(
                    routeDTO.getStartLat(),
                    routeDTO.getStartLng(),
                    routeDTO.getEndLat(),
                    routeDTO.getEndLng(),
                    Odsay_apiKey
            );

            ResponseBody responseBody = call.execute().body();
            if (responseBody != null) {
                String rawJson = responseBody.string();
                RouteProcessDTO.SearchPath parsedResponse = gson.fromJson(rawJson, RouteProcessDTO.SearchPath.class);
                if (parsedResponse != null && parsedResponse.getResult() != null) {
                    List<RouteProcessDTO.Path> paths = parsedResponse.getResult().getPath();
                    if (paths != null) {
                        List<RouteProcessDTO.Path> sortedPaths = paths.stream()
                                .map(path -> new AbstractMap.SimpleEntry<>(path, calculateRouteScore(path)))
                                .sorted(Map.Entry.comparingByValue())
                                .limit(3)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                        CompletableFuture<List<ResultDTO>> futureResult = asyncRouteDetails(sortedPaths);
                        return futureResult.join();
                    }
                }
            }
            return Collections.emptyList();
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    private CompletableFuture<List<ResultDTO>> asyncRouteDetails(List<RouteProcessDTO.Path> paths) {
        // path별 비동기 처리
        // result 함수를 CompletableFuture로 감싸고, executorService를 사용하여 병렬로 처리
        List<CompletableFuture<ResultDTO>> futureList = paths.stream()
                .map(path -> CompletableFuture.supplyAsync(() -> result(path), executorService))
                .collect(Collectors.toList());
        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(v -> futureList.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    private String mapTransitType(int pathType) {
        switch (pathType) {
            case 1:
                return "지하철";
            case 2:
                return "버스";
            case 3:
                return "버스+지하철";
            default:
                return "알 수 없음";
        }
    }
    private List<Integer> mapPath(List<RouteProcessDTO.SubPath> subPaths) {
        return subPaths.stream()
                .map(subPath -> {
                    switch (subPath.getTrafficType()) {
                        case 1:
                            return 1;
                        case 2:
                            return 2;
                        case 3:
                            return subPath.getSectionTime() > 0 ? 3 : null;
                        default:
                            return null;
                    }
                })
                .filter(detail -> detail != null)
                .collect(Collectors.toList());
    }

    private List<String> mapDetailedTrans(List<RouteProcessDTO.SubPath> subPaths) {
        return subPaths.stream()
                .map(subPath -> {
                    switch (subPath.getTrafficType()) {
                        case 1:
                            return subPath.getLane().get(0).getName() + "(" + subPath.getStartName() + " → " + subPath.getEndName() + ")";
                        case 2:
                            return subPath.getLane().get(0).getBusNo() + "번" + "(" + subPath.getStartName() + " → " + subPath.getEndName() + ")";
                        case 3:
                            return subPath.getSectionTime() > 0 ? "도보" : "";
                        default:
                            return "알 수 없는 교통수단";
                    }
                })
                .filter(detail -> !detail.isEmpty())
                .collect(Collectors.toList());
    }

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
                String predictTime1 ;
                String predictTime2 ;

                    // fetchBusLaneDetail 함수 호출
                BusDetailProcessDTO.BusLaneDetail busDetail = fetchBusLaneDetail(busID);

                // compareStationIDs 함수 호출
                List<Integer> stationInfos = compareStationIDs(busDetail, startID, endID);

                int startStationInfo = stationInfos.get(0);
                int endStationInfo = stationInfos.get(1);

                List<BusArriveProcessDTO.arriveDetail> arriveDetails = fetchAndBusArrive(startLocalStationID, busLocalBlID, startStationInfo);

                if (arriveDetails != null && arriveDetails.get(0).getMsgBody() != null && arriveDetails.get(0).getMsgBody().getItemList() != null) {
                    BusArriveProcessDTO.Item firstItem = arriveDetails.get(0).getMsgBody().getItemList().get(0);
                    predictTime1 = firstItem.getArrmsg1();
                    predictTime2 = firstItem.getArrmsg2();
                } else {
                    predictTime1 = "서비스 지역 아님";
                    predictTime2 = "서비스 지역 아님";
                }
                List<RouteProcessDTO.Station> stations = subPath.getPassStopList().getStations();
                // 결과를 Map으로 저장
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("busLocalBlID", busLocalBlID);
                List<Integer> stationRoute = new ArrayList<>();

                for (RouteProcessDTO.Station station : stations) {
                    stationRoute.add(station.getLocalStationID());
                }
                dataMap.put("startX",startX);
                dataMap.put("startY",startY);
                dataMap.put("stationInfo", stationRoute);
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



    private CompletableFuture<Map.Entry<Integer, Map<String, Object>>> processTrafficType3Async(RouteProcessDTO.SubPath subPath, int index) {
        return CompletableFuture.supplyAsync(() -> {
            // trafficType 3에 대한 비동기 작업 로직 구현
            // System.out.println("Processing TrafficType 3 SubPath: " + subPath + " with index: " + index);
            return new AbstractMap.SimpleEntry<>(index, null);
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
        resultDTO.setMainTransitType(mapTransitType(pathType));
        resultDTO.setPathTransitType(mapPath(subPathList));

        resultDTO.setTransitTypeNo(mapDetailedTrans(subPathList));
        // 비동기 처리
        List<CompletableFuture<Map.Entry<Integer, Map<String, Object>>>> futureList = new ArrayList<>();

        for (int i = 0; i < subPathList.size(); i++) {
            RouteProcessDTO.SubPath subPath = subPathList.get(i);
            int index = i; // 현재 인덱스 저장

            if (subPath.getTrafficType() == 2) {
                CompletableFuture<Map.Entry<Integer, Map<String, Object>>> future = processTrafficType2Async(subPath, index);
                futureList.add(future);
            } else if (subPath.getTrafficType() == 3) {
                CompletableFuture<Map.Entry<Integer, Map<String, Object>>> future = processTrafficType3Async(subPath, index);
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
                    routeIdSetDTO.setBusLocalBlID((Integer) dataMap.get("busLocalBlID"));
                    routeIdSetDTO.setStartX((Double) dataMap.get("startX"));
                    routeIdSetDTO.setStartY((Double) dataMap.get("startY"));
                    routeIdSetDTO.setStartStationInfo((Integer) dataMap.get("startStationInfo"));
                    routeIdSetDTO.setEndStationInfo((Integer) dataMap.get("endStationInfo"));
                    routeIdSetDTO.getStationInfo().addAll((List<Integer>) dataMap.get("stationInfo"));
                    routeIdSetDTO.getPredictTimes1().add((String) dataMap.get("predictTime1"));
                    routeIdSetDTO.getPredictTimes2().add((String) dataMap.get("predictTime2"));
                    resultDTO.getRouteIds().add(routeIdSetDTO);
                    // 교통 유형이 2인 경우
                }else if(processedSubPath.getTrafficType() == 3){
                    // 교통 유형 3일 경우 처리
                }

                System.out.println("처리 완료된 subPath 인덱스: " + subPathIndex);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        return resultDTO;
    }



    private BusDetailProcessDTO.BusLaneDetail fetchBusLaneDetail(int busID) throws IOException {
        Call<ResponseBody> call = routeApi.busLaneDetail(busID, Odsay_apiKey);
        ResponseBody responseBody = call.execute().body();

        if (responseBody != null) {
            String rawJson = responseBody.string();
            BusDetailProcessDTO.BusLaneDetail parsedResponse = gson.fromJson(rawJson, BusDetailProcessDTO.BusLaneDetail.class);
            return parsedResponse != null ? parsedResponse : new BusDetailProcessDTO.BusLaneDetail();
        } else {
            return new BusDetailProcessDTO.BusLaneDetail();
        }
    }


    private List<Integer> compareStationIDs(BusDetailProcessDTO.BusLaneDetail busDetailProcess, int startID, int endID) {
        List<Integer> startIndices = new ArrayList<>();
        List<Integer> endIndices = new ArrayList<>();

        List<BusDetailProcessDTO.Station> stations = busDetailProcess.getResult().getStation();

        if (stations != null) {
            for (BusDetailProcessDTO.Station station : stations) {
                if (station.getStationID() == startID) {
                    startIndices.add(station.getIdx());
                } else if (station.getStationID() == endID) {
                    endIndices.add(station.getIdx());
                }
            }
        }

        List<Integer> stationInfos = new ArrayList<>();

        for (Integer startIndex : startIndices) {
            for (Integer endIndex : endIndices) {
                if (startIndex < endIndex) {
                    stationInfos.add(startIndex + 1);
                    stationInfos.add(endIndex + 1);
                    return stationInfos;
                }
            }
        }

        // 일치하는 정류장 정보 없음
        stationInfos.add(-1);
        return stationInfos;
    }

    public List<BusArriveProcessDTO.arriveDetail> fetchAndBusArrive(int stId, int busRouteId, int ord) {
        try {
            Call<ResponseBody> call = busApi.getArrInfoByRoute(
                    Seoul_apiKey,
                    stId,
                    busRouteId,
                    ord,
                    "json"
            );
            ResponseBody responseBody = call.execute().body();
            if (responseBody != null) {
                String rawJson = responseBody.string();
                BusArriveProcessDTO.arriveDetail parsedResponse = gson.fromJson(rawJson, BusArriveProcessDTO.arriveDetail.class);

                // 리스트 형태로 반환
                return parsedResponse != null ? List.of(parsedResponse) : List.of();
            }else {
                return List.of();
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return List.of(); // 예외 상황에서도 빈 리스트 반환
    }
}