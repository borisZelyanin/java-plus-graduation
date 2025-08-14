package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.collector.kafka.UserActionKafkaProducer;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

    private final UserActionKafkaProducer kafkaProducer;

    public void processUserAction(UserActionProto request) {

        // маппинг enum
        ActionTypeAvro actionTypeAvro = mapActionType(request.getActionType());

        // сборка Avro
        UserActionAvro avro = new UserActionAvro();
        avro.setUserId(request.getUserId());
        avro.setEventId(request.getEventId());
        avro.setActionType(actionTypeAvro);
        avro.setTimestamp(Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos()));

        kafkaProducer.send(avro);

    }

    private ActionTypeAvro mapActionType(ActionTypeProto t) {
        if (t == null) return ActionTypeAvro.VIEW;
        return switch (t) {
            case ACTION_VIEW     -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE     -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Invalid action type: " + t);
        };
    }
}