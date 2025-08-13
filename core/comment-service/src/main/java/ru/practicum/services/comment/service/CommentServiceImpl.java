package ru.practicum.services.comment.service;

import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.lib.dto.comment.CommentRequestDto;
import ru.practicum.lib.dto.comment.CommentResponseDto;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.enums.EventState;
import ru.practicum.lib.exception.ConflictException;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.services.comment.mapper.CommentMapper;
import ru.practicum.services.comment.model.Comment;



import ru.practicum.services.comment.repository.CommentRepository;
import ru.practicum.services.comment.utils.FeigenClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    private final FeigenClient FeigenClient;


    @Override
    public List<CommentResponseDto> findAll(Long userId,
                                            Long eventId,
                                            PageRequest pageRequest) {
        UserDto user = FeigenClient.getUserById(userId);
        EventFullDto event = FeigenClient.getEventById(eventId);
        List<Comment> comments = commentRepository.findByAuthorAndEvent(user.getId(), event.getId(), pageRequest);
        return comments.stream()
                .map(c -> CommentMapper.toCommentResponseDto(c, user, event))
                .toList();
    }

    @Override
    public CommentResponseDto save(CommentRequestDto commentRequestDto,
                                   Long userId,
                                   Long eventId) {
        UserDto user = FeigenClient.getUserById(userId);
        EventFullDto event = FeigenClient.getEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя написать комментарий к событию которое еще не было опубликованно");
        }
        Comment comment = commentRepository.save(CommentMapper.toComment(commentRequestDto, user, event));
        return CommentMapper.toCommentResponseDto(comment,user,event);
    }

    @Override
    public CommentResponseDto update(CommentRequestDto commentRequestDto,
                                     Long userId,
                                     Long commentId) {
        Comment oldComment = getCommentById(commentId);
        var user = FeigenClient.getUserById(userId);


        if (!oldComment.getAuthor().equals(userId)) {
            throw new ConflictException("Редактировать комментарии разрешено только его автору");
        }
        oldComment.setText(commentRequestDto.getText());
        Comment comment = commentRepository.save(oldComment);
        EventFullDto event = FeigenClient.getEventById(comment.getEvent());
        return CommentMapper.toCommentResponseDto(comment, user ,event);
    }

    @Override
    public void delete(Long userId,
                       Long commentId) {
        Comment comment = getCommentById(commentId);
        FeigenClient.getUserById(userId);
        EventFullDto event = FeigenClient.getEventById(comment.getEvent());

        if (!comment.getAuthor().equals(userId) &&
                !comment.getAuthor().equals(event.getInitiator().getId())) {
            throw new ConflictException("Удалять комментарии разрешено только его автору или инициатору мероприятия");
        }
        commentRepository.deleteById(commentId);
    }


    @Override
    public void deleteByIds(final List<Long> ids) {
//        List<EventShortDto> events = eventRepository.findAllById(ids);
//        if (ids.size() != events.size()) {
//            throw new ValidationException("Были переданы несуществующие id событий");
//        }
        commentRepository.deleteAllById(ids);
        log.info("Комментарии успешно удалены");
    }

    @Override
    public void deleteByEventId(Long eventId) {
        EventFullDto event = FeigenClient.getEventById(eventId);
        commentRepository.deleteByEvent(event.getId());
        log.info("Все комментарии у события с id = {} успешно удалены", eventId);
    }

    @Override
    public List<CommentResponseDto> findByEvent(Long eventId,
                                                PageRequest pageRequest) {
        EventFullDto event = FeigenClient.getEventById(eventId);
        List<Comment> comments = commentRepository.findByEvent(event.getId(), pageRequest);
        log.info("Получены все комментарии события с id = {}", eventId);
        return comments.stream()
                .map(c -> {
                    UserDto user = FeigenClient.getUserById(c.getAuthor());
                    return  CommentMapper.toCommentResponseDto(c, user, event);
                })
                .toList();
    }

    @Override
    public CommentResponseDto findById(final Long commentId) {
        Comment comment = getCommentById(commentId);
        UserDto user = FeigenClient.getUserById(comment.getAuthor());
        EventFullDto event = FeigenClient.getEventById(comment.getEvent());
        return CommentMapper.toCommentResponseDto(comment,user,event);
    }


    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментария с id = {} нет." + commentId));
    }
}
