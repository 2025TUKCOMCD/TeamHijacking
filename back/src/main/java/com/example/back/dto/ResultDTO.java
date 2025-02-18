package com.example.back.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResultDTO {
    private int totalTime;
    private int transitCount;
    private String mainTransitType;
    private String detailedPath;
    private String detailTrans;
    private List<List<Integer>> RouteIds = new ArrayList<>(); // 여러 버스의 버스 경로 ID 리스트
    private List<String> predictTimes1 = new ArrayList<>(); // 여러 버스의 predictTime1 리스트
    private List<String> predictTimes2 = new ArrayList<>(); // 여러 버스의 predictTime2 리스트
    private List<String> busErrors = new ArrayList<>();
    public void addBusDetail(String error) {
        busErrors.add(error);
    }
}
