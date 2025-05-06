package com.example.back.config;

import com.example.back.dto.realtime.RealtimeDTO;
import com.example.back.service.MessageReceiverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;

@Service
public class RealtimeWebSocketHandler implements WebSocketHandler {

    private final MessageReceiverService messageReceiverService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

    public RealtimeWebSocketHandler(MessageReceiverService messageReceiverService) {
        this.messageReceiverService = messageReceiverService;

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket 연결이 수립되었습니다: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();

            try {
                // JSON 데이터를 DTO로 변환
                RealtimeDTO data = objectMapper.readValue(payload, RealtimeDTO.class);
                // 서비스 호출로 비즈니스 로직 처리
                String processedData = messageReceiverService.processData(data);

                // 처리 결과를 클라이언트로 전송
                session.sendMessage(new TextMessage(processedData));

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