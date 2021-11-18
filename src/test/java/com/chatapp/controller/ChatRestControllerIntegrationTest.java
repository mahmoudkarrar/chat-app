package com.chatapp.controller;

import com.chatapp.ChatAppApplication;
import com.chatapp.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;

import static com.chatapp.TestHelper.*;
import static java.lang.String.format;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ChatAppApplication.class)
@Slf4j
class ChatRestControllerIntegrationTest {
    @LocalServerPort
    private String port;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("GET /api/v1/chat/history 200 return a live stream of event. for later joining clients it discard messages beyond the history limit")
    public void shouldReceiveRecentChatHistory() {
        WebSocketClient client = new ReactorNettyWebSocketClient();

        String msgToBeSent = eventToString(TEST_PAYLOAD);
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


        Flux<Event> events = webTestClient
                .get().uri("api/v1/chat/history")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Event.class)
                .getResponseBody();

        StepVerifier.create(events.map(Event::getContent))
                .expectNext(CHAT_MSG_CONTENT + 2)
                .expectNext(CHAT_MSG_CONTENT + 3)
                .expectNext(CHAT_MSG_CONTENT + 4)
                .verifyComplete();
    }
}