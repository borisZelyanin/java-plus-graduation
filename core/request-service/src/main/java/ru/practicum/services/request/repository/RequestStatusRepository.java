package ru.practicum.services.request.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.lib.enums.RequestStatus;
import ru.practicum.services.request.model.RequestStatusEntity;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatusEntity, Long> {
    // найти по одному enum
    Optional<RequestStatusEntity> findByName(RequestStatus name);

    // найти по набору enum’ов
    List<RequestStatusEntity> findByNameIn(Collection<RequestStatus> names);

    // удобный «reference» вариант (не обязательно):
    @EntityGraph(attributePaths = {}) // можно убрать
    default RequestStatusEntity getRequired(RequestStatus name) {
        return findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + name));
    }
}
