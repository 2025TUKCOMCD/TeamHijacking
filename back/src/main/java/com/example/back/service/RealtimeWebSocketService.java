package com.example.back.service;

import com.example.back.dto.RealTimeResultDTO;
import com.example.back.dto.realtime.RealtimeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;

@Service
public class RealtimeWebSocketService implements WebSocketHandler {

    @Autowired
    private final MessageService messageService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

    public RealtimeWebSocketService(MessageService messageService) {
        this.messageService = messageService;
    }

    // 연결 설정
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket 연결이 수립되었습니다: " + session.getId());
    }

    // 메시지 수신 처리
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        RealTimeResultDTO realTimeResultDTO;
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();

            try {
                // JSON 데이터를 DTO로 변환
                RealtimeDTO data = objectMapper.readValue(payload, RealtimeDTO.class);

//                // 서비스 호출로 비즈니스 로직 처리
//                switch (data.getType()) {
//                    case 1:
//                        // 지하철 데이터 처리
//                        // 탑승 전인 경우
//                        if(data.isBoarding()) realTimeResultDTO = messageService.processSubwayBoardingData(data);
//                        else {
//                            // 탑승 중인 경우
//                            realTimeResultDTO =messageService.processSubwayAlightingData(data);
//                        }
//                    case 2:
//                        // 버스 데이터 처리
//                        if (data.isBoarding()) {
//                            // 탑승 전인 경우
//                            realTimeResultDTO = messageService.processBusBoardingData(data);
//                        } else {
//                            // 탑승 중인 경우
//                            realTimeResultDTO = messageService.processBusAlightingData(data);
//                        }
//                    case 3:
//                        // 도보 데이터 처리
//                        realTimeResultDTO = messageService.processWalkingData(data);
//                    default:
//                        realTimeResultDTO = messageService.processDefaultData(data);
//                }
//
//                // 처리 결과를 클라이언트로 전송
//                session.sendMessage(new TextMessage(realTimeResultDTO.toString()));

            } catch (Exception e) {
                // 오류 처리
                session.sendMessage(new TextMessage("Invalid JSON format: " + e.getMessage()));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket 오류 발생: " + session.getId() + ", 오류: " + exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("WebSocket 연결이 종료되었습니다: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}