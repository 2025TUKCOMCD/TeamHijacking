package com.example.back.dto.realtime;

import lombok.Data;

@Data
public class RealtimeDTO {
    private int type;                   // 지하철(1), 버스(2),도보(3) 여부
    private int boarding;               // 탑승 여부(1: 탑승 전, 2: 탑승 후, 3: 탑승 중)
    private int transportLocalID;       // 대중교통 : 노선/역 ID (busRouteId, lineId)
    private int DBUsage;                // DB 사용 여부 (0: 사용 안함, 1: 사용함)
    private int stationId;              // 버스 : 정류장 ID (stid)
    private int startOrd;               // 버스 : 위치 정보 첫번째 순서
    private int endOrd;                 // 버스 : 위치 정보 마지막 순서
    private int trainNo;                // 지하철 : 기차 번호
    private String vehid;               // 버스 : 차량 ID (vehId)
    private String startName;           // 지하철 : 지하철 역 이름
    private String secondName;          // 지하철 : 두번째 지하철 역 이름
    private String endName;             // 지하철 : 지하철 역 이름
    private String direction;           // 지하철 : 방향 정보
}
