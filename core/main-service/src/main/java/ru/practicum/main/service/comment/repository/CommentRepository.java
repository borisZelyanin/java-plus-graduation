package ru.practicum.main.service.comment.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.service.comment.model.Comment;
import ru.practicum.main.service.event.model.Event;
import ru.practicum.main.service.user.model.User;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    void deleteByEvent(Event event);

    List<Comment> findByEvent(Event event,
                              PageRequest pageRequest);

    List<Comment> findByAuthorAndEvent(User author,
                                       Event event,
                                       PageRequest pageRequest);
}