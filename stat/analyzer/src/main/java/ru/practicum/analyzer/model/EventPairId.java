package ru.practicum.analyzer.model;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EventPairId implements Serializable {
    private Long eventA;
    private Long eventB;
}