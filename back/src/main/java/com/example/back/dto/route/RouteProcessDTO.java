package com.example.back.dto.route;

import lombok.Data;

import java.util.List;

@Data
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
        private Integer startID;
        private Integer endID;
        private List<Lane> lane; // 사용
        private Integer sectionTime; // 사용
        private String startLocalStationID;
        private PassStopList passStopList;
        private String endLocalStationID;
    }

    @Data
    public static class Lane {
        private String name; // 사용
        private String busNo; // 사용
        private Integer type;
        private Integer busID; // 사용
        private Integer busCityCode; // 사용
        private List<PassStopList> passStopList;
    }

    @Data
    public static class PassStopList {
        private List<Stations> Stations; // 사용
    }

    @Data
    public static class Stations {
        private Integer stationID; // 사용
        private String stationName;
        private String stationNumber;
        private String stationType;
        private String localStationID; // 사용
    }


}
