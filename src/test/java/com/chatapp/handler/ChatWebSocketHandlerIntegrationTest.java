package com.chatapp.handler;

import com.chatapp.ChatAppApplication;
import com.chatapp.model.Event;
import com.chatapp.model.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;

import static java.lang.String.format;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ChatAppApplication.class)
@Slf4j
class ChatWebSocketHandlerIntegrationTest {

    @LocalServerPort
    private String port;

    private final String CHAT_MSG_CONTENT = "Test Msg";
    private final Event TEST_PAYLOAD = Event.builder().type(EventType.CHAT).content(CHAT_MSG_CONTENT)
            .sender("test").build();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldHandleRequest() {
        WebSocketClient client = new ReactorNettyWebSocketClient();

        Sinks.One<String> sink = Sinks.one();

        String chatMsg = toString(TEST_PAYLOAD);
        client.execute(URI.create(format("ws://localhost:%s/chat", port)),
                session -> session.send(Mono.just(session.textMessage(chatMsg)))
                        .concatWith(
                        session.receive().map(WebSocketMessage::getPayloadAsText).doOnNext(sink::tryEmitValue)
                                .then())
                        .then())
                .subscribe();

        StepVerifier.create(sink.asMono())
                .thenConsumeWhile(value -> value.equals(chatMsg))
                .verifyComplete();

    }

    @SneakyThrows
    private String toString(Event event) {
        return objectMapper.writeValueAsString(event);
    }

    @SneakyThrows
    private Event toEvent(String eventString) {
        return objectMapper.readValue(eventString, Event.class);
    }
    @Test
    void shouldBroadcastAllMessagesToTheClient() {
        //clients
        WebSocketClient client = new ReactorNettyWebSocketClient();

        String msgToBeSent = toString(TEST_PAYLOAD);
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        int count = 4;
        Flux<String> input = Flux.range(1, count).map(index -> msgToBeSent.replace(CHAT_MSG_CONTENT, CHAT_MSG_CONTENT+ index));

        client.execute(URI.create(format("ws://localhost:%s/chat", port)),
                        session -> session.send(input.map(session::textMessage))
                                .thenMany(
                                        session.receive().take(count).map(WebSocketMessage::getPayloadAsText)
                                                .doOnNext(sink::tryEmitNext)
                                                .then())
                                .then())
                .block(Duration.ofSeconds(1));


        StepVerifier.create(sink.asFlux().take(count).map(this::toEvent).map(Event::getContent))
                .expectNext(CHAT_MSG_CONTENT + 1)
                .expectNext(CHAT_MSG_CONTENT + 2)
                .expectNext(CHAT_MSG_CONTENT + 3)
                .expectNext(CHAT_MSG_CONTENT + 4)
                .verifyComplete();


    }

}