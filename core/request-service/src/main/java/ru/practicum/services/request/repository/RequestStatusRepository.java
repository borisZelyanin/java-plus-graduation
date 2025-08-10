package ru.practicum.services.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.lib.enums.RequestStatus;
import ru.practicum.services.request.model.RequestStatusEntity;


import java.util.Optional;

@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatusEntity, Long> {
    Optional<RequestStatusEntity> findByName(RequestStatus name);
}
