// src/main/java/ru/practicum/analyzer/model/InteractionEntity.java
package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "interactions")
@IdClass(InteractionId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InteractionEntity {
    @Id
    private Long userId;

    @Id
    private Long eventId;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private Instant updatedAt;
}

