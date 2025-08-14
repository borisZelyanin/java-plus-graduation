package ru.practicum.aggregator.logic;

import org.springframework.stereotype.Repository;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SimilarityAggregator {

    /** eventId -> (userId -> maxWeight) */
    private final Map<Long, Map<Long, Double>> weightsByEventUser = new ConcurrentHashMap<>();
    /** userId  -> (eventId -> maxWeight) */
    private final Map<Long, Map<Long, Double>> weightsByUserEvent = new ConcurrentHashMap<>();
    /** eventId -> S_A */
    private final Map<Long, Double> sumWeightsByEvent = new ConcurrentHashMap<>();
    /** firstEventId -> (secondEventId -> S_min), где first < second */
    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

    // Константы весов (можно вынести в application.yml, если захочешь конфигурировать)
    private static final double VIEW_WEIGHT = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT = 1.0;

    /** Инкрементальный апдейт похожести пар после действия пользователя */
    public List<EventSimilarityAvro> updateEventSimilarity(UserActionAvro action) {
        if (action == null) {
            return List.of();
        }

        final long u = action.getUserId();
        final long A = action.getEventId();
        final long tsMillis = action.getTimestamp() != null
                ? action.getTimestamp().toEpochMilli()
                : System.currentTimeMillis();

        final double wNew = getUserActionWeight(action.getActionType());
        if (wNew <= 0.0) {
            return List.of();
        }

        Map<Long, Double> byUser =
                weightsByUserEvent.computeIfAbsent(u, __ -> new ConcurrentHashMap<>());
        double wOld = byUser.getOrDefault(A, 0.0);

        if (wNew <= wOld) {
            return List.of();
        }

        // 1) обновляем веса
        byUser.put(A, wNew);
        weightsByEventUser
                .computeIfAbsent(A, __ -> new ConcurrentHashMap<>())
                .put(u, wNew);

        // 2) обновляем сумму весов по событию
        double deltaWA = wNew - wOld;
        sumWeightsByEvent.merge(A, deltaWA, Double::sum);

        // 3) обновляем S_min и считаем score для всех других событий пользователя
        List<EventSimilarityAvro> out = new ArrayList<>();
        for (Map.Entry<Long, Double> e : byUser.entrySet()) {
            long B = e.getKey();
            if (B == A) continue;

            double w_u_B = e.getValue();
            double oldMin = Math.min(wOld, w_u_B);
            double newMin = Math.min(wNew, w_u_B);
            double deltaSmin = newMin - oldMin;
            if (deltaSmin == 0.0) continue;

            long first = Math.min(A, B);
            long second = Math.max(A, B);

            minWeightsSums
                    .computeIfAbsent(first, __ -> new ConcurrentHashMap<>())
                    .merge(second, deltaSmin, Double::sum);

            double SA = sumWeightsByEvent.getOrDefault(first, 0.0);
            double SB = sumWeightsByEvent.getOrDefault(second, 0.0);
            double Smin = minWeightsSums.getOrDefault(first, Map.of())
                    .getOrDefault(second, 0.0);

            if (SA > 0 && SB > 0 && Smin > 0) {
                double score = Smin / (SA * SB);

                out.add(EventSimilarityAvro.newBuilder()
                        .setEventA(first)
                        .setEventB(second)
                        .setScore(score)
                        .setTimestamp(Instant.ofEpochMilli(tsMillis))
                        .build());
            }
        }

        return out;
    }
    /** Получение веса действия без отдельного класса-конфига */
    private double getUserActionWeight(ActionTypeAvro actionType) {
        if (actionType == null) return 0.0;
        return switch (actionType) {
            case VIEW     -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE     -> LIKE_WEIGHT;
        };
    }
}