package ru.practicum.aggregator.logic;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

@Component
public class SimilarityAggregator {

    /** eventId -> (userId -> maxWeight) */
    private final Map<Long, Map<Long, Double>> userActionsWeight = new HashMap<>();

    /** eventId -> sum of user weights for this event */
    private final Map<Long, Double> eventsWeightsSum = new HashMap<>();

    /** firstEventId -> (secondEventId -> sum of min weights), where first < second */
    private final Map<Long, Map<Long, Double>> minWeightsSum = new HashMap<>();

    private static final double VIEW_WEIGHT     = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT     = 1.0;

    /**
     * Инкрементально пересчитывает коэффициенты сходства для пар событий,
     * затронутых действием пользователя.
     */
    public Optional<List<EventSimilarityAvro>> updateEventSimilarity(UserActionAvro userAction) {
        if (userAction == null) return Optional.empty();

        final long userId  = userAction.getUserId();
        final long eventId = userAction.getEventId();
        final Instant ts   = userAction.getTimestamp() != null ? userAction.getTimestamp() : Instant.now();

        // старый/новый максимальный вес взаимодействия пользователя с eventId
        Map<Long, Double> usersWeightsForEvent =
                userActionsWeight.computeIfAbsent(eventId, k -> new HashMap<>());

        final double oldWeight = usersWeightsForEvent.getOrDefault(userId, 0.0);
        final double newWeight = weightOf(userAction.getActionType());

        // если новый вес не улучшает максимум — ничего не делаем
        if (oldWeight >= newWeight) return Optional.empty();

        // 1) обновить максимум веса для пользователя по этому событию
        usersWeightsForEvent.merge(userId, newWeight, Math::max);

        // 2) обновить суммарный вес по событию
        final double newSum = eventsWeightsSum.getOrDefault(eventId, 0.0) - oldWeight + newWeight;
        eventsWeightsSum.put(eventId, newSum);

        // 3) обновить суммы min-весов и пересчитать сходство с другими событиями,
        //    с которыми этот же пользователь уже взаимодействовал
        List<EventSimilarityAvro> out = new ArrayList<>();

        for (long otherEventId : userActionsWeight.keySet()) {
            if (otherEventId == eventId) continue;

            Map<Long, Double> otherEventUsers = userActionsWeight.get(otherEventId);
            if (otherEventUsers == null || !otherEventUsers.containsKey(userId)) continue;

            // обновить сумму минимальных весов для пары (eventId, otherEventId)
            double newSumMinPairWeight =
                    updateMinWeightSum(eventId, otherEventId, userId, oldWeight, newWeight);

            // пересчитать сходство
            double similarity = calcSimilarity(eventId, otherEventId, newSumMinPairWeight);

            out.add(buildSimilarity(eventId, otherEventId, similarity, ts));
        }

        return out.isEmpty() ? Optional.empty() : Optional.of(out);
    }

    /** Вычисление коэффициента сходства на основе суммы минимальных весов. */
    private double calcSimilarity(long eventId, long otherEventId, double newSumMinPairWeight) {
        if (newSumMinPairWeight == 0) return 0.0;

        double sumEventWeight      = eventsWeightsSum.getOrDefault(eventId, 0.0);
        double sumOtherEventWeight = eventsWeightsSum.getOrDefault(otherEventId, 0.0);

        // формула как в исходной логике: делим на произведение корней сумм
        // (если нужна другая нормировка — меняется только эта строка)
        return newSumMinPairWeight / (Math.sqrt(sumEventWeight) * Math.sqrt(sumOtherEventWeight));
    }

    /**
     * Обновляет сумму минимальных весов для пары мероприятий (порядок фиксируем first<second).
     * Возвращает новое значение суммы для пары.
     */
    private double updateMinWeightSum(long eventId, long otherEventId, long userId,
                                      double oldWeight, double newWeight) {

        double otherOldWeight = userActionsWeight.get(otherEventId).get(userId);

        double oldMin = Math.min(oldWeight,      otherOldWeight);
        double newMin = Math.min(newWeight,      otherOldWeight);

        long first  = Math.min(eventId, otherEventId);
        long second = Math.max(eventId, otherEventId);

        double oldSumForPair = minWeightsSum
                .computeIfAbsent(first, k -> new HashMap<>())
                .getOrDefault(second, 0.0);

        // если минимум не поменялся — сумма остаётся прежней
        if (Double.compare(oldMin, newMin) == 0) return oldSumForPair;

        double updatedSumForPair = oldSumForPair - oldMin + newMin;
        minWeightsSum.get(first).put(second, updatedSumForPair);

        return updatedSumForPair;
    }

    /** Построение сообщения о сходстве событий. */
    private EventSimilarityAvro buildSimilarity(long a, long b, double score, Instant ts) {
        long first  = Math.min(a, b);
        long second = Math.max(a, b);
        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(score)
                .setTimestamp(ts)
                .build();
    }

    /** Маппинг действия пользователя в вес. */
    private double weightOf(ActionTypeAvro actionType) {
        if (actionType == null) return 0.0;
        return switch (actionType) {
            case VIEW     -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE     -> LIKE_WEIGHT;
        };
    }
}