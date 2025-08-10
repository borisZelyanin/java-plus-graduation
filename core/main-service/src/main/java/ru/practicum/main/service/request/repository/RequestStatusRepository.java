package ru.practicum.main.service.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.service.request.model.RequestStatus;
import ru.practicum.main.service.request.model.RequestStatusEntity;

import java.util.Optional;

@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatusEntity, Long> {
    Optional<RequestStatusEntity> findByName(RequestStatus name);
}
