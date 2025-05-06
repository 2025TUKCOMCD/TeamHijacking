package com.example.back.service;

import com.example.back.dto.realtime.RealtimeDTO;
import org.springframework.stereotype.Service;

@Service
public class MessageReceiverService {
    public String processData(RealtimeDTO data) {
        System.out.println("서비스에서 데이터 처리 중...");
        System.out.println("타입: " + data.getType());
        System.out.println("탑승 여부: " + data.isBoarding());
        System.out.println("대중교통 ID: " + data.getId());
        System.out.println("역 ID: " + data.getStationId());

        // 지하철 데이터만 처리
        if (data.getStationName() != null) {
            System.out.println("지하철 역 이름: " + data.getStationName());
        }
        if (data.getDirection() != null) {
            System.out.println("지하철 방향: " + data.getDirection());
        }

        // 처리 완료된 데이터 응답 생성
        return "Processed transportation data for type: " + data.getType();
    }
}
