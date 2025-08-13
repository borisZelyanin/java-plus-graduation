// src/main/java/ru/practicum/analyzer/ingest/UserActionsIngestor.java
package ru.practicum.analyzer.processor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.config.AnalyzerProperties;
import ru.practicum.analyzer.model.InteractionEntity;
import ru.practicum.analyzer.repository.InteractionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.*;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionsIngestor {
    private final Consumer<String, UserActionAvro> consumer;
    private final InteractionRepository interactions;
    private final AnalyzerProperties props;

    @PostConstruct
    public void start() {
        consumer.subscribe(List.of(props.getKafka().getTopicUserActions()));
        Thread t = new Thread(this::loop, "user-actions-ingestor");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        try {
            while (true) {
                ConsumerRecords<String, UserActionAvro> recs = consumer.poll(Duration.ofMillis(1000));
                if (recs.isEmpty()) continue;

                recs.forEach(r -> upsertInteraction(r.value()));
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("UserActionsIngestor error", e);
        } finally {
            consumer.close();
        }
    }

    @Transactional
    void upsertInteraction(UserActionAvro msg) {
        long userId = msg.getUserId();
        long eventId = msg.getEventId();
        double weight = switch (msg.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
        InteractionEntity entity = interactions.findById(new ru.practicum.analyzer.model.InteractionId(userId, eventId))
                .orElse(InteractionEntity.builder()
                        .userId(userId)
                        .eventId(eventId)
                        .weight(0d)
                        .updatedAt(Instant.EPOCH)
                        .build());
        if (weight > entity.getWeight()) {
            entity.setWeight(weight);
            entity.setUpdatedAt(msg.getTimestamp());
            interactions.save(entity);
        }
    }
}