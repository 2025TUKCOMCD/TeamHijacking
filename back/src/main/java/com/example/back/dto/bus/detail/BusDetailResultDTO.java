package com.example.back.dto.bus.detail;

import lombok.Data;
@Data
public class BusDetailResultDTO {
    private String stationName;
    private String busNO;
    private int startID;
    private int endID;
    private int busLocalBlID;
    private int startStationInfo;
    private int endStationInfo;


}
