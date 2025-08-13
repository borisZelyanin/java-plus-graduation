package ru.practicum.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.config.AggregatorProperties;
import ru.practicum.aggregator.logic.SimilarityAggregator;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
public class AggregationStarter {

    private final Consumer<String, UserActionAvro> consumer;
    private final Producer<String, EventSimilarityAvro> producer;
    private final SimilarityAggregator aggregator; // твоя логика перерасчёта сходства
    private final AggregatorProperties props;

    public void start() {
        validateProps();

        final String inputTopic = props.getUserActions();
        final String outputTopic = props.getEventsSimilarity();
        final Duration pollTimeout = Duration.ofMillis(
                props.getPollTimeoutMs() != null ? props.getPollTimeoutMs() : 2000
        );

        consumer.subscribe(List.of(inputTopic));
        log.info("📥 Подписка на топик: {}", inputTopic);

        try {
            for(;;) {
                var records = consumer.poll(pollTimeout);

                if (records.isEmpty()) {
                    continue;
                }
                records.forEach(record -> {
                    UserActionAvro event = record.value();
                    if (event == null) {
                        log.warn("⚠ Пропущено пустое сообщение (partition={}, offset={})",
                                record.partition(), record.offset());
                        return;
                    }

                    log.debug("➡ Получено событие userId={}, eventId={}, action={}",
                            event.getUserId(), event.getEventId(), event.getActionType());

                    // агрегатор может вернуть 0..N пересчитанных коэффициентов
                    aggregator.updateState(event).ifPresent(similarities -> {
                        for (EventSimilarityAvro sim : similarities) {
                            // ключом делаем упорядоченную пару, чтобы все записи пары шли в один partition (по желанию)
                            String key = sim.getEventA() + "_" + sim.getEventB();

                            ProducerRecord<String, EventSimilarityAvro> recordOut =
                                    new ProducerRecord<>(outputTopic, key, sim);

                            producer.send(recordOut, (metadata, ex) -> {
                                if (ex != null) {
                                    log.error("❌ Ошибка при отправке similarity ({}): {}",
                                            key, ex.getMessage(), ex);
                                } else {
                                    log.debug("✅ similarity отправлен: key={}, topic={}, partition={}, offset={}",
                                            key, metadata.topic(), metadata.partition(), metadata.offset());
                                }
                            });
                        }
                    });
                });

                consumer.commitAsync((offsets, ex) -> {
                    if (ex != null) {
                        log.warn("⚠ Ошибка commitAsync: {}", ex.getMessage(), ex);
                    }
                });
            }

        } catch (WakeupException ignored) {
            log.info("⛔ Получен сигнал завершения (Wakeup)");
        } catch (Exception e) {
            log.error("💥 Ошибка в процессе агрегации", e);
        } finally {
            try {
                consumer.commitSync();
            } catch (Exception e) {
                log.warn("⚠ Ошибка commitSync при завершении: {}", e.getMessage(), e);
            }
            // закрываем consumer/producer аккуратно
            safeCloseConsumer();
            safeCloseProducer();
        }
    }

    private void validateProps() {
        if (isBlank(props.getEventsSimilarity()) || isBlank(props.getUserActions())) {
            throw new IllegalStateException("Kafka topics не сконфигурированы: inputTopic=" +
                    props.getUserActions() + ", outputTopic=" + props.getEventsSimilarity());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void safeCloseConsumer() {
        try {
            consumer.close();
            log.info("📴 Consumer закрыт");
        } catch (Exception e) {
            log.warn("⚠ Ошибка при закрытии consumer: {}", e.getMessage(), e);
        }
    }

    private void safeCloseProducer() {
        try {
            producer.flush();
        } catch (Exception e) {
            log.warn("⚠ Ошибка при flush producer: {}", e.getMessage(), e);
        }
        try {
            producer.close();
            log.info("📤 Producer закрыт");
        } catch (Exception e) {
            log.warn("⚠ Ошибка при закрытии producer: {}", e.getMessage(), e);
        }
    }
}