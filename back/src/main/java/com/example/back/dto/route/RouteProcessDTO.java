package com.example.back.dto.route;

import lombok.Data;

import java.util.List;


public class RouteProcessDTO {
    @Data
    public static class SearchPath {
        private Result result;
    }

    @Data
    public static class Result {
        private List<Path> path;
    }

    @Data
    public static class Path {
        private int pathType; // 사용
        private Info info; // 사용
        private List<SubPath> subPath; // 사용
    }

    @Data
    public static class Info {
        private int totalTime; // 사용
        private int totalWalk; // 사용
        private int busTransitCount; // 사용
        private int subwayTransitCount; // 사용
    }

    @Data
    public static class SubPath {
        private int trafficType; // 사용
        private int stationCount; // 사용
        private Double distance;
        private String startName;
        private String endName;
        private Double startX;
        private Double startY;
        private int wayCode;
        private int startID;
        private int endID;
        private List<Lane> lane; // 사용
        private int sectionTime; // 사용
        private int startLocalStationID;
        private int endLocalStationID;
        private PassStopList passStopList;
    }

    @Data
    public static class Lane {
        private String name; // 사용
        private String busNo; // 사용
        private int type;
        private int busID; // 사용
        private int busCityCode; // 사용
        private int busLocalBlID;
        private int subwayCode;
    }

    @Data
    public static class PassStopList {
        private List<Station> stations; // 사용
    }

    @Data
    public static class Station {
        private int stationID; // 사용
        private String stationName;
        private String stationNumber;
        private String stationType;
        private int localStationID; // 사용
    }
}
