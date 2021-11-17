package com.chatapp.model;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Event {
    private EventType type;
    private String content;
    private String sender;
}
