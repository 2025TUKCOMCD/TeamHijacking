package com.example.back.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResultDTO {
    private int totalTime;
    private int transitCount;
    private String mainTransitType;
    private String detailedPath;
    private String DetailTrans;
    private List<Integer> busRouteId;
    private String predictTime1;
    private String predictTime2;
}
