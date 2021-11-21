package com.chatapp.config;

import com.chatapp.handler.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@Configuration
@Slf4j
public class WebSocketServerConfig {
    @Bean
    public HandlerMapping webSocketMapping(ChatWebSocketHandler webSocketHandler) {
        return new SimpleUrlHandlerMapping(Map.of("/chat", webSocketHandler), -1);
    }

    @Bean
    public FluxMessageChannel fluxMessageChannel() {
        return new FluxMessageChannel();
    }

    @Bean
    public PublishSubscribeChannel errorChannel() {
        PublishSubscribeChannel channel = new PublishSubscribeChannel(true);
        channel.setBeanName("errorChannel");
        channel.subscribe(message -> log.error("ERROR detected {}",message));
        return channel;
    }
}
