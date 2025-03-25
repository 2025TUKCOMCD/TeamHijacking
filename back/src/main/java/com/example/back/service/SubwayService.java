package com.example.back.service;

import org.springframework.stereotype.Service;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class SubwayService {
    private static final Map<Integer, Integer> subwayCodeMap = Map.ofEntries(
            Map.entry(1, 1001), Map.entry(2, 1002), Map.entry(3, 1003), Map.entry(4, 1004),
            Map.entry(5, 1005), Map.entry(6, 1006), Map.entry(7, 1007), Map.entry(8, 1008),
            Map.entry(9, 1009), Map.entry(91, 1032), Map.entry(104, 1063), Map.entry(101, 1065),
            Map.entry(108, 1067), Map.entry(116, 1075), Map.entry(109, 1077), Map.entry(112, 1081),
            Map.entry(113, 1092), Map.entry(114, 1093), Map.entry(117, 1094)
    );

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
        System.out.println("현재 요일 타입: " + currentDayType);
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
}
