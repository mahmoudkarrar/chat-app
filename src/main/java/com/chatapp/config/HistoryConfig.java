package com.chatapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class HistoryConfig {

    @Bean
    public Long historyLimit(@Value("${chat.history.capacity:10}") Long limit) {
        return limit;
    }
    @Bean
    public Sinks.Many<String> chatHistory(@Value("${chat.history.capacity:10}") Integer historySize){
        return Sinks.many().replay().limit(historySize);
    }

    @Bean
    public AtomicLong totalMessages(){
        return new AtomicLong(0);
    }
}
