package com.chatapp.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ChatWebSocketHandler implements WebSocketHandler {
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<WebSocketMessage> output = session.receive()
<<<<<<< HEAD
                .map(value -> session.textMessage("Echo " + value));
=======
                .map(value -> session.textMessage("Echo " + value.getPayloadAsText()));
>>>>>>> Feature-test-echo-message
        return session.send(output);
    }
}
