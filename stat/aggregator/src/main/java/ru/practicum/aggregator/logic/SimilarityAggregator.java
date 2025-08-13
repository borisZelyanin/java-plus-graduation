package ru.practicum.aggregator.logic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SimilarityAggregator {

    // eventId -> (userId -> maxWeight)
    private final Map<Long, Map<Long, Double>> weightsByEventUser = new ConcurrentHashMap<>();
    // eventId -> S_A
    private final Map<Long, Double> sumWeightsByEvent = new ConcurrentHashMap<>();
    // firstEventId -> (secondEventId -> S_min), где first < second
    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

    // веса действий
    private static final double VIEW_WEIGHT = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT = 1.0;

    /**
     * Инкрементально пересчитывает сходство для пар (A,B), где A — событие из action,
     * B — события, с которыми этот же пользователь уже взаимодействовал.
     */
    public Optional<List<EventSimilarityAvro>> updateState(UserActionAvro action) {
        if (action == null) return Optional.empty();

        final long userId = action.getUserId();
        final long eventId = action.getEventId();
        final long tsMillis = action.getTimestamp() != null ? action.getTimestamp().toEpochMilli() : System.currentTimeMillis();

        final double newWeightCandidate = toWeight(action.getActionType());
        if (newWeightCandidate <= 0.0) return Optional.empty();

        Map<Long, Double> usersForEvent =
                weightsByEventUser.computeIfAbsent(eventId, e -> new ConcurrentHashMap<>());
        double oldWeight = usersForEvent.getOrDefault(userId, 0.0);

        // обновляем только если вес вырос
        if (newWeightCandidate <= oldWeight) {
            return Optional.empty();
        }

        final double newWeight = newWeightCandidate;
        final double deltaWA = newWeight - oldWeight;

        // 1) обновить w_{u,A} и S_A
        usersForEvent.put(userId, newWeight);
        sumWeightsByEvent.merge(eventId, deltaWA, Double::sum);

        // 2) обновить S_min(A,B) и посчитать score для всех B, где у этого u уже есть вес
        List<EventSimilarityAvro> out = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : weightsByEventUser.entrySet()) {
            long otherEventId = entry.getKey();
            if (otherEventId == eventId) continue;

            Double w_u_B = entry.getValue().get(userId);
            if (w_u_B == null || w_u_B <= 0.0) continue;

            double oldMin = Math.min(oldWeight, w_u_B);
            double newMin = Math.min(newWeight, w_u_B);
            double deltaSmin = newMin - oldMin;
            if (deltaSmin == 0.0) continue;

            addToSmin(eventId, otherEventId, deltaSmin);

            long first = Math.min(eventId, otherEventId);
            long second = Math.max(eventId, otherEventId);

            double SA = sumWeightsByEvent.getOrDefault(first, 0.0);
            double SB = sumWeightsByEvent.getOrDefault(second, 0.0);
            double Smin = getSmin(first, second);

            if (SA > 0 && SB > 0 && Smin > 0) {
                double score = Smin / (SA * SB);

                EventSimilarityAvro sim = new EventSimilarityAvro();
                sim.setEventA(first);
                sim.setEventB(second);
                sim.setScore(score);
                sim.setTimestamp(Instant.ofEpochSecond(tsMillis)); // <-- long миллисекунды

                out.add(sim);
            }
        }

        return out.isEmpty() ? Optional.empty() : Optional.of(out);
    }

    private double toWeight(ActionTypeAvro type) {
        if (type == null) return 0.0;
        return switch (type) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };
    }

    private void addToSmin(long eventA, long eventB, double delta) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        minWeightsSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .merge(second, delta, Double::sum);
    }

    private double getSmin(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return minWeightsSums
                .getOrDefault(first, Collections.emptyMap())
                .getOrDefault(second, 0.0);
    }
}