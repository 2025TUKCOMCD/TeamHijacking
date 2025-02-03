package com.example.back.dto;

import lombok.Data;

@Data
public class RouteDTO {
    private int startLat;
    private int startLng;
    private int endLat;
    private int endLng;
}
