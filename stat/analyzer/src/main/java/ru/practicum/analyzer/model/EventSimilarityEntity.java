package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "event_similarity")
@IdClass(EventPairId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventSimilarityEntity {
    @Id
    private Long eventA; // МЕНЬШИЙ id

    @Id
    private Long eventB; // БОЛЬШИЙ id

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private Instant updatedAt;
}
