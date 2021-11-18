package com.chatapp.handler;

import com.chatapp.service.HistoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@AllArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final IntegrationFlowContext integrationFlowContext;

    private final FluxMessageChannel fluxMessageChannel;
    private final HistoryService historyService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<Message<String>> input = session.receive()
                .map(msg -> {
                    historyService.emitMessageToHistory(msg.getPayloadAsText());
                    return MessageBuilder.withPayload(msg.getPayloadAsText()).setHeader("webSocketSession", session)
                            .build();
                });

        Publisher<Message<WebSocketMessage>> messagePublisher = IntegrationFlows.from(input)
                .channel(fluxMessageChannel)
                .<String> handle((p, h) -> ((WebSocketSession) h.get("webSocketSession")).textMessage(p))
                .toReactivePublisher();

        IntegrationFlowContext.IntegrationFlowRegistration flowRegistration = this.integrationFlowContext
                .registration((IntegrationFlow) messagePublisher).register();

        Flux<WebSocketMessage> output = Flux.from(messagePublisher).map(Message::getPayload);

        return session.send(output.doFinally(s -> flowRegistration.destroy()));
    }
}
