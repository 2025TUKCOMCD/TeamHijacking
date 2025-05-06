package com.example.back.controller;

import com.example.back.service.RealtimeWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RealtimeController implements WebSocketConfigurer {

    private final RealtimeWebSocketService realtimeWebSocketService;

    // RealtimeWebSocketHandler를 주입받는 생성자
    @Autowired
    public RealtimeController(RealtimeWebSocketService realtimeWebSocketService) {
        this.realtimeWebSocketService = realtimeWebSocketService;
    }

    // WebSocketConfigurer 인터페이스의 메서드 구현
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeWebSocketService, "/realtime")
                .setAllowedOrigins("*");
    }
}
