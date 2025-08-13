package ru.practicum.services.comment.mapper;

import ru.practicum.lib.dto.comment.CommentRequestDto;
import ru.practicum.lib.dto.comment.CommentResponseDto;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.services.comment.model.Comment;
import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment toComment(CommentRequestDto commentRequestDto,
                                    UserDto user,
                                    EventFullDto event) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .created(LocalDateTime.now())
                .author(user.getId())
                .event(event.getId()).build();
    }

    public static CommentResponseDto toCommentResponseDto(Comment comment, UserDto user,
                                                          EventFullDto event) {

        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(user.getName())
                .event(event)
                .created(comment.getCreated())
                .build();
    }
}
