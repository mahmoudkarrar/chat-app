package com.chatapp;

import com.chatapp.model.Event;
import com.chatapp.model.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public class TestHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String CHAT_MSG_CONTENT = "Test Msg";
    public static final Event TEST_PAYLOAD = Event.builder().type(EventType.CHAT).content(CHAT_MSG_CONTENT)
            .sender("test").build();


    @SneakyThrows
    public static String eventToString(Event event) {
        return objectMapper.writeValueAsString(event);
    }

    @SneakyThrows
    public static Event stringToEvent(String eventString) {
        return objectMapper.readValue(eventString, Event.class);
    }
}
