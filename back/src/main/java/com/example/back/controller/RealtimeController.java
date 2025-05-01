package com.example.back.controller;

import com.example.back.config.RealtimeWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RealtimeController implements WebSocketConfigurer {

    private final RealtimeWebSocketHandler realtimeWebSocketHandler;

    @Autowired
    public RealtimeController(RealtimeWebSocketHandler realtimeWebSocketHandler) {
        this.realtimeWebSocketHandler = realtimeWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeWebSocketHandler, "/realtime")
                .setAllowedOrigins("*");
    }
}
