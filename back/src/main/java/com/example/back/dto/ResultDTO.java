package com.example.back.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResultDTO {
    private int totalTime;
    private int transitCount;
    private String mainTransitType; // 1: 지하철, 2: 버스, 3: 버스+지하철 순서 나열
    private List<Integer> pathTransitType = new ArrayList<>(); // (1: 지하철, 2: 버스, 3: 도보) 순서 나열
    private List<String> TransitTypeNo; // 지하철, 버스, 도보 번호 순서 나열
    // 구조화된 처리
    private List<RouteIdSetDTO> routeIds = new ArrayList<>(); // 여러 버스의 버스 경로 ID 리스트를 구조화된 형태로 저장
}
