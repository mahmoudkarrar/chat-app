package com.chatapp.controller;

import com.chatapp.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
@AllArgsConstructor
public class ChatRestController {

    private final Sinks.Many<String> chatHistory;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "chat/history", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> getChatHistory() {
        return chatHistory.asFlux()
                .map(this::toEvent);
    }

    @SneakyThrows
    private Event toEvent(String message) {
        return objectMapper.readValue(message, Event.class);
    }
}
