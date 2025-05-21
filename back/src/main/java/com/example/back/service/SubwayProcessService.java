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

    String predictTime1String, predictTime2String = null;

    // 오디세이 -> 서울 공공데이터 역이름 변환
    private String normalizeStationName(String name) {
        return switch (name) {
            case "서울역" -> "서울";
            case "평택지제" -> "지제";
            default -> name;
        };
    }

    // 서울 지하철 도착 정보 처리(환승 필요 지하철 포함)
    protected String[] processSeoulSubway(int convertedCode, String startName, String endName, String direction, String secondStation) {

        // 오디세이 -> 서울 공공데이터 역이름 변경
        startName = normalizeStationName(startName);
        // 데이터베이스 역id 추출
        int statnId = databaseService.findStationId(convertedCode, startName);

        List<SubwayArriveProcessDTO.RealtimeArrival> arrivals = null;
        try {
            // 도착정보 가져오기
            arrivals = apiService.fetchAndSubwayArrive(startName).getRealtimeArrivalList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (arrivals == null || arrivals.isEmpty()) return null;

        // 같은 방향 및 목적지 포함 노선
        List<SubwayArriveProcessDTO.RealtimeArrival> matchedArrivals = new ArrayList<>();
        // 같은 방향 및 중간 환승 포함 노선

        for (SubwayArriveProcessDTO.RealtimeArrival arrival : arrivals) {
            if (Integer.parseInt(arrival.getStatnId()) == statnId && direction.equals(arrival.getUpdnLine())) {
                String[] parts = arrival.getTrainLineNm().split(" - ");
                // 다음역
                if (parts.length > 1 && parts[1].replace("방면", "").trim().equals(secondStation)) {
                    // 도착역
                    String destination = parts[0].replace("행", "").trim();
                    // 경로 비교
                    int result = subwayService.compareRoutes(convertedCode, startName, endName, startName, destination);

                    if (result == 1 || result == 2) {
                        matchedArrivals.add(arrival);
                    }
                }
            }
        }

        return handleArrivalQueue(matchedArrivals, startName, convertedCode, direction);
    }

    // 시간표 추출
    protected String[] fetchPredictedTimes(int subwayCode, String startName) {
        Map<String, Object> timeAndDayType = subwayService.getTimeAndDayType(subwayCode);
        Time seoulTime = (Time) timeAndDayType.get("seoulTime");
        String currentDayType = (String) timeAndDayType.get("currentDayType");
        // 데이터베이스 도착정보 조회
        List<TimeTable> predictTimeList = databaseService.findNextSubwayArrivals(subwayCode, startName, seoulTime, currentDayType);
        return processPredictedArrivals(predictTimeList);
    }

    // 구간 별 소요 시간 추출
    protected int[] getBetweenTimes(int convertedCode, String startName, String endName, String direction) {
        List<String> route = subwayService.findRoute(convertedCode, startName, endName );  // 경로 탐색
        return subwayService.findTime(route, direction, convertedCode);
    }

    // 도착정보 처리 기준 및 큐
    private String[] handleArrivalQueue(List<SubwayArriveProcessDTO.RealtimeArrival> matched, String startName, int subwayCode, String direction) {
        PriorityQueue<SubwayArriveProcessDTO.RealtimeArrival> queue = new PriorityQueue<>(
                Comparator
                        // ARVL_CD 기준 정렬
                        .comparingInt((SubwayArriveProcessDTO.RealtimeArrival a) -> getArvlCdPriority(a.getArvlCd()))
                        // BARVL_DT가 0이면 travelTime을 사용, 그렇지 않으면 더 작은 값으로 정렬
                        .thenComparingInt(a -> {
                            int barvlDt = Integer.parseInt(a.getBarvlDt());  // 도착 예정 시간 (초 단위)
                            String stationName = getStationNameFromMsg(a.getArvlMsg2());  // 역 이름 추출
                            List<String> route = subwayService.findRoute(subwayCode, startName, stationName);  // 경로 탐색
                            int travelTime = subwayService.calculateTravelTime(route, direction, subwayCode); // 예상 이동 시간 (분 단위)
                            int travelTimeInSeconds = travelTime * 60;  // 분 → 초 변환

                            return (barvlDt == 0) ? travelTimeInSeconds : Math.min(barvlDt, travelTimeInSeconds); // 더 빠른 값으로 정렬
                        })
        );

        // matched 리스트를 PriorityQueue에 추가
        queue.addAll(matched);

        if (!queue.isEmpty()) {
            SubwayArriveProcessDTO.RealtimeArrival first = queue.poll();
            String predictTime1String = handleArrivalByCriterion(first, startName, subwayCode, direction);

            if (!queue.isEmpty()) {
                SubwayArriveProcessDTO.RealtimeArrival second = queue.poll();
                String predictTime2String = handleArrivalByCriterion(second, startName, subwayCode, direction);
                return new String[]{predictTime1String, predictTime2String};
            } else {
                System.out.println("두 번째 도착 정보가 없습니다.");
                return new String[]{predictTime2String, "두 번째 도착 정보 없음"};
            }
        } else {
            System.out.println("조건에 맞는 도착 정보가 없습니다.");
            return new String[]{predictTime1String, predictTime2String};
        }
    }

    // 데이터베이스 도착정보 처리
    protected String[] processPredictedArrivals(List<TimeTable> predictTimeList) {
        if (predictTimeList != null && !predictTimeList.isEmpty()) {
            Time Time1 = predictTimeList.size() > 0 ? predictTimeList.get(0).getArrival_Time() : null;
            Time Time2 = predictTimeList.size() > 1 ? predictTimeList.get(1).getArrival_Time() : null;

            Time current = subwayService.getSeoulCurrentTime();
            if (Time1 != null) {
                long minutes = Duration.between(current.toLocalTime(), Time1.toLocalTime()).toMinutes();
                predictTime1String = minutes + "분 후";
            }
            if (Time2 != null) {
                long minutes = Duration.between(current.toLocalTime(), Time2.toLocalTime()).toMinutes();
                predictTime2String = minutes + "분 후";
            }
        }
        return new String[]{predictTime1String, predictTime2String};
    }

    // 실시간 도착정보 처리
    private String handleArrivalByCriterion(SubwayArriveProcessDTO.RealtimeArrival arrival, String startName,int subwayCode, String direction) {
        String arvlCdString = arrival.getArvlCd();
        if(!arvlCdString.equals("99")){
            return handleBasedOnArvlCd(arrival);
        }else if(!arrival.getBarvlDt().equals("0")){
            return handleBasedOnBarvlDt(arrival);
        }else if(arrival.getArvlMsg2() != null && arrival.getArvlMsg2().contains("번째 전역")) {
            return handleBasedOnNthStation(arrival, startName, direction,subwayCode);
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
    private String handleBasedOnNthStation(SubwayArriveProcessDTO.RealtimeArrival arrival, String startName, String direction,int subwayCode) {
        String stationName = getStationNameFromMsg(arrival.getArvlMsg2());
        // FindNetwork로 이전
        List<String> route = subwayService.findRoute(subwayCode,startName, stationName);
        // TimeFindNetwork로 이전
        int travelTime = subwayService.calculateTravelTime(route,direction,subwayCode); // 두 역 간 이동 시간 계산

        return travelTime > 0 ? travelTime + "분 후 도착" + "(" + stationName + ")" : "경로 정보 없음 (" + arrival.getArvlMsg3() + ")";
    }

    // 처리 순서
    private int getArvlCdPriority(String arvlCd) {
        switch (arvlCd) {
            case "0": case "1": case "2": case "3": return 0;
            case "4": case "5": return 1;
            default: return 2;
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
        String trainNm = null;
        String nowStation = data.getStartstationName();
        int subwayCode = data.getTransportLocalID();
        int convertedCode = subwayService.convertSubwayCode(subwayCode);
        String routeName = subwayService.convertRouteName(convertedCode);

        RealSubwayLocationDTO realSubwayLocation;
        // API 호출

        try {
            realSubwayLocation = apiService.fetchAndSubwayLocation(routeName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 실시간 위치정보에서 역 이름과 방향이 같은 역 찾기
        List<RealSubwayLocationDTO.RealtimePositionDTO> stations = realSubwayLocation.getRealtimePositionList();
        String[][] stationInfo = new String[stations.size()][2];
        for (int i = 0; i < stations.size(); i++) {
            stationInfo[i][0] = stations.get(i).getUpdnLine();
            stationInfo[i][1] = stations.get(i).getStatnNm();
        }

        // 역이름 과 방향이 같을 경우 stations의 trainNm추출
        for (String[] station : stationInfo) {
            if (station[1].equals(nowStation) && station[0].equals(data.getDirection())) {
                return trainNm = station[0];
            }
        }
        return null;
    }
}
