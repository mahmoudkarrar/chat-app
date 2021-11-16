package com.chatapp.client;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

public class Client {

    public static void main(String[] args) {

        WebSocketClient client = new ReactorNettyWebSocketClient();
<<<<<<< HEAD
        client.execute(
                        URI.create("ws://localhost:8080/chat"),
                        session -> session.send(
                                        Mono.just(session.textMessage("event-spring-reactive-client-websocket")))
                                .thenMany(session.receive()
                                        .map(WebSocketMessage::getPayloadAsText)
                                        .log())
                                .then())
=======
        client.execute(URI.create("ws://localhost:8080/chat"),
                session -> session.send(Mono.just(session.textMessage("event-spring-reactive-client-websocket")))
                        .thenMany(session.receive().map(WebSocketMessage::getPayloadAsText).log()).then())
>>>>>>> Feature-test-echo-message
                .block(Duration.ofSeconds(10L));
    }
}
