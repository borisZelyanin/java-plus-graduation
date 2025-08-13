// src/main/java/ru/practicum/analyzer/service/RecommendationService.java
package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.config.AnalyzerProperties;
import ru.practicum.analyzer.model.EventSimilarityEntity;
import ru.practicum.analyzer.model.InteractionEntity;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.InteractionRepository;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final InteractionRepository interactions;
    private final EventSimilarityRepository similarities;
    private final AnalyzerProperties props;

    /** stream "Similar events" с исключением того, что юзер уже видел/лайкнул/регистрировался */
    public List<Map.Entry<Long, Double>> similarEventsForUser(long eventId, long userId, int maxResults) {
        Set<Long> seen = interactions.findAllEventIdsUserInteracted(userId);

        List<EventSimilarityEntity> sims =
                similarities.findAllForEvent(eventId);

        return sims.stream()
                .map(s -> {
                    long other = (s.getEventA() == eventId) ? s.getEventB() : s.getEventA();
                    return Map.entry(other, s.getScore());
                })
                .filter(e -> !seen.contains(e.getKey()))
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /** рекомендации с предсказанием оценки по K ближайшим соседям */
    public List<Map.Entry<Long, Double>> recommendationsForUser(long userId, int maxResults) {
        // 1) недавние взаимодействия пользователя
        int recentN = props.getLogic().getRecentN();
        List<InteractionEntity> recent =
                interactions.findRecentByUser(userId, PageRequest.of(0, recentN));
        if (recent.isEmpty()) return List.of();

        Set<Long> already = recent.stream().map(InteractionEntity::getEventId).collect(Collectors.toSet());

        // 2) кандидаты: похожие на недавно просмотренные, которых пользователь не видел
        Map<Long, Double> candidateBestSim = new HashMap<>();
        for (InteractionEntity it : recent) {
            List<EventSimilarityEntity> sims = similarities.findAllForEvent(it.getEventId());
            for (EventSimilarityEntity s : sims) {
                long other = (s.getEventA().equals(it.getEventId())) ? s.getEventB() : s.getEventA();
                if (already.contains(other)) continue;
                candidateBestSim.merge(other, s.getScore(), Math::max);
            }
        }
        if (candidateBestSim.isEmpty()) return List.of();

        // 3) предсказание: взвешенная сумма оценок K ближайших соседей
        int k = props.getLogic().getKNeighbors();
        // взаимодействия пользователя -> карта (eventId -> weight)
        Map<Long, Double> userRatings = recent.stream()
                .collect(Collectors.toMap(InteractionEntity::getEventId, InteractionEntity::getWeight, (a,b)->a));

        List<Map.Entry<Long, Double>> scored = new ArrayList<>();
        for (long candidate : candidateBestSim.keySet()) {
            // найдём соседей: все сходства (candidate, e_seen)
            List<EventSimilarityEntity> sims = similarities.findAllForEvent(candidate);

            // отфильтруем только те, где второй — из уже виденных пользователем
            List<Map.Entry<Long, Double>> neighbors = sims.stream()
                    .map(s -> {
                        long other = (s.getEventA() == candidate) ? s.getEventB() : s.getEventA();
                        return Map.entry(other, s.getScore());
                    })
                    .filter(e -> userRatings.containsKey(e.getKey()))
                    .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                    .limit(k)
                    .toList();

            if (neighbors.isEmpty()) continue;

            double num = 0.0;
            double den = 0.0;
            for (var n : neighbors) {
                double sim = n.getValue();
                double r = userRatings.get(n.getKey()); // 0..1 (макс. вес)
                num += sim * r;
                den += sim;
            }
            if (den > 0) {
                scored.add(Map.entry(candidate, num / den));
            }
        }

        scored.sort((a,b) -> Double.compare(b.getValue(), a.getValue()));
        return scored.stream().limit(maxResults).toList();
    }

    /** сумма максимальных весов по каждому eventId */
    public Map<Long, Double> interactionsCount(Collection<Long> eventIds) {
        Map<Long, Double> out = new HashMap<>();
        interactions.sumWeightsByEventIds(eventIds).forEach(arr -> {
            Long eventId = (Long) arr[0];
            Double sum = (Double) arr[1];
            out.put(eventId, sum);
        });
        // fill zeros for absent
        for (Long id : eventIds) out.putIfAbsent(id, 0.0);
        return out;
    }
}