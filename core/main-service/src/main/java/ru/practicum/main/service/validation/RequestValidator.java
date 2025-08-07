package ru.practicum.main.service.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.main.service.event.model.Event;
import ru.practicum.main.service.event.model.EventState;
import ru.practicum.main.service.exceptions.ConflictException;
import ru.practicum.main.service.exceptions.ValidationException;
import ru.practicum.main.service.request.model.Request;
import ru.practicum.main.service.request.repository.RequestRepository;
import ru.practicum.main.service.user.model.User;

@Slf4j
@Component
public class RequestValidator {

    private final RequestRepository requestRepository;

    @Autowired
    public RequestValidator(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public void validateRequestCreation(User user, Event event) {
        checkEventState(event);
        checkEventOwnership(user, event);
        checkDuplicateRequest(user.getId(), event.getId());
        checkEventCapacity(event);
    }

    private void checkEventState(Event event) {
        if (!event.getState().name().equals(EventState.PUBLISHED.name())) {
            throw new ConflictException("Нельзя подавать заявку на неопубликованное мероприятие");
        }
    }

    private void checkEventOwnership(User user, Event event) {
        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Пользователь не может подать заяку на участие в своем же мероприятии");
        }
    }

    private void checkDuplicateRequest(Long userId, Long eventId) {
        requestRepository.findByRequesterIdAndEventId(userId, eventId)
                .ifPresent(req -> {
                    throw new ConflictException("Пользователь: " +
                            userId + " уже подал заявку на участи в событии: " + eventId);
                });
    }

    public void validateRequestOwnership(User user, Request request) {
        if (!request.getRequester().equals(user)) {
            throw new ValidationException("Только пользователь подавший заявку может отменить ее. " +
                    "Пользователь ID: " + user.getId() +
                    "Заявка с ID: " + request.getId());
        }
    }

    private void checkEventCapacity(Event event) {
        if (event.getParticipantLimit() > 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Событие с ID: " + event.getId() + " нет свободных слотов");
        }
    }
}
