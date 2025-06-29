package com.example.back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;


// 네트워크 및 시간 처리
@Service
public class SubwayService {

    @Autowired
    private NetworkService networkService;

    @Autowired
    private RouteTimeService routeTimeService;

    private static final Map<Integer, Integer> subwayCodeMap = Map.ofEntries(
            // 서울 실시간 통신 가능
            Map.entry(1, 1001), Map.entry(2, 1002), Map.entry(3, 1003), Map.entry(4, 1004),
            Map.entry(5, 1005), Map.entry(6, 1006), Map.entry(7, 1007), Map.entry(8, 1008),
            Map.entry(9, 1009), Map.entry(91, 1032), Map.entry(104, 1063), Map.entry(101, 1065),
            Map.entry(108, 1067), Map.entry(116, 1075), Map.entry(109, 1077), Map.entry(112, 1081),
            Map.entry(113, 1092), Map.entry(114, 1093), Map.entry(117, 1094),
            // 102 자기부상철도 중단
            // 서울 실시간 통신 불가능
            Map.entry(107, 107), Map.entry(110, 100),Map.entry(115, 115),
            Map.entry(21, 21),Map.entry(22, 22),
            // 부산 실시간 통신 불가능
            Map.entry(71, 71), Map.entry(72, 72),Map.entry(73, 73),
            Map.entry(74, 74),Map.entry(78, 78), Map.entry(79, 79),
            // 대구 실시간 통신 불가능
            Map.entry(41, 41), Map.entry( 42,42), Map.entry(43,4),Map.entry( 48,48),
            // 광주 실시간 통신 불가능
            Map.entry(51,51),
            // 대전 실시간 통신 불가능
            Map.entry(31,31)
    );

    private static final Map<Integer, String> subwayMap = Map.ofEntries(
            Map.entry(1001, "1호선"), Map.entry(1002, "2호선"),Map.entry(1003, "3호선"),
            Map.entry(1004, "4호선"), Map.entry(1005, "5호선"), Map.entry(1006, "6호선"),
            Map.entry(1007, "7호선"), Map.entry(1008, "8호선"), Map.entry(1009, "9호선"),
            Map.entry(1032, "GTX-A"), Map.entry(1063, "경의중앙선"), Map.entry(1065, "공항철도"),
            Map.entry(1067, "경춘선"), Map.entry(1075, "수인분당선"), Map.entry(1077, "신분당선"),
            Map.entry(1081, "경강선"), Map.entry(1092, "우이신설선"), Map.entry(1093, "서해선"),
            Map.entry(1094, "신림선")
            );


    // 오디세이 -> 서울 지하철 노선 코드 변환
    public int convertSubwayCode(int subwayCode) {
        return subwayCodeMap.getOrDefault(subwayCode, 0);
    }

    // 서울 지하철 노선 코드 제한
    public String convertRouteName(int subwayCode){
        return subwayMap.getOrDefault(subwayCode,"알 수 없는 노선");
    }

    // 구간별 소요시간 지하철 노선 코드 반환
    // 1001,1004,1008,1009,1032,1065,1067,1075,1077,1081,1093,1094 일때만
    public Boolean convertSubwayCodeForTime(int subwayCode) {
        if (subwayCode == 1001 || subwayCode == 1004 || subwayCode == 1008 || subwayCode == 1009 ||
                subwayCode == 1032 || subwayCode == 1065 || subwayCode == 1067 || subwayCode == 1075 ||
                subwayCode == 1077 || subwayCode == 1081 || subwayCode == 1093 || subwayCode == 1094) {
            return true;
        } else {
            return false; // Invalid code
        }
    }


    // 지하철 데이터베이스 코드 확인
    public boolean DBCode(int subwayCode) {
        List<Integer> DBCodes = Arrays.asList(107, 110, 115, 21, 22, 71, 72, 73, 74, 78, 79, 41, 42, 43, 48, 31, 51);
        return DBCodes.contains(subwayCode);
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
        // 1. findRoute 메서드 호출 시 입력 파라미터 로깅
        System.out.println("findRoute 호출: subwayCode=" + subwayCode + ", start='" + start + "', end='" + end + "'");

        Map<String, List<String>> network = networkService.getNetwork(subwayCode,start);

        // 2. networkService.getNetwork() 결과 로깅
        if (network == null) {
            System.out.println("networkService.getNetwork() 결과: null 반환됨. 네트워크 정보 없음.");
            return Collections.emptyList();
        }
        if (network.isEmpty()) {
            System.out.println("networkService.getNetwork() 결과: 빈 네트워크 반환됨. 네트워크 정보 없음.");
            System.out.println("네트워크 정보가 없습니다. 올바른 지하철 코드를 입력했는지 확인하세요."); // 기존 메시지 유지
            return Collections.emptyList();
        }
        System.out.println("networkService.getNetwork() 결과: 네트워크 로드 성공. 네트워크 크기: " + network.size());
        // 네트워크 내용이 너무 크지 않다면 일부 또는 전체를 로깅할 수도 있습니다. (주의: 로그가 너무 길어질 수 있음)
        // System.out.println("네트워크 내용 (일부): " + network); // 디버깅 시에만 사용 권장

        List<String> route = routeTimeService.findRoute(network, start, end);

        // 3. routeTimeService.findRoute() 결과 로깅
        if (route == null || route.isEmpty()) {
            System.out.println("routeTimeService.findRoute() 결과: 경로를 찾을 수 없거나 빈 경로 반환됨.");
        } else {
            System.out.println("routeTimeService.findRoute() 결과: 경로 찾음. 경로: " + route);
        }

        return route;
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

    // 경로 별 시간 추출
    public int[] findTime(List<String>route, String direction,int convertedCode) {
        Map<String, Integer> travelTimeMap = routeTimeService.getStationTravelTime(convertedCode);
        int[] travelTimes = new int[route.size() - 1];
        if (direction.equals("상행")) {
            for (int i = 0; i < route.size() - 1; i++) {
                String key = route.get(i) + "-" + route.get(i + 1);
                // stationId에 따른 네트워크 선택 및 시간 계산
                travelTimes[i] = travelTimeMap.getOrDefault(key, 0); // 네트워크 이동 시간 가
                System.out.println(travelTimes[i]);
            }
        }
        // 하행
        else if (direction.equals("하행")) {
            for (int i = 0; i < route.size() - 1; i++) { // 상행과 동일하게 순방향 루프
                String key = route.get(i) + "-" + route.get(i + 1); // 올바른 키: 이전 역 -> 다음 역
                travelTimes[i] = travelTimeMap.getOrDefault(key, 0); // 배열에 순차적으로 저장
                System.out.println("하행 " + route.get(i) + "->" + route.get(i+1) + " 시간: " + travelTimes[i]);
            }
        }
        return travelTimes;
    }

    // 두 역 간 이동 시간을 계산합니다.
    public int calculateTravelTime(List<String> route, String direction, int subwayCode) {
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
        System.out.println("TotalTime : " + totalTime);
        // 두 역 간 이동 시간을 더하기
        return totalTime;
    }
}
