package com.example.back.dto.subway.realtime;

import lombok.Data;

import java.util.List;

@Data
public class RealSubwayLocationDTO {
    private Result result;
    private List<Row> row;

    @Data
    public class Result {
        private String code;
        private String developerMessage;
        private String link;
        private String message;
        private int status;
        private int total;
    }

    @Data
    public class Row {
        private int rowNum;
        private int selectedCount;
        private int totalCount;
        private int subwayId;
        private String subwayNm;
        private String statnId;
        private String statnNm;
        private String trainNo;
        private String lastRecptnDt;
        private String recptnDt;
        private int updnLine;
        private String statnTid;
        private String statnTnm;
        private int trainSttus;
        private int directAt;
        private int lstcarAt;
    }
}
