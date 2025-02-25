package com.example.back.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RouteIdSetDTO {
    private List<Integer> busLocalBlID = new ArrayList<>(); // 버스 로컬 블 ID 리스트
    private int startStationInfo ;
    private int endStationInfo;
    private List<Integer> stationInfo = new ArrayList<>(); // 정류장 정보 리스트
    private List<String> predictTimes1 = new ArrayList<>(); // 여러 버스의 predictTime1 리스트
    private List<String> predictTimes2 = new ArrayList<>(); // 여러 버스의 predictTime2 리스트
}
