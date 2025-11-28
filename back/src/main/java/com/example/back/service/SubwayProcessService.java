package com.example.back.service;

import com.example.back.domain.TimeTable;
import com.example.back.dto.realtime.RealtimeDTO;
import com.example.back.dto.route.RouteProcessDTO;
import com.example.back.dto.subway.arrive.SubwayArriveProcessDTO;
import com.example.back.dto.subway.realtime.RealSubwayLocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Time;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

// 지하철 경로 처리
@Service
public class SubwayProcessService {
    @Autowired
    private SubwayService subwayService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private APIService apiService;



    // 오디세이 -> 서울 공공데이터 역이름 변환
    private String normalizeStationName(String name) {
        return switch (name) {
            case "서울역" -> "서울";
            case "평택지제" -> "지제";
            default -> name;
        };
    }



    // 서울 지하철 도착 정보 처리(환승 필요 지하철 포함)
    protected List<SubwayArriveProcessDTO.RealtimeArrival> processSeoulSubway(int convertedCode, String startName, String endName, String direction, String secondName) {

        // 오디세이 -> 서울 공공데이터 역이름 변경 (normalizeStationName 메서드는 이미 있다고 가정)
        String convertedStartName = normalizeStationName(startName);
        String convertedEndName = normalizeStationName(endName);
        String convertedSecondName = normalizeStationName(secondName); // 환승역 이름
        System.out.println(convertedStartName + "+" + convertedSecondName + "+" + convertedEndName);

        // 데이터베이스 역id 추출
        int statnId = databaseService.findStationId(convertedCode, convertedStartName);

        System.out.println("조회할 역 ID: " + statnId);
        List<SubwayArriveProcessDTO.RealtimeArrival> arrivals;
        try {
            // 도착정보 가져오기
            // apiService.fetchAndSubwayArrive(convertedStartName)가 반환하는 타입에 맞게 수정 필요
            // 예시: .getRealtimeArrivalList() 가 필요하다면 추가
            arrivals = apiService.fetchAndSubwayArrive(convertedStartName).getRealtimeArrivalList();
            System.out.println("API로부터 수신된 도착 정보 (총 " + arrivals.size() + "개): " + arrivals);
        } catch (IOException e) {
            throw new RuntimeException("지하철 도착 정보를 가져오는 중 오류 발생: " + e.getMessage(), e);
        }

        List<SubwayArriveProcessDTO.RealtimeArrival> matchedArrivals = new ArrayList<>();

        // 2호선 외 일반 노선에서 사용할 사용자 요청 경로 (미리 계산)
        List<String> userDesiredPathForGeneralLines = null;
        if (convertedCode != 1002) { // 2호선이 아닌 경우에만 최단 경로 계산
            userDesiredPathForGeneralLines = subwayService.findRoute(convertedCode, convertedStartName, convertedEndName);
            if (userDesiredPathForGeneralLines == null || userDesiredPathForGeneralLines.isEmpty()) {
                System.out.println("일반 노선 (" + convertedCode + "호선) 사용자 요청 경로를 찾을 수 없습니다: " + convertedStartName + " -> " + convertedEndName + ". 필터링을 건너뜀.");
                // return handleArrivalQueue(new ArrayList<>(), startName, convertedCode, direction);
                return new ArrayList<>();
            }
        }

        // --- 필터링 로직 시작 ---
        for (SubwayArriveProcessDTO.RealtimeArrival arrival : arrivals) {
            // 현재 역 ID와 일치하는 도착 정보만 필터링 (API가 해당 역 정보만 주지만, 혹시 다른 역 정보가 섞여 올 경우 대비)
            if (Integer.parseInt(arrival.getStatnId()) == statnId) {
                System.out.println("\n현재 처리 중인 도착 정보: " + arrival);

                String apiUpdnLine = arrival.getUpdnLine(); // 내선, 외선
                String apiTrainLineNm = arrival.getTrainLineNm();
                String apiBstatnNm = arrival.getBstatnNm(); // 열차의 최종 종착역 이름

                boolean isMatchingDirection = false;
                boolean isDestinationOnTrack = false; // 열차가 요청된 목적지 방향으로 가는지 (해당 경로를 경유하는지)

                if (convertedCode == 1002) { // 2호선 특수 처리
                    boolean isBranchLineTrain = false; // 지선 열차인지 여부

                    // 지선 열차 식별 (trainLineNm 또는 bstatnNm 활용)
                    // 예: '성수지선', '신도림지선'이라는 문구가 trainLineNm에 포함되거나,
                    // bstatnNm이 지선 종착역인 경우 (까치산, 신설동, 용두)
                    if (apiBstatnNm.equals("까치산") || apiBstatnNm.equals("신설동")  ||
                            apiTrainLineNm.contains("까치산행") || apiTrainLineNm.contains("신설동행") ) {
                        isBranchLineTrain = true;
                    }

                    if (isBranchLineTrain) {
                        System.out.println("  -> 2호선 지선 열차 감지: " + apiTrainLineNm);
                        // 지선 열차는 일반적인 상행/하행 개념으로 판단
                        if (apiUpdnLine.equals(direction)) { // API의 상하행과 요청 방향 일치
                            isMatchingDirection = true;
                            // 지선 열차의 경우 trainLineNm이나 bstatnNm에 요청 목적지가 포함되는지 확인
                            if (apiTrainLineNm.contains(convertedEndName) || apiBstatnNm.equals(convertedEndName) ||
                                    apiTrainLineNm.contains(convertedEndName + "행") || apiTrainLineNm.contains(convertedEndName + "방면")) {
                                isDestinationOnTrack = true;
                            }
                            // 추가적으로, 환승역(secondName)이 지선 경로에 있다면 더 확실
                            else if (!convertedSecondName.isEmpty() && apiTrainLineNm.contains(convertedSecondName + "방면")) {
                                isDestinationOnTrack = true;
                            }
                            // 필요하다면 지선 열차의 경우에도 compareRoutes를 사용하여 정확한 경로 포함 관계 판단 가능
                            // 다만, ODSay의 'secondName'이 실제 경유역인지 trainLineNm에 따라 다를 수 있어 신중
                        }
                    } else { // 2호선 본선 순환 열차
                        System.out.println("  -> 2호선 본선 순환 열차 감지: " + apiTrainLineNm);

                        // 1. 사용자 요청 경로의 '다음 역'을 파악
                        String userNextStationOnPath = "";
                        List<String> userPath = subwayService.findRoute(convertedCode, convertedStartName, convertedEndName);
                        if (userPath != null && userPath.size() > 1) {
                            userNextStationOnPath = userPath.get(1); // 사용자 경로의 시작역 바로 다음 역
                            System.out.println("    사용자 경로의 다음 역: " + userNextStationOnPath);
                        } else {
                            // 사용자 경로를 찾을 수 없거나 짧으면 이 열차는 제외 (혹은 다른 방법으로 처리)
                            System.out.println("    사용자 경로의 다음 역을 파악할 수 없어 2호선 본선 열차 제외됨.");
                            continue; // 다음 도착 정보로 넘어감
                        }

                        // 2. API trainLineNm에서 '방면' 역 추출
                        String apiTrainLineNmDirectionStation = "";
                        String[] parts = apiTrainLineNm.split(" - ");
                        if (parts.length > 1) {
                            apiTrainLineNmDirectionStation = parts[1].replace("방면", "").trim();
                            System.out.println("    API trainLineNm의 방면 역: " + apiTrainLineNmDirectionStation);
                        }

                        // 전제: API 'updnLine'이 '내선'이면 상행, '외선'이면 하행으로 매핑
                        if (direction.equals("상행") && apiUpdnLine.equals("내선")) { // 요청이 상행(내선)이고 API가 '내선'
                            isMatchingDirection = true;
                            // 열차의 '방면' 역이 사용자 경로의 다음 역과 일치하는지 확인
                            if (!userNextStationOnPath.isEmpty() && apiTrainLineNmDirectionStation.equals(userNextStationOnPath)) {
                                isDestinationOnTrack = true;
                            }
                            // 방면 정보가 불확실하거나 없는 경우, 보조적으로 성수행/시청행을 사용
                            else if (apiTrainLineNm.contains("성수행") || apiTrainLineNm.contains("시청행")) {
                                // 이 경우 'isDestinationOnTrack'이 true가 되지만, 정확도는 떨어질 수 있음
                                // 2호선 본선은 순환하므로 성수/시청행은 항상 해당 방면으로 가는 것과 같음.
                                // 하지만 '잠실새내방면'과 같은 구체적인 정보가 있다면 그게 더 정확.
                                isDestinationOnTrack = true;
                            }

                        } else if (direction.equals("하행") && apiUpdnLine.equals("외선")) { // 요청이 하행(외선)이고 API가 '외선'
                            isMatchingDirection = true;
                            // 열차의 '방면' 역이 사용자 경로의 다음 역과 일치하는지 확인
                            if (!userNextStationOnPath.isEmpty() && apiTrainLineNmDirectionStation.equals(userNextStationOnPath)) {
                                isDestinationOnTrack = true;
                            }
                            // 방면 정보가 불확실하거나 없는 경우, 보조적으로 성수행/시청행을 사용
                            else if (apiTrainLineNm.contains("성수행") || apiTrainLineNm.contains("시청행")) {
                                isDestinationOnTrack = true;
                            }
                        }
                    }

                    if (isMatchingDirection && isDestinationOnTrack) {
                        matchedArrivals.add(arrival);
                        System.out.println("  -> 2호선 필터링 통과: " + apiTrainLineNm + " (API updnLine: " + apiUpdnLine + ")");
                    } else {
                        System.out.println("  -> 2호선 필터링 제외: " + apiTrainLineNm + " (API updnLine: " + apiUpdnLine + ")" +
                                " 요청방향=" + direction + ", 목표역=" + convertedEndName +
                                ", 방향일치=" + isMatchingDirection + ", 목적지방향일치=" + isDestinationOnTrack +
                                ", 지선여부=" + isBranchLineTrain);
                    }

                } else { // 2호선 외의 일반 노선 처리 (기존 compareRoutes 활용)
                    System.out.println("  -> 일반 노선 열차 감지: " + apiTrainLineNm);
                    String[] parts = apiTrainLineNm.split(" - ");
                    String destination = parts[0].replace("행", "").trim(); // 열차의 최종 목적지 (예: "병점행" -> "병점")

                    // 사용자의 요청 경로(userDesiredPathForGeneralLines)와 이 열차의 운행 방향(startName -> destination)을 비교
                    // compareRoutes는 startName -> endName 경로와 startName -> destination 경로를 비교하여
                    // 첫 번째 경로가 두 번째 경로의 부분 경로이거나 동일 방향인지 판단합니다.
                    int result = subwayService.compareRoutes(convertedCode, convertedStartName, convertedEndName, convertedStartName, destination);
                    System.out.println("  -> 일반 노선 경로 비교 결과: " + result + " (" + convertedStartName + "->" + convertedEndName + " vs " + convertedStartName + "->" + destination + ")");

                    if (result == 1 || result == 2) {
                        matchedArrivals.add(arrival);
                        System.out.println("  -> 일반 노선 필터링 통과: " + apiTrainLineNm + ", 결과: " + result);
                    } else {
                        System.out.println("  -> 일반 노선 필터링 제외: " + apiTrainLineNm + ", 경로 비교 결과: " + result);
                    }
                }
            }
        }
        // --- 필터링 로직 종료 ---

        System.out.println("필터링된 최종 도착 정보: " + matchedArrivals);
        // handleArrivalQueue는 기존과 동일하게 사용 (Comparator는 이전 답변에서 개선된 버전으로 가정)
        return matchedArrivals;
        //return handleArrivalQueue(matchedArrivals, startName, convertedCode, direction);
    }

    // 시간표 추출
    protected List<TimeTable> fetchPredictedTimes(int subwayCode, String startName, String direction) {
        Map<String, Object> timeAndDayType = subwayService.getTimeAndDayType(subwayCode);
        Time seoulTime = (Time) timeAndDayType.get("seoulTime");
        String currentDayType = (String) timeAndDayType.get("currentDayType");
        String convertDirection = "up";
        if(direction.equals("상행")) {
            convertDirection = "up";
        }else if(direction.equals("하행")) {
            convertDirection = "dn";
        }
        // 데이터베이스 도착정보 조회
        List<TimeTable> predictTimeTable = databaseService.findNextSubwayArrivals(subwayCode, startName, seoulTime, currentDayType, convertDirection);

        return predictTimeTable;
    }

    protected List<TimeTable> dbFindRoute(int subwayCode, String startName, String trainNo, String direction) {
        Map<String, Object> timeAndDayType = subwayService.getTimeAndDayType(subwayCode);
        Time seoulTime = (Time) timeAndDayType.get("seoulTime");
        String currentDayType = (String) timeAndDayType.get("currentDayType");
        String convertDirection = "up";
        if(direction.equals("상행")) {
            convertDirection = "up";
        }else if(direction.equals("하행")) {
            convertDirection = "dn";
        }
        // 데이터베이스 도착정보 조회
        List<TimeTable> predictTimeTable = databaseService.findTrainNoSubwayArrivals(subwayCode, startName, seoulTime, currentDayType, trainNo ,convertDirection);

        return predictTimeTable;
    }

    // 구간 별 소요 시간 추출
    protected int[] getBetweenTimes(int convertedCode, String startName, String endName, String direction) {
        List<String> route = subwayService.findRoute(convertedCode, startName, endName);  // 경로 탐색

        return subwayService.findTime(route, direction, convertedCode);
    }

    // 도착정보 처리 기준 및 큐
    protected String[] handleArrivalQueue(List<SubwayArriveProcessDTO.RealtimeArrival> matched, String startName, int subwayCode, String direction) {
        PriorityQueue<SubwayArriveProcessDTO.RealtimeArrival> queue = new PriorityQueue<>(
                Comparator
                        // ARVL_CD 기준 정렬
                        .comparingInt((SubwayArriveProcessDTO.RealtimeArrival a) -> getArvlCdPriority(a.getArvlCd()))
                        // BARVL_DT가 0이면 travelTime을 사용, 그렇지 않으면 더 작은 값으로 정렬
                        .thenComparingInt(a -> {
                            int barvlDt = Integer.parseInt(a.getBarvlDt());  // 도착 예정 시간 (초 단위)
                            String stationName = getStationNameFromMsg(a.getArvlMsg2());  // 역 이름 추출
                            System.out.println("역 이름: " + stationName);
                            List<String> route = subwayService.findRoute(subwayCode, startName, stationName);  // 경로 탐색
                            System.out.println("경로: " + route);
                            int travelTime = subwayService.calculateTravelTime(route, direction, subwayCode); // 예상 이동 시간 (분 단위)
                            System.out.println("예상 이동 시간: " + travelTime + "분");
                            int travelTimeInSeconds = travelTime * 60;  // 분 → 초 변환

                            return (barvlDt == 0) ? travelTimeInSeconds : Math.min(barvlDt, travelTimeInSeconds); // 더 빠른 값으로 정렬
                        })
        );

        // matched 리스트를 PriorityQueue에 추가
        queue.addAll(matched);
        System.out.println("정렬된 도착 정보: " + queue);
        String predictTime1String = "첫 번째 도착 정보 없음"; // 기본값으로 초기화
        String predictTime2String = "두 번째 도착 정보 없음"; // 기본값으로 초기화
        String trainNo1String = "0";
        String trainNo2String = "0";

        if (!queue.isEmpty()) {
            SubwayArriveProcessDTO.RealtimeArrival first = queue.poll();
            System.out.println(">>> PriorityQueue에서 첫 번째로 poll된 객체: " + first.getArvlMsg2() + ", ARVL_CD: " + first.getArvlCd());
            predictTime1String = handleArrivalByCriterion(first, startName, subwayCode, direction);
            trainNo1String = first.getBtrainNo();

            if (!queue.isEmpty()) {
                SubwayArriveProcessDTO.RealtimeArrival second = queue.poll();
                System.out.println(">>> PriorityQueue에서 두 번째로 poll된 객체: " + second.getArvlMsg2() + ", ARVL_CD: " + second.getArvlCd());
                predictTime2String = handleArrivalByCriterion(second, startName, subwayCode, direction);
            } if(!queue.isEmpty()) {
                System.out.println("두 번째 도착 정보가 없습니다.");
            }
        } else {
            System.out.println("조건에 맞는 도착 정보가 없습니다.");
        }
        return new String[]{predictTime1String, predictTime2String,trainNo1String};
    }

    // 데이터베이스 도착정보 처리
    protected String[] processPredictedArrivals(List<TimeTable> predictTimeList) {
        String predictTime1String = "첫 번째 도착 정보 없음"; // 기본값으로 초기화
        String predictTime2String = "두 번째 도착 정보 없음"; // 기본값으로 초기화
        if (predictTimeList != null && !predictTimeList.isEmpty()) {
            Time Time1 = predictTimeList.size() > 0 ? predictTimeList.get(0).getArrival_Time() : null;
            Time Time2 = predictTimeList.size() > 1 ? predictTimeList.get(1).getArrival_Time() : null;

            Time current = subwayService.getSeoulCurrentTime();
            if (Time1 != null) {
                long minutes = Duration.between(current.toLocalTime(), Time1.toLocalTime()).toMinutes();
                if(minutes > 60){
                    predictTime1String = "운행종료";
                }else {
                    predictTime1String = minutes + "분 후";
                }
            }
            if (Time2 != null) {
                long minutes = Duration.between(current.toLocalTime(), Time2.toLocalTime()).toMinutes();
                if(minutes > 60) {
                    predictTime1String = "운행종료";
                }else{
                    predictTime2String = minutes + "분 후";
                }
            }
        }
        return new String[]{predictTime1String, predictTime2String};
    }

    // 데이터베이스 열차정보 가져오기
    protected String processPredictedTrainInfo(List<TimeTable> predictTimeList) {
        String trainNo1String = "0";
        String trainNo2String = "0";
        if (predictTimeList != null && !predictTimeList.isEmpty()) {
            TimeTable firstArrival = predictTimeList.get(0);
            trainNo1String = firstArrival.getTrain_ID();
        }
        return trainNo1String;
    }


    // 실시간 도착정보 처리
    private String handleArrivalByCriterion(SubwayArriveProcessDTO.RealtimeArrival arrival, String startName, int subwayCode, String direction) {
        String arvlCdString = arrival.getArvlCd();
        if (!arvlCdString.equals("99")) {
            return handleBasedOnArvlCd(arrival);
        } else if (!arrival.getBarvlDt().equals("0")) {
            return handleBasedOnBarvlDt(arrival);
        } else if (arrival.getArvlMsg2() != null && arrival.getArvlMsg2().contains("번째 전역")) {
            return handleBasedOnNthStation(arrival, startName, direction, subwayCode);
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
    private String handleBasedOnNthStation(SubwayArriveProcessDTO.RealtimeArrival arrival, String startName, String direction, int subwayCode) {
        String stationName = getStationNameFromMsg(arrival.getArvlMsg2());
        // FindNetwork로 이전
        List<String> route = subwayService.findRoute(subwayCode, startName, stationName);
        // TimeFindNetwork로 이전
        int travelTime = subwayService.calculateTravelTime(route, direction, subwayCode); // 두 역 간 이동 시간 계산

        return travelTime > 0 ? travelTime + "분 후 도착" + "(" + stationName + ")" : "경로 정보 없음 (" + arrival.getArvlMsg3() + ")";
    }

    // 처리 순서
    private int getArvlCdPriority(String arvlCd) {
        switch (arvlCd) {
            case "0":
            case "1":
            case "2":
            case "3":
                return 0;
            case "4":
            case "5":
                return 1;
            default:
                return 2;
        }
    }

    // arvlMsg2에서 역 이름 추출
    private String getStationNameFromMsg(String arvlMsg2) {
        if (arvlMsg2.contains("번째 전역") && arvlMsg2.contains("(") && arvlMsg2.contains(")")) {
            // () 내부에 있는 내용을 추출
            return arvlMsg2.replaceAll(".*\\(([^)]+)\\).*", "$1");
        }
        return ""; // 역 이름을 찾을 수 없으면 빈 문자열 반환
    }

    // 실시간 위치정보 현재 위치 특정
    protected String getTranNo(RealtimeDTO data) {
        String nowStation = data.getStartName();
        int subwayCode = data.getTransportLocalID();
        int convertedDirection = data.getDirection().equals("상행") ? 0 : 1; // 상행: 0, 하행: 1
        int convertedCode = subwayService.convertSubwayCode(subwayCode);
        String routeName = subwayService.convertRouteName(convertedCode);

        RealSubwayLocationDTO realSubwayLocation;
        // API 호출

        try {
            realSubwayLocation = apiService.fetchAndSubwayLocation(routeName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<RealSubwayLocationDTO.RealtimePositionDTO> stations = realSubwayLocation.getRealtimePositionList();
        if(stations == null || stations.isEmpty()) {
            throw new IllegalStateException("실시간 위치 정보가 없습니다.");
        }

        for (RealSubwayLocationDTO.RealtimePositionDTO station : stations) {
            System.out.println(station.getStatnNm());
            if (station.getStatnNm().equals(nowStation) && station.getUpdnLine()==convertedDirection) {
                // 해당 열차의 고유 번호 반환
                String trainNo = station.getTrainNo();

                return trainNo ; // trainNo가 int 타입이어야 합니다
            }
        }
        // 못 찾았을 경우 예외 발생 또는 기본값 설정
        throw new IllegalStateException("해당 역에 위치한 열차 정보를 찾을 수 없습니다.");
    }

    // 실시간 위치정보 현재 위치 추출
    protected String getLocation(RealtimeDTO data) {
        String trainNo = data.getTrainNo(); // 열차 고유 번호
        int subwayCode = data.getTransportLocalID(); // 지하철 노선 코드
        int convertedDirection = data.getDirection().equals("상행") ? 0 : 1; // 상행: 0, 하행: 1
        int convertedCode = subwayService.convertSubwayCode(subwayCode); // 변환 노선 코드
        String routeName = subwayService.convertRouteName(convertedCode); // 노선 이름 변환

        RealSubwayLocationDTO realSubwayLocation;
        // API 호출
        try {
            realSubwayLocation = apiService.fetchAndSubwayLocation(routeName); // 실시간 위치 정보 API 호출
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // trainNo가 같은경우를 찾아 statnNm 반환
        List<RealSubwayLocationDTO.RealtimePositionDTO> stations = realSubwayLocation.getRealtimePositionList();
        for (RealSubwayLocationDTO.RealtimePositionDTO station : stations) {
            if (station.getTrainNo().equals(trainNo) && station.getUpdnLine()==convertedDirection) {
                // 해당 역에 위치한 열차의 현재 위치 반환
                return station.getStatnNm(); // location이 int 타입이어야 합니다
            }
        }
        return null; // 못 찾았을 경우 -1 반환
    }
}
