package ru.practicum.main.service.comment.mapper;

import ru.practicum.main.service.comment.dto.CommentRequestDto;
import ru.practicum.main.service.comment.dto.CommentResponseDto;
import ru.practicum.main.service.comment.model.Comment;
import ru.practicum.main.service.event.mapper.EventMapper;
import ru.practicum.main.service.event.model.Event;
import ru.practicum.main.service.user.model.User;

import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment toComment(CommentRequestDto commentRequestDto,
                                    User user,
                                    Event event) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .created(LocalDateTime.now())
                .author(user)
                .event(event).build();
    }

    public static CommentResponseDto toCommentResponseDto(Comment comment) {

        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .event(EventMapper.toShortDto(comment.getEvent()))
                .created(comment.getCreated())
                .build();
    }
}
