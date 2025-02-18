package com.example.back.dto.route;

import lombok.Data;

import java.util.List;

@Data
public class RouteResultDTO {
    private int totalTime;
    private int transitCount;
    private String mainTransitType;
    private String detailedPath;
    private String DetailTrans;
}
