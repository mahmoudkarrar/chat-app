package com.chatapp.handler;

import com.chatapp.ChatAppApplication;
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
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ChatAppApplication.class)
@Slf4j
class ChatWebSocketHandlerIntegrationTest {

    @LocalServerPort
    private String port;

    private final String TEST_PAYLOAD = "event-spring-reactive-client-websocket";

    @Test
    void shouldHandleRequest() {
        WebSocketClient client = new ReactorNettyWebSocketClient();

        Sinks.One<String> sink = Sinks.one();

        client.execute(URI.create(format("ws://localhost:%s/chat", port)),
                session -> session.send(Mono.just(session.textMessage(TEST_PAYLOAD))).concatWith(
                        session.receive().map(WebSocketMessage::getPayloadAsText).doOnNext(sink::tryEmitValue).then())
                        .then())
                .subscribe();

        String expected = " " + TEST_PAYLOAD;
        StepVerifier.create(sink.asMono()).thenConsumeWhile(value -> value.equals(expected)).verifyComplete();

    }
}