// src/main/java/ru/practicum/analyzer/model/InteractionId.java
package ru.practicum.analyzer.model;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InteractionId implements Serializable {
    private Long userId;
    private Long eventId;
}