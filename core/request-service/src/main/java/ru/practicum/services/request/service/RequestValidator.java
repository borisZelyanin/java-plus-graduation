package ru.practicum.services.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.enums.EventState;
import ru.practicum.lib.exception.ConflictException;
import ru.practicum.lib.exception.ValidationException;
import ru.practicum.services.request.model.Request;
import ru.practicum.services.request.repository.RequestRepository;


@Slf4j
@Component
public class RequestValidator {

    private final RequestRepository requestRepository;

    @Autowired
    public RequestValidator(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public void validateRequestCreation(UserDto user, EventFullDto event) {
        checkEventState(event);
        checkEventOwnership(user, event);
        checkDuplicateRequest(user.getId(), event.getId());
        checkEventCapacity(event);
    }

    private void checkEventState(EventFullDto event) {
        if (!event.getState().name().equals(EventState.PUBLISHED.name())) {
            throw new ConflictException("Нельзя подавать заявку на неопубликованное мероприятие");
        }
    }

    private void checkEventOwnership(UserDto user, EventFullDto event) {
        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Пользователь не может подать заяку на участие в своем же мероприятии");
        }
    }

    private void checkDuplicateRequest(Long userId, Long eventId) {
        requestRepository.findByRequesterAndEvent(userId, eventId)
                .ifPresent(req -> {
                    throw new ConflictException("Пользователь: " +
                            userId + " уже подал заявку на участи в событии: " + eventId);
                });
    }

    public void validateRequestOwnership(UserDto user, Request request) {
        if (!request.getRequester().equals(user)) {
            throw new ValidationException("Только пользователь подавший заявку может отменить ее. " +
                    "Пользователь ID: " + user.getId() +
                    "Заявка с ID: " + request.getId());
        }
    }

    private void checkEventCapacity(EventFullDto event) {
        if (event.getParticipantLimit() > 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Событие с ID: " + event.getId() + " нет свободных слотов");
        }
    }
}
