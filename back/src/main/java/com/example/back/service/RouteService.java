package com.example.back.service;

import com.example.back.api.BusApi;
import com.example.back.api.RouteApi;
import com.example.back.api.SubwayApi;
import com.example.back.domain.TimeTable;
import com.example.back.dto.ResultDTO;
import com.example.back.dto.RouteIdSetDTO;
import com.example.back.dto.bus.arrive.BusArriveProcessDTO;
import com.example.back.dto.bus.detail.BusDetailProcessDTO;
import com.example.back.dto.route.*;
import com.example.back.dto.subway.arrive.SubwayArriveProcessDTO;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Time;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class RouteService{
    private final String ODsayBaseURL = "https://api.odsay.com/v1/api/";
    private final String Seoul_BusBaseURL = "http://ws.bus.go.kr/api/rest/";
    private final String Seoul_SubwayBaseURL = "http://swopenAPI.seoul.go.kr/api/subway/";

    @Autowired
    private RouteCalService routeCalService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private SubwayService subwayService;

    @Autowired
    private BusService busService;

    @Value("${ODsay.apikey}")
    private String Odsay_apiKey;

    @Value(("${Public_Bus_APIKEY}"))
    private String Seoul_Bus_apiKey;

    @Value(("${Public_Subway_APIKEY}"))
    private String Seoul_Subway_apiKey;

    enum CompareCriterion {
        ARVL_CD, BARVL_DT, NTH_STATION, NONE
    }

    CompareCriterion lastCriterion = CompareCriterion.NONE;


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

    private final Retrofit BusRetrofit = new Retrofit.Builder()
            .baseUrl(Seoul_BusBaseURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    private final Retrofit SubwayRetrofit = new Retrofit.Builder()
            .baseUrl(Seoul_SubwayBaseURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    private final RouteApi routeApi = OdsayRetrofit.create(RouteApi.class);
    private final BusApi busApi = BusRetrofit.create(BusApi.class);
    private final SubwayApi subwayApi = SubwayRetrofit.create(SubwayApi.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    // 비동기 경로 상세 정보 조회
    private CompletableFuture<List<ResultDTO>> asyncRouteDetails(List<RouteProcessDTO.Path> paths) {
        // path별 비동기 처리
        // result 함수를 CompletableFuture로 감싸고, executorService를 사용하여 병렬로 처리
        List<CompletableFuture<ResultDTO>> futureList = paths.stream()
                .map(path -> CompletableFuture.supplyAsync(() -> result(path), executorService))
                .collect(Collectors.toList());
        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(v -> futureList.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    // Odsay 버스 상세 정보 조회
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

    public List<BusArriveProcessDTO.arriveDetail> fetchAndBusArrive(int stId, int busRouteId, int ord) {
        try {
            Call<ResponseBody> call = busApi.getArrInfoByRoute(
                    Seoul_Bus_apiKey,
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

    // 서울 지하철 도착 정보 조회
    public List<SubwayArriveProcessDTO.RealtimeArrival> fetchAndSubwayArrive(String stationName) {
        try {
            // Retrofit 호출
            System.out.println("Station Name: " + stationName);
            Call<ResponseBody> call = subwayApi.getRealtimeStationArrival(Seoul_Subway_apiKey, stationName);
            Response<ResponseBody> response = call.execute(); // 동기 호출

            // API 응답 확인
            if (response.isSuccessful() && response.body() != null) {
                ResponseBody responseBody = response.body();
                String rawJson = responseBody.string();
                SubwayArriveProcessDTO subwayArriveProcessDTO = gson.fromJson(rawJson, SubwayArriveProcessDTO.class);

                // 도착 정보 리스트 반환
                return subwayArriveProcessDTO.getRealtimeArrivalList();
            } else {
                // API 호출 실패 시 로그 출력
                System.err.println("API 호출 실패 - 상태 코드: " + response.code());
                System.err.println("응답 메시지: " + response.message());
            }
        } catch (IOException e) {
            // 예외 처리
            System.err.println("API 호출 중 예외 발생: " + e.getMessage());
        }
        // 오류 발생 시 빈 리스트 반환
        return List.of();
    }

    // 경로 조회 및 처리
    public List<ResultDTO> fetchAndProcessRoutes(RouteDTO routeDTO) {
        try {
            long startTime = System.nanoTime(); // 시작 시간 측정
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
                                .map(path -> new AbstractMap.SimpleEntry<>(path, routeCalService.calculateRouteScore(path)))
                                .sorted(Map.Entry.comparingByValue())
                                .limit(3)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                        CompletableFuture<List<ResultDTO>> futureResult = asyncRouteDetails(sortedPaths);
                        long endTime = System.nanoTime();
                        System.out.println("⏳ GSON 처리 시간: " + (endTime - startTime) / 1_000_000.0 + " ms");
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

    // 처리 순서
    private int getArvlCdPriority(String arvlCd) {
        switch (arvlCd) {
            case "0": case "1": case "2": case "3": return 0;
            case "4": case "5": return 1;
            default: return 2;
        }
    }

    // arvlMsg2 "[N]번째 전역" 추출
    private int getNthStation(String arvlMsg2) {
        if (arvlMsg2.contains("번째 전역")) {
            String extracted = arvlMsg2.replaceAll(".*\\[(\\d+)].*", "$1");
            return Integer.parseInt(extracted);
        }
        return Integer.MAX_VALUE;
    }
    // arvlMsg2에서 역 이름 추출
    private String getStationNameFromMsg(String arvlMsg2) {
        if (arvlMsg2.contains("번째 전역") && arvlMsg2.contains("(") && arvlMsg2.contains(")")) {
            // () 내부에 있는 내용을 추출
            return arvlMsg2.replaceAll(".*\\(([^)]+)\\).*", "$1");
        }
        return ""; // 역 이름을 찾을 수 없으면 빈 문자열 반환
    }

    // ArvlCd에 따라 처리 순서 지정
    private int compareByArvlCd(SubwayArriveProcessDTO.RealtimeArrival a, SubwayArriveProcessDTO.RealtimeArrival b) {
        int aPriority = getArvlCdPriority(a.getArvlCd());
        int bPriority = getArvlCdPriority(b.getArvlCd());

        if (aPriority != bPriority) {
            // 도착 상태에 따라 우선순위 비교
            return Integer.compare(aPriority, bPriority);
        }
        return 0; // 동일한 우선순위인 경우 다음 조건으로 넘어감
    }

    // BarvlDt에 따라 처리 순서 지정
    private int compareByBarvlDt(SubwayArriveProcessDTO.RealtimeArrival a, SubwayArriveProcessDTO.RealtimeArrival b) {
        int aBarvlDt = Integer.parseInt(a.getBarvlDt());
        int bBarvlDt = Integer.parseInt(b.getBarvlDt());


        if (aBarvlDt != bBarvlDt) {
            // 도착까지 남은 시간이 다른 경우 비교
            return Integer.compare(aBarvlDt, bBarvlDt);
        }
        return 0; // 동일한 경우 다음 조건으로 넘어감
    }

    // NthStation에 따라 처리 순서 지정
    private int compareByNthStation(SubwayArriveProcessDTO.RealtimeArrival a, SubwayArriveProcessDTO.RealtimeArrival b) {
        // []역 번호를 추출
        int aNthStation = getNthStation(a.getArvlMsg2());
        int bNthStation = getNthStation(b.getArvlMsg2());

        if (aNthStation != bNthStation) {
            // 역 번호 비교
            return Integer.compare(aNthStation, bNthStation);
        }
        return 0; // 동일한 경우 우선순위 동일
    }


    private String handleArrivalByCriterion(SubwayArriveProcessDTO.RealtimeArrival arrival, String startName) {
        String arvlCdString = arrival.getArvlCd();
        System.out.println(arvlCdString);
        System.out.println(arrival.getBarvlDt());
        System.out.println(arrival.getArvlMsg2());
        if(!arvlCdString.equals("99")){
            return handleBasedOnArvlCd(arrival);
        }else if(!arrival.getBarvlDt().equals("0")){
            return handleBasedOnBarvlDt(arrival);
        }else if(arrival.getArvlMsg2() != null && arrival.getArvlMsg2().contains("번째 전역")) {
            return handleBasedOnNthStation(arrival, startName);
        }
        return arrival.getArvlMsg2();
    }

    // ARVL_CD 기준 처리
    private String handleBasedOnArvlCd(SubwayArriveProcessDTO.RealtimeArrival arrival) {
        String arvlCdString;
        switch (arrival.getArvlCd()) {
            case "0":
                arvlCdString = "진입";
                break;
            case "1":
                arvlCdString = "도착";
                break;
            case "2":
                arvlCdString = "출발";
                break;
            case "3":
                arvlCdString = "전역출발";
                break;
            case "4":
                arvlCdString = "전역진입";
                break;
            case "5":
                arvlCdString = "전역도착";
                break;
            default:
                arvlCdString = "알 수 없음";
        }
        return arvlCdString + " (" + arrival.getArvlMsg3() + ")";
    }
    // BARVL_DT 기준 처리
    private String handleBasedOnBarvlDt(SubwayArriveProcessDTO.RealtimeArrival arrival) {
        int barvlDt = Integer.parseInt(arrival.getBarvlDt());
        int minutes = barvlDt / 60; // 초를 분으로 변환
        return minutes > 0 ? minutes + "분 후 도착 " : "곧 도착 예정 (" + arrival.getArvlMsg3() + ")";
    }

    // NTH_STATION 기준 처리
    private String handleBasedOnNthStation(SubwayArriveProcessDTO.RealtimeArrival arrival, String startName) {
        String stationName = getStationNameFromMsg(arrival.getArvlMsg2());
        List<String> route = subwayService.findRoute(startName, stationName);
        int travelTime = subwayService.calculateTravelTime(route); // 두 역 간 이동 시간 계산
        return travelTime > 0 ? travelTime + "분 후 도착 " : "경로 정보 없음 (" + arrival.getArvlMsg3() + ")";
    }
    public CompletableFuture<Map.Entry<Integer, Map<String, Object>>> processTrafficType1Async(RouteProcessDTO.SubPath subPath, int index) {
        return CompletableFuture.supplyAsync(() -> {

                    Map<String, Object> timeAndDayType;

                    List<TimeTable> predictTimeList = null;
                    String predictTime1String = null;
                    String predictTime2String = null;
                    // 첫번째 역 이름
                    String startName = subPath.getStartName();
                    System.out.println(startName);
                    // 마지막 역 이름
                    String endName = subPath.getEndName();
                    System.out.println(endName);
                    // 노선 정보
                    int subwayCode = subPath.getLane().get(0).getSubwayCode();
                    System.out.println(subwayCode);
                    // 노선 이름
                    String subwayLineName = subPath.getLane().get(0).getName();
                    System.out.println(subwayLineName);
                    // 노선 방향
                    String direction = subPath.getWayCode() == 1 ? "상행" : "하행";
                    System.out.println(direction);

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
                    System.out.println("Second Station: " + secondStation);
                    // 결과 데이터를 저장할 Map 생성
                    Map<String, Object> dataMap = new HashMap<>();

                    // 지하철 도시 코드 확인
                //if(index < 2){ // 인덱스 0, 1에 대해서만 처리
                    System.out.println("index" + index);
                    Map<Integer, Integer> subwayCodeToCity = subwayService.getSubwayCodeToCityMapping();
                    System.out.println(subwayCodeToCity);
                    int city = subwayCodeToCity.getOrDefault(subwayCode, 0);
                    if (city == 10) {
                        // 시간표 기준 지하철
                        if (subwayService.isSpecialSeoulCode(subwayCode)) {
                            // 시간 확인
                            timeAndDayType = subwayService.getTimeAndDayType(subwayCode);
                            Time seoulTime = (Time) timeAndDayType.get("seoulTime");
                            String currentDayType = (String) timeAndDayType.get("currentDayType");
                            // 시간표 기준 도착정보 확인
                            predictTimeList = databaseService.findNextSubwayArrivals(subwayCode, startName, seoulTime, currentDayType);
                        } else {
                            // 오디세이 -> 서울 지하철 노선 코드 변환
                            int convertedSubwayCode = subwayService.convertSubwayCode(subwayCode);
                            // 역 이름 예외 처리
                            if (startName.equals("서울역")) {
                                startName = "서울";
                            }
                            else if (startName.equals("평택지제")){
                                startName = "지제";
                            }
                            // 역 ID 조회
                            int statn_id = databaseService.findStationId(convertedSubwayCode, startName);
                            System.out.print("statn_id" + statn_id);

                            // 실시간 도착 정보 조회
                            List<SubwayArriveProcessDTO.RealtimeArrival> realtimeArrivals = fetchAndSubwayArrive(startName);
                            if (realtimeArrivals != null && !realtimeArrivals.isEmpty()) {
                                List<SubwayArriveProcessDTO.RealtimeArrival> directionFilteredArrivals = new ArrayList<>();
                                List<SubwayArriveProcessDTO.RealtimeArrival> preliminaryArrivals = new ArrayList<>();

                                // 원하는 방향의 도착 정보 필터링
                                for (SubwayArriveProcessDTO.RealtimeArrival arrival : realtimeArrivals) {
                                    if (Integer.parseInt(arrival.getStatnId()) == statn_id) {
                                        System.out.println("Statn ID: " + statn_id);
                                        // 사용자가 원하는 방향인지 확인
                                        if (arrival.getUpdnLine().equals(direction)) {
                                            // TrainLineNm 조건 확인
                                            String trainLineNm = arrival.getTrainLineNm();
                                            if (trainLineNm != null) {
                                                String[] parts = trainLineNm.split(" - ");
                                                String destination = parts[0].replace("행", "").trim();
                                                String nextStation = parts[1].replace("방면", "").trim(); // 정확한 역 이름 추출
                                                if (parts.length > 1 &&  nextStation.equals(secondStation)) {
                                                    if (convertedSubwayCode == 1001) {
                                                        int result = subwayService.compareRoutes(startName,endName,startName,destination);
                                                        System.out.println("result" + result);
                                                        if (result == 1){
                                                            // 조건에 맞는 데이터를 리스트에 추가
                                                            directionFilteredArrivals.add(arrival);
                                                        }else if(result == 2){
                                                            preliminaryArrivals.add(arrival);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 처리 순서 정의
                                if (!directionFilteredArrivals.isEmpty()) {
                                    PriorityQueue<SubwayArriveProcessDTO.RealtimeArrival> arrival1Queue = new PriorityQueue<>(
                                            (a, b) -> {
                                                int result = compareByArvlCd(a, b);
                                                if (result != 0) {
                                                    lastCriterion = CompareCriterion.ARVL_CD;
                                                    return result;
                                                }

                                                result = compareByBarvlDt(a, b);
                                                if (result != 0) {
                                                    lastCriterion = CompareCriterion.BARVL_DT;
                                                    return result;
                                                }

                                                result = compareByNthStation(a, b);
                                                if (result != 0) {
                                                    lastCriterion = CompareCriterion.NTH_STATION;
                                                    return result;
                                                }

                                                lastCriterion = CompareCriterion.NONE; // 동일한 경우
                                                return 0;
                                            }
                                    );

                                    arrival1Queue.addAll(directionFilteredArrivals);
                                    System.out.println("최종 정렬 기준: " + lastCriterion);
                                    // 처리 순서 별 도착 정보 처리 및 출력
                                    if (!arrival1Queue.isEmpty()) {
                                        System.out.println(arrival1Queue);

                                        SubwayArriveProcessDTO.RealtimeArrival firstArrival = arrival1Queue.poll();
                                        predictTime1String = handleArrivalByCriterion(firstArrival,startName);
                                        System.out.println("가장 가까운 도착 정보: " + predictTime1String);

                                        if (!arrival1Queue.isEmpty()) {
                                            SubwayArriveProcessDTO.RealtimeArrival secondArrival = arrival1Queue.poll();
                                            predictTime2String = handleArrivalByCriterion(secondArrival,startName);                                            System.out.println("두 번째 도착 정보: " + predictTime2String);
                                        } else {
                                            System.out.println("두 번째 도착 정보가 없습니다.");
                                        }
                                    } else {
                                        System.out.println("조건에 맞는 도착 정보가 없습니다.");
                                    }
                                } else {
                                    PriorityQueue<SubwayArriveProcessDTO.RealtimeArrival> arrival2Queue = new PriorityQueue<>(
                                            (a, b) -> {
                                                int result = compareByArvlCd(a, b);
                                                if (result != 0) return result;

                                                result = compareByBarvlDt(a, b);
                                                if (result != 0) return result;

                                                result = compareByNthStation(a, b);
                                                return result;
                                            }
                                    );

                                    arrival2Queue.addAll(preliminaryArrivals);

                                    if (!arrival2Queue.isEmpty()) {
                                        SubwayArriveProcessDTO.RealtimeArrival firstArrival = arrival2Queue.poll();
                                        predictTime1String = handleArrivalByCriterion(firstArrival,startName);

                                        System.out.println("가장 가까운 도착 정보: " + predictTime1String);

                                        if (!arrival2Queue.isEmpty()) {
                                            SubwayArriveProcessDTO.RealtimeArrival secondArrival = arrival2Queue.poll();
                                            predictTime2String = handleArrivalByCriterion(secondArrival, startName);                                            System.out.println("두 번째 도착 정보: " + predictTime2String);
                                            System.out.println("두 번째 도착 정보: " + predictTime2String);
                                        } else {
                                            System.out.println("두 번째 도착 정보가 없습니다.");
                                        }
                                    } else {
                                        System.out.println("조건에 맞는 도착 정보가 없습니다.");
                                    }
                                }
                            }
                        }
                    } else if (city == 20 || city == 30 || city == 40 || city == 50) { // 부산, 대구, 광주, 대전
                        timeAndDayType = subwayService.getTimeAndDayType(subwayCode);
                        Time seoulTime = (Time) timeAndDayType.get("seoulTime");
                        String currentDayType = (String) timeAndDayType.get("currentDayType");
                        predictTimeList = databaseService.findNextSubwayArrivals(subwayCode, startName, seoulTime, currentDayType);
                    } else {
                        System.out.println("Unknown subway code: " + subwayCode);
                    }

                    // 결과 처리 (도착 시간 추출)
                    Time Time1;
                    Time Time2;

                    if (predictTimeList != null && !predictTimeList.isEmpty()) {
                        System.out.println("Predict Time List: " + predictTimeList);
                        Time1 = predictTimeList.size() > 0 ? predictTimeList.get(0).getArrival_Time() : null;
                        Time2 = predictTimeList.size() > 1 ? predictTimeList.get(1).getArrival_Time() : null;

                        Time seoulTime = subwayService.getSeoulCurrentTime();

                        if (Time1 != null) {
                            LocalTime arrivalTime1 = Time1.toLocalTime();
                            long predictTime1 = java.time.Duration.between(seoulTime.toLocalTime(), arrivalTime1).toMinutes();

                            // predictTime1을 String으로 변환
                            predictTime1String = predictTime1 + "분 후";
                            System.out.println(predictTime1String);
                        }
                        if (Time2 != null) {
                            LocalTime arrivalTime2 = Time2.toLocalTime();
                            long predictTime2 = java.time.Duration.between(seoulTime.toLocalTime(), arrivalTime2).toMinutes();
                            // predictTime2를 String으로 변환
                            predictTime2String = predictTime2 + "분 후";
                            System.out.println(predictTime2String);
                        }
                    }
                //}
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
                BusDetailProcessDTO.BusLaneDetail busDetail = fetchBusLaneDetail(busID);

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
                    System.out.println("index" + index);
                    // 도착 정보 조회
                    List<BusArriveProcessDTO.arriveDetail> arriveDetails = fetchAndBusArrive(startLocalStationID, busLocalBlID, startStationInfo);

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
                System.out.println("처리 완료된 subPath 인덱스: " + subPathIndex);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        return resultDTO;
    }
}