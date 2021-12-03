package com.chatapp.handler;

import com.chatapp.model.Event;
import com.chatapp.model.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private final Sinks.Many<Event> chatHistory = Sinks.many().replay().limit(1000);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Void> handle(WebSocketSession session) {
        AtomicReference<Event> lastReceivedEvent = new AtomicReference<>();
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::toEvent)
                .doOnNext(event -> {
                    lastReceivedEvent.set(event);
                    chatHistory.tryEmitNext(event);
                })
                .doOnComplete(() -> {
                    if(lastReceivedEvent.get() != null) {
                        lastReceivedEvent.get().setType(EventType.LEAVE);
                        chatHistory.tryEmitNext(lastReceivedEvent.get());
                    }
                    log.info("Completed!");
                })
                .zipWith(session.send(chatHistory.asFlux()
                        .map(this::toString)
                        .map(session::textMessage)))
                .then();
    }

    @SneakyThrows
    private Event toEvent(String message) {
        return objectMapper.readValue(message, Event.class);
    }

    @SneakyThrows
    private String toString(Event event) {
        return objectMapper.writeValueAsString(event);
    }
}
