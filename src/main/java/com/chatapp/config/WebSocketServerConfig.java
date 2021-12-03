package com.chatapp.config;

import com.chatapp.handler.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import reactor.core.publisher.Hooks;

import java.util.Map;

@Configuration
@Slf4j
public class WebSocketServerConfig {
    @Bean
    public HandlerMapping webSocketMapping(ChatWebSocketHandler webSocketHandler) {
        return new SimpleUrlHandlerMapping(Map.of("/chat", webSocketHandler), -1);
    }

    @Bean
    public void errorDroppedHandler() {
        Hooks.onErrorDropped(error -> log.warn("Exception happened: ", error));
    }
}
