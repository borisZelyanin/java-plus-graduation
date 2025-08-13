package ru.practicum.services.comment.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.services.comment.model.Comment;


import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    void deleteByEvent(Long event);

    List<Comment> findByEvent(Long event,
                              PageRequest pageRequest);

    List<Comment> findByAuthorAndEvent(Long author,
                                       Long event,
                                       PageRequest pageRequest);
}