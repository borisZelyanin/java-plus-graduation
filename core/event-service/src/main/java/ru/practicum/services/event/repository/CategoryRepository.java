package ru.practicum.services.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<ru.practicum.services.event.model.Category, Long> {
    List<ru.practicum.services.event.model.Category> findByIdIn(List<Long> categoriesId, Pageable pageable);
}
