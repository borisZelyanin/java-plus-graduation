package ru.practicum.services.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.services.request.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequester(Long requesterId);

    Optional<Request> findByRequesterAndEvent(Long requesterId, Long eventId);

    List<Request> findByEvent(Long eventId);


}
