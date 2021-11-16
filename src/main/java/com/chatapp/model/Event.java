package com.chatapp.model;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private EventType type;
    private String content;
    private String sender;
}
