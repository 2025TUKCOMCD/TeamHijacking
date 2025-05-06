package com.example.back.dto.realtime;

import lombok.Data;

@Data
public class RealtimeDTO {
    private int type;               // 지하철(1), 버스(2),도보(3) 여부
    private boolean boarding;       // 탑승 여부(true/false)
    private int transportLocalID;   // 대중교통 역 ID (stid)
    private int stationId;          // 노선 ID (busRouteId, lineId)
    private int startOrd;           // 버스 위치 정보 첫번째 순서
    private int endOrd;             // 버스 위치 정보 마지막 순서
    private String stationName;     // 지하철일 경우만 지하철 역 이름
    private String direction;       // 지하철일 경우만 방향 정보
}
