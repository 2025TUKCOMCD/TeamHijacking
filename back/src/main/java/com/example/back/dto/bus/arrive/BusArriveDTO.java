package com.example.back.dto.bus.arrive;

import lombok.Data;

@Data
public class BusArriveDTO {
    private String stationName;
    private int stId;
    private int busRouteId;
    private int ord;
    private String resultType ;
}
