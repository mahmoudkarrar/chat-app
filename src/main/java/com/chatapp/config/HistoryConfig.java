package com.chatapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class HistoryConfig {
    @Bean
    public Sinks.Many<String> chatHistory(@Value("${chat.history.capacity:10}") Integer historySize){
        return Sinks.many().replay().limit(historySize);
    }
}
