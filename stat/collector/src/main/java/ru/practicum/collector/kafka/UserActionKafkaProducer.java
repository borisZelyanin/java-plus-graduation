package ru.practicum.collector.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.practicum.collector.config.CollectorProperties;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Отправляет пользовательские действия в Kafka (Avro).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionKafkaProducer {

    /** Типизированный шаблон: ключ Long, значение — конкретный Avro (UserActionAvro). */
    private final KafkaTemplate<Long, SpecificRecordBase> kafkaTemplate;
    private final CollectorProperties props;

    /**
     * Асинхронно отправляет событие в топик {@code collector.user-actions}.
     * Ключом выступает {@code eventId}, таймстемп берётся из сообщения (если есть) — для сортировки на брокере.
     */
    public CompletableFuture<SendResult<Long, SpecificRecordBase>> send(UserActionAvro payload) {
        final String topic = props.getTopicUserActions();
        final Long key = payload.getEventId(); // партиционирование по событию
        final Long tsMillis = extractTimestampMillis(payload);

        // построим запись и добавим несколько полезных заголовков для дебага
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(topic, null, tsMillis, key, payload);
        record.headers()
                .add(new RecordHeader("x-user-id", String.valueOf(payload.getUserId()).getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("x-event-id", String.valueOf(payload.getEventId()).getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("x-action-type", String.valueOf(payload.getActionType()).getBytes(StandardCharsets.UTF_8)));

        log.debug("➡️  Kafka send: topic={}, key={}, ts={}, payload={}",
                topic, key, tsMillis, shortPayload(payload));

        CompletableFuture<SendResult<Long, SpecificRecordBase>> future = kafkaTemplate.send(record);

        future.whenComplete((res, ex) -> {
            if (ex != null) {
                log.error("❌ Kafka send FAILED: topic={}, key={}, reason={}", topic, key, ex.toString(), ex);
            } else if (res != null && res.getRecordMetadata() != null) {
                var md = res.getRecordMetadata();
                log.info("✅ Kafka send OK: topic={} partition={} offset={} key={}",
                        md.topic(), md.partition(), md.offset(), key);
            } else {
                log.warn("⚠️ Kafka send completed without metadata: topic={}, key={}", topic, key);
            }
        });

        return future;
    }

    /**
     * Осторожно извлекаем миллисекунды из Avro-поля timestamp.
     * В зависимости от генерации Avro это поле может быть Long (logicalType=timestamp_millis)
     * или java.time.Instant. Поддержим оба варианта.
     */
    private Long extractTimestampMillis(UserActionAvro msg) {
        try {
            Object ts = msg.get("timestamp"); // generic доступ к полю
            if (ts == null) return System.currentTimeMillis();

            if (ts instanceof Long l) {
                return l;
            }
            if (ts instanceof Instant inst) {
                return inst.toEpochMilli();
            }
        } catch (Exception ignore) {
            // упадём в дефолт
        }
        return System.currentTimeMillis();
    }

    /**
     * Короткое представление для логов (без спама всем объектом).
     */
    private String shortPayload(UserActionAvro p) {
        return "UserActionAvro{userId=%d,eventId=%d,action=%s}".formatted(
                p.getUserId(), p.getEventId(), p.getActionType()
        );
    }

    @PreDestroy
    public void close() {
        try {
            log.info("🛑 Shutting down KafkaTemplate");
            kafkaTemplate.flush();
        } catch (Exception e) {
            log.warn("⚠️ Error on KafkaTemplate flush: {}", e.toString(), e);
        }
    }
}