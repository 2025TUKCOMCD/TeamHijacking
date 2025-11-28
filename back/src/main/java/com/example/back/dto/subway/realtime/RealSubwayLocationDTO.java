package com.example.back.dto.subway.realtime;

import lombok.Data;
import java.util.List;

@Data
public class RealSubwayLocationDTO {
    private ErrorMessageDTO errorMessage;
    private List<RealtimePositionDTO> realtimePositionList;

    @Data
    public static class ErrorMessageDTO {
        private int status;
        private String code;
        private int total;
    }

    @Data
    public static class RealtimePositionDTO {
        private int subwayId;   // String으로 변경
        private String subwayNm;
        private String statnId;
        private String statnNm;
        private String trainNo;
        private int lastRecptnDt;
        private String recptnDt;
        private int updnLine;   // String으로 변경
        private int statnTid;
        private String statnTnm;
        private int trainSttus; // String으로 변경
        private int directAt;   // String으로 변경
        private int lstcarAt;   // String으로 변경
    }
}