package ru.practicum.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.EventSimilarityEntity;
import ru.practicum.analyzer.model.EventPairId;

import java.util.*;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarityEntity, EventPairId> {

    List<EventSimilarityEntity> findAllForEvent(@Param("eventId") long eventId);

    List<EventSimilarityEntity> topSimilar(@Param("eventId") long eventId, Pageable pageable);
}