package com.example.back.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RouteIdSetDTO {
    private int transportLocalID; // 버스나 지하철의 공통 ID
    @JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
    private int startStationInfo; // 시작 정류장(또는 역) 정보
    @JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
    private int endStationInfo; // 도착 정류장(또는 역) 정보
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String subwayLineName; // 지하철 노선 이름 (예: "1호선", "2호선")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String trainDirection; // 지하철 방향 (예: 상행/하행)

    private Double startX; // 시작 지점의 X 좌표 (위도)
    private Double startY; // 시작 지점의 Y 좌표 (경도)

    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private List<String> transferStations = new ArrayList<>(); // 노선 중간과정의 정류장/역 정보
    // 공통으로 사용할 정류장/역 정보
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private List<Integer> stationInfo = new ArrayList<>(); // 정류장/역 정보 리스트
    // 예측 시간을 버스/지하철에 공통으로 사용
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String predictTimes1 ; // 여러 교통수단의 예상 도착 시간 리스트 1
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String predictTimes2 ; // 여러 교통수단의 예상 도착 시간 리스트 2
}
