package com.example.back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;



@Service
public class SubwayService {

    @Autowired
    private NetworkService networkService;

    @Autowired
    private RouteTimeService routeTimeService;

    private static final Map<Integer, Integer> subwayCodeMap = Map.ofEntries(
            Map.entry(1, 1001), Map.entry(2, 1002), Map.entry(3, 1003), Map.entry(4, 1004),
            Map.entry(5, 1005), Map.entry(6, 1006), Map.entry(7, 1007), Map.entry(8, 1008),
            Map.entry(9, 1009), Map.entry(91, 1032), Map.entry(104, 1063), Map.entry(101, 1065),
            Map.entry(108, 1067), Map.entry(116, 1075), Map.entry(109, 1077), Map.entry(112, 1081),
            Map.entry(113, 1092), Map.entry(114, 1093), Map.entry(117, 1094)
    );


    // 오디세이 -> 서울 지하철 노선 코드 변환
    public int convertSubwayCode(int subwayCode) {
        return subwayCodeMap.getOrDefault(subwayCode, 0);
    }

    // 서울 지하철 특수 코드 확인
    public boolean isSpecialSeoulCode(int subwayCode) {
        List<Integer> specialSeoulCodes = Arrays.asList(110, 115, 21, 22, 107);
        return specialSeoulCodes.contains(subwayCode);
    }

    // 서울 지하철 현재 시간 가져오기
    public static Time getSeoulCurrentTime() {
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        LocalTime seoulTime = ZonedDateTime.now(seoulZoneId).toLocalTime();
        return Time.valueOf(seoulTime);
    }

    // 서울 지하철 도착 정보 조회
    public Map<String, Object> getTimeAndDayType(int subwayCode) {
        Time seoulTime = getSeoulCurrentTime();
        String currentDayType = getCurrentDayType(subwayCode);
        Map<String, Object> result = new HashMap<>();
        result.put("seoulTime", seoulTime);
        result.put("currentDayType", currentDayType);
        return result;
    }

    // 현재 요일 계산
    public String getCurrentDayType(int subwayCode) {
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        DayOfWeek currentDay = ZonedDateTime.now(seoulZoneId).getDayOfWeek();
        if (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
            if (isWeekendRoute(subwayCode)) {
                if (currentDay == DayOfWeek.SATURDAY) {
                    return "sat"; // 토요일
                } else {
                    return "sun"; // 일요일
                }
            } else {
                return "wknd"; // 주말 (공통)
            }
        } else {
            return "wkdy"; // 평일
        }
    }

    // 특정 route_id가 주말에 토요일과 일요일을 구분해야 하는지 확인
    public boolean isWeekendRoute(int subwayCode) {
        List<Integer> weekendSpecificRoutes = Arrays.asList(71, 72, 73, 74, 41, 42, 43, 51, 110);
        return weekendSpecificRoutes.contains(subwayCode);
    }

    // 지하철 코드 - 도시 매핑 메서드
    public Map<Integer, Integer> getSubwayCodeToCityMapping() {
        Map<Integer, Integer> subwayCodeToCity = new HashMap<>();
        // 서울 지하철
        for (int code : new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 91, 101, 102, 104, 107, 108, 109, 110, 112, 113, 114, 115, 116, 117, 21, 22}) {
            subwayCodeToCity.put(code, 10);
        }
        // 부산
        for (int code : new int[]{71, 72, 73, 74, 78, 79}) {
            subwayCodeToCity.put(code, 20);
        }
        // 대구
        for (int code : new int[]{41, 42, 43, 48}) {
            subwayCodeToCity.put(code, 30);
        }
        // 광주
        subwayCodeToCity.put(51, 40);
        // 대전
        subwayCodeToCity.put(31, 50);

        return subwayCodeToCity;
    }

    // 경로 탐색
    public List<String> findRoute(int subwayCode, String start, String end) {
        return routeTimeService.findRoute(networkService.getNetwork(subwayCode,start), start, end);
    }

    // 경로 비교 메서드
    public int compareRoutes(int subwayCode,String aStart, String aEnd, String bStart, String bEnd) {

        List<String> routeA = findRoute(subwayCode,aStart, aEnd);
        List<String> routeB = findRoute(subwayCode,bStart, bEnd);


        if (routeA.isEmpty() || routeB.isEmpty()) {
            System.out.println("입력한 경로 중 하나 이상에서 경로를 찾을 수 없습니다.");
            return -1;
        }
        // 출발역이 같다고 가정하므로, 두 경로의 진행 방향 (초기 구간)이 같은지 확인
        else if (!routeTimeService.isSameDirection(routeA, routeB)) {
            System.out.println("두 경로는 시작점은 같으나, 진행 방향이 다릅니다.");
            return 0;
        }
        // A 경로가 B의 부분 경로인 경우 (A의 길이가 B보다 짧으면서 B의 접두어인지 KMP로 확인)
        else if (routeA.size() < routeB.size() && routeTimeService.isSubPath(routeA, routeB)) {
            System.out.println("A 경로는 B 경로의 연속적인 부분 경로입니다. (A ⊂ B)");
            return 1;
        }
        else {
            System.out.println("A 경로는 B 경로와 같은 방향이지만, 포함 관계에 있지 않습니다.");
            return 2;
        }
    }

    // 두 역 간 이동 시간을 계산합니다.
    public int calculateTravelTime(List<String> route, String direction,int subwayCode) {
        int totalTime = 0;
        Map<String, Integer> travelTimeMap = routeTimeService.getStationTravelTime(subwayCode);
        if (travelTimeMap == null) {
            System.out.println("No travel time data available for subwayCode: " + subwayCode);
            return -1; // Error case
        }

        // direction에 따른 route.get(i)와 route.get(i+1) 간의 이동 시간 계산
        // 상행
        if(direction.equals("상행")){
            for (int i = 0; i < route.size() - 1; i++) {
                String key = route.get(i) + "-" + route.get(i + 1);
                // stationId에 따른 네트워크 선택 및 시간 계산
                totalTime += travelTimeMap.getOrDefault(key, 0);
            }
        }
        // 하행
        else if(direction.equals("하행")){
            for (int i = route.size() - 1; i > 0; i--) {
                String key = route.get(i) + "-" + route.get(i - 1);
                    totalTime += travelTimeMap.getOrDefault(key, 0);
            }
        }
        // 두 역 간 이동 시간을 더하기
        return totalTime;
    }
}
