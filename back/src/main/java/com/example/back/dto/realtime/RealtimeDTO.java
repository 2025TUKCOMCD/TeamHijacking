package com.example.back.dto.realtime;

import lombok.Data;

@Data
public class RealtimeDTO {
    private String type;          // 버스, 지하철 도보 여부
    private boolean boarding;        // 탑승 여부(true/false)
    private String id;            // 대중교통 ID (busId, lineId 등)
    private String stationId;     // 버스/지하철 역 ID
    private String stationName;   // 지하철일 경우만 지하철 역 이름
    private String direction;     // 지하철일 경우만 방향 정보
}
