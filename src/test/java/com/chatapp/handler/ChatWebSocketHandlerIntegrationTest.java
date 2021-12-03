package com.chatapp.handler;

import com.chatapp.ChatAppApplication;
import com.chatapp.TestHelper;
import com.chatapp.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
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

import static com.chatapp.TestHelper.*;
import static java.lang.String.format;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ChatAppApplication.class)
@Slf4j
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class ChatWebSocketHandlerIntegrationTest {

    @LocalServerPort
    private String port;

    @Test
    @DisplayName("test - send 1 message to the server /chat endpoint. Server should broadcast the message to the registered clients")
    void shouldEchoTheMessageToTheClient() {
        WebSocketClient client = new ReactorNettyWebSocketClient();

        Sinks.One<String> sink = Sinks.one();

        String chatMsg = eventToString(TEST_PAYLOAD);
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

    @Test
    void shouldMultiplexSendAndReceive() {
        WebSocketClient client = new ReactorNettyWebSocketClient();

        Sinks.Many<String> input = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.Many<String> output = Sinks.many().unicast().onBackpressureBuffer();

        client.execute(URI.create(format("ws://localhost:%s/chat", port)), session -> {
                var sender = session.send(input.asFlux()
                        .map(session::textMessage));

                var receiver = session.receive()
                        .take(2)
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(output::tryEmitNext)
                    .doOnComplete(output::tryEmitComplete)
                    .then();

                 return sender.zipWith(receiver).then();
        }).subscribe();

        StepVerifier.create(output.asFlux())
                .then(() -> input.tryEmitNext("Hello"))
                .expectNext("Hello")
                .then(() -> input.tryEmitNext("World"))
                .expectNext("World")
                .then(input::tryEmitComplete)
                .expectComplete()
                .verify(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("test - send 4 messages to the server /chat endpoint. Server should broadcast the message to the registered clients")
    void shouldBroadcastAllSentMessagesToTheClient() {
        //clients
        WebSocketClient client = new ReactorNettyWebSocketClient();

        String msgToBeSent = eventToString(TEST_PAYLOAD);
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        int count = 4;
        Flux<String> input = Flux.range(1, count).map(index -> msgToBeSent.replace(CHAT_MSG_CONTENT, CHAT_MSG_CONTENT + index));

        client.execute(URI.create(format("ws://localhost:%s/chat", port)),
                        session -> session.send(input.map(session::textMessage))
                                .thenMany(
                                        session.receive().take(count).map(WebSocketMessage::getPayloadAsText)
                                                .doOnNext(sink::tryEmitNext)
                                                .then())
                                .then())
                .block(Duration.ofSeconds(1));


        StepVerifier.create(sink.asFlux().take(count).map(TestHelper::stringToEvent).map(Event::getContent))
                .expectNext(CHAT_MSG_CONTENT + 1)
                .expectNext(CHAT_MSG_CONTENT + 2)
                .expectNext(CHAT_MSG_CONTENT + 3)
                .expectNext(CHAT_MSG_CONTENT + 4)
                .verifyComplete();
    }

}