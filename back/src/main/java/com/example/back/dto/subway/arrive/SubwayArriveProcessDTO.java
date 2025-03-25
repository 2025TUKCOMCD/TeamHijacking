package com.example.back.dto.subway.arrive;

import lombok.Data;

import java.util.List;

@Data
public class SubwayArriveProcessDTO {

    private ErrorMessage errorMessage;
    private List<RealtimeArrival> realtimeArrivalList;

    @Data
    public static class ErrorMessage {
        private int status;
        private String code;
        private String message;
        private String link;
        private String developerMessage;
        private int total;
    }

    @Data
    public static class RealtimeArrival {
        private String subwayId;
        private String updnLine;
        private String trainLineNm;
        private String barvlDt;
        private String statnId;
        private String statnNm;
        private String bstatnId;
        private String bstatnNm;
        private String arvlMsg2;
        private String arvlMsg3;

    }
}
