package com.example.back.dto.bus.detail;

import com.example.back.dto.route.RouteProcessDTO;
import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Data;

import java.util.List;

@JsonFilter("BusDetailProcess")
@Data
public class BusDetailProcessDTO {

    @Data
    public static class BusLaneDetail{
        private Result result;
    }

    @Data
    public static class Result {
            private String busNo;                // 버스 번호
            private int busID;                   // 버스 ID
            private int type;                    // 버스 타입
            private int busCityCode;             // 버스 도시 코드
            private List<Station> station;       // 정류장 목록
    }

    @Data
    public static class Station {
        private int idx;                // 정류장 인덱스
        private String stationName;     // 정류장 이름
        private int stationID;          // 정류장 지역 ID
    }
}
