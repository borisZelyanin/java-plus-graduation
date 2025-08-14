package ru.practicum.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.model.EventPairId;
import ru.practicum.analyzer.model.EventSimilarityEntity;

import java.util.List;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarityEntity, EventPairId> {

    @Query("""
           select s
           from EventSimilarityEntity s
           where s.eventA = :eventId or s.eventB = :eventId
           """)
    List<EventSimilarityEntity> findAllForEvent(@Param("eventId") long eventId);

    @Query("""
           select s
           from EventSimilarityEntity s
           where s.eventA = :eventId or s.eventB = :eventId
           order by s.score desc
           """)
    List<EventSimilarityEntity> topSimilar(@Param("eventId") long eventId, Pageable pageable);
}