package ru.practicum.services.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.services.event.model.Compilation;


@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {
}