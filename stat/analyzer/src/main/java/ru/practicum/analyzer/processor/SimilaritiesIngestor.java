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
import ru.practicum.analyzer.model.EventPairId;
import ru.practicum.analyzer.model.EventSimilarityEntity;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilaritiesIngestor {
    private final Consumer<String, EventSimilarityAvro> consumer;
    private final EventSimilarityRepository repo;
    private final AnalyzerProperties props;

    @PostConstruct
    public void start() {
        consumer.subscribe(List.of(props.getKafka().getTopicSimilarities()));
        Thread t = new Thread(this::loop, "similarities-ingestor");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        try {
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> recs = consumer.poll(Duration.ofMillis(1000));
                if (recs.isEmpty()) continue;

                recs.forEach(r -> upsertSimilarity(r.value()));
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("SimilaritiesIngestor error", e);
        } finally {
            consumer.close();
        }
    }

    @Transactional
    void upsertSimilarity(EventSimilarityAvro msg) {
        long a = Math.min(msg.getEventA(), msg.getEventB());
        long b = Math.max(msg.getEventA(), msg.getEventB());
        EventSimilarityEntity e = repo.findById(new EventPairId(a, b))
                .orElse(EventSimilarityEntity.builder()
                        .eventA(a).eventB(b)
                        .score(0d)
                        .updatedAt(msg.getTimestamp())
                        .build());
        e.setScore(msg.getScore());
        e.setUpdatedAt(msg.getTimestamp());
        repo.save(e);
    }
}