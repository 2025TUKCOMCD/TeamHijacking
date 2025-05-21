package com.example.back.dto;

import lombok.Data;

@Data
public class RealTimeResultDTO {
    private int trainNo; // 기차 번호
    private int nextRequest; // 1: 현재 대중교통 2: 다음 대중교통
    private String predictTimes1;
    private String predictTimes2;
}
