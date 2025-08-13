package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.collector.config.CollectorProperties;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

    private final Producer<String, UserActionAvro> producer;
    private final CollectorProperties props;

    public void processUserAction(UserActionProto request) {
        // маппинг Timestamp -> millis
        long tsMillis = toMillis(request.getTimestamp());

        // маппинг enum
        ActionTypeAvro actionTypeAvro = mapActionType(request.getActionType());

        // сборка Avro
        UserActionAvro avro = new UserActionAvro();
        avro.setUserId(request.getUserId());
        avro.setEventId(request.getEventId());
        avro.setActionType(actionTypeAvro);
        avro.setTimestamp(Instant.ofEpochMilli(tsMillis));

        // ключ
        String key = request.getUserId() + "_" + request.getEventId();

        ProducerRecord<String, UserActionAvro> record =
                new ProducerRecord<>(props.getUserActions(), key, avro);

        producer.send(record, (md, ex) -> {
            if (ex != null) {
                log.error("❌ Kafka send failed: topic={}, key={}", props.getUserActions(), key, ex);
            } else {
                log.debug("✅ Sent to Kafka: topic={}, partition={}, offset={}",
                        md.topic(), md.partition(), md.offset());
            }
        });
    }

    private long toMillis(com.google.protobuf.Timestamp ts) {
        if (ts == null) return System.currentTimeMillis();
        return ts.getSeconds() * 1000L + ts.getNanos() / 1_000_000L;
    }

    private ActionTypeAvro mapActionType(ActionTypeProto t) {
        if (t == null) return ActionTypeAvro.VIEW;
        return switch (t) {
            case ACTION_VIEW     -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE     -> ActionTypeAvro.LIKE;
            default -> ActionTypeAvro.VIEW;
        };
    }
}