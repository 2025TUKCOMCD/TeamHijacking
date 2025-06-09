package com.example.back.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class RealTimeResultDTO {
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private String trainNo; // 기차 번호
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private String vehId;
    private int nextRequest; // 1: 현재 대중교통 2: 다음 대중교통
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private String location; // 현재 위치

    private String predictTimes1;
    private String predictTimes2;
}
