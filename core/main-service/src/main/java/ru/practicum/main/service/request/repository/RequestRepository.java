package ru.practicum.main.service.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.service.request.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    Optional<Request> findByRequesterIdAndEventId(Long requesterId,
                                                  Long eventId);

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);

    List<Request> findRequestByIdIn(List<Long> requestsId);
}
