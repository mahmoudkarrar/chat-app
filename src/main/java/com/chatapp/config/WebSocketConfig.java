package com.chatapp.config;

import com.chatapp.handler.ChatWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@Configuration
public class WebSocketConfig {
    @Bean
    public HandlerMapping webSocketMapping(ChatWebSocketHandler webSocketHandler) {
        return new SimpleUrlHandlerMapping(Map.of("/chat", webSocketHandler), -1);
    }

}
