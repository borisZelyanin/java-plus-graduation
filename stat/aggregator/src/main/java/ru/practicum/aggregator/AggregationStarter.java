package ru.practicum.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.config.AggregatorProperties;
import ru.practicum.aggregator.logic.AggregatorRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@RequiredArgsConstructor
@Component
@Slf4j
public class AggregationStarter {

    private final KafkaConsumer<Long, UserActionAvro> consumer;
    private final Producer<Long, EventSimilarityAvro> producer;
    private final AggregatorRepository aggregator; // твоя логика перерасчёта сходства
    private final AggregatorProperties props;
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private static final int COUNT_FIX_OFFSETS = 10;

    public void start() {
        validateProps();

        final String inputTopic = props.getUserActions();
        final String outputTopic = props.getEventsSimilarity();
        final Duration pollTimeout = Duration.ofMillis(
                props.getPollTimeoutMs() != null ? props.getPollTimeoutMs() : 5000
        );

        consumer.subscribe(List.of(inputTopic));
        log.info("📥 Подписка на топик: {}", inputTopic);

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            for (;;) {
                var records = consumer.poll(pollTimeout);
                if (records.isEmpty()) continue;

                records.forEach(record -> {
                    var event = record.value();
                    if (event == null) {
                        log.warn("⚠ Пропущено пустое сообщение (partition={}, offset={})",
                                record.partition(), record.offset());
                        return;
                    }

                    log.debug("➡ userId={}, eventId={}, action={}",
                            event.getUserId(), event.getEventId(), event.getActionType());

                    int count = 0;

                    for (EventSimilarityAvro sim : aggregator.updateEventSimilarity(event)) {
                        producer.send(
                                new ProducerRecord<>(outputTopic,null ,sim.getTimestamp().toEpochMilli(),sim.getEventA() , sim),
                                (md, ex) -> {
                                    if (ex != null) {
                                        log.error("❌ send similarity fail (key={}): {}",
                                                sim.getTimestamp().toEpochMilli(), ex.getMessage(), ex);
                                    } else {
                                        log.debug("✅ similarity sent: topic={}, partition={}, offset={}",
                                                md.topic(), md.partition(), md.offset());
                                    }
                                }
                        );
                        manageOffsets(record, count, consumer);
                    }
                });

//                // один общий коммит за пачку
//                consumer.commitAsync((offsets, ex) -> {
//                    if (ex != null) log.warn("⚠ commitAsync error: {}", ex.getMessage(), ex);
//                });
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

    private void manageOffsets(ConsumerRecord<Long, UserActionAvro> record, int count, Consumer<Long, UserActionAvro> consumer) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % COUNT_FIX_OFFSETS == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Error during offset fixing: {}", offsets, exception);
                }
            });
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
