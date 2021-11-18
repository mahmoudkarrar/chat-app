package com.chatapp.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicLong;

@Service
@AllArgsConstructor
@Getter
public class HistoryService {
    private final Sinks.Many<String> chatHistory;
    private final AtomicLong totalMessages;
    private final Long historyLimit;

    public void emitMessageToHistory(String message) {
        chatHistory.tryEmitNext(message);
        if( totalMessages.get() < historyLimit) {
            totalMessages.incrementAndGet();
        }
    }
}
