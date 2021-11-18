package com.chatapp.controller;

import com.chatapp.model.Event;
import com.chatapp.service.HistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/")
public class ChatRestController {

    private final HistoryService historyService;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "chat/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Event> getHistory() {
        return historyService.getChatHistory()
                .asFlux()
                .take(Long.min(historyService.getHistoryLimit(), historyService.getTotalMessages().get()))
                .map(this::toEvent);
    }

    @SneakyThrows
    private Event toEvent(String message) {
        return objectMapper.readValue(message, Event.class);
    }
}
