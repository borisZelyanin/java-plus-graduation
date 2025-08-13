package ru.practicum.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // не обязательно, но можно

import ru.practicum.analyzer.model.InteractionEntity;
import ru.practicum.analyzer.model.InteractionId;

import java.util.*;

@Repository
public interface InteractionRepository extends JpaRepository<InteractionEntity, InteractionId> {

    @Query("select i from InteractionEntity i where i.userId=:userId order by i.updatedAt desc")
    List<InteractionEntity> findRecentByUser(@Param("userId") long userId, Pageable pageable);

    @Query("select i from InteractionEntity i where i.userId=:userId and i.eventId in :eventIds")
    List<InteractionEntity> findByUserAndEvents(@Param("userId") long userId, @Param("eventIds") Collection<Long> eventIds);

    @Query("select i.eventId, sum(i.weight) from InteractionEntity i where i.eventId in :eventIds group by i.eventId")
    List<Object[]> sumWeightsByEventIds(@Param("eventIds") Collection<Long> eventIds);

    @Query("select i.eventId from InteractionEntity i where i.userId=:userId")
    Set<Long> findAllEventIdsUserInteracted(@Param("userId") long userId);
}