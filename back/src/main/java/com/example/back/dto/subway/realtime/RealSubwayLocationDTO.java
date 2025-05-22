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
        private String message;
        private String link;
        private String developerMessage;
        private int total;
    }

    @Data
    public static class RealtimePositionDTO {
        private Integer beginRow;
        private Integer endRow;
        private Integer curPage;
        private Integer pageRow;
        private int totalCount;
        private int rowNum;
        private int selectedCount;
        private String subwayId;   // String으로 변경
        private String subwayNm;
        private String statnId;
        private String statnNm;
        private String trainNo;
        private String lastRecptnDt;
        private String recptnDt;
        private String updnLine;   // String으로 변경
        private String statnTid;
        private String statnTnm;
        private String trainSttus; // String으로 변경
        private String directAt;   // String으로 변경
        private String lstcarAt;   // String으로 변경
    }
}