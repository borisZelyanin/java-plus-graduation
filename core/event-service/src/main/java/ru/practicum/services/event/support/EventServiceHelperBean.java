package ru.practicum.services.event.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.lib.dto.event.UpdateEventAdminRequest;
import ru.practicum.lib.dto.event.UpdateEventUserRequest;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.lib.enums.EventState;
import ru.practicum.lib.enums.StateAction;
import ru.practicum.lib.exception.ConflictException;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.lib.exception.ValidationException;
import ru.practicum.services.event.model.Category;
import ru.practicum.services.event.model.Event;
import ru.practicum.services.event.repository.CategoryRepository;
import ru.practicum.services.event.repository.EventRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class EventServiceHelperBean {

    private final EventRepository eventRepository;
    private final EventValidator eventValidator;
    private final CategoryRepository categoryRepository;

    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие с ID: " + eventId));
    }

    private static <T> void applyIfPresent(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }

    private static <S, T> void applyIfPresent(S value, Function<S, T> mapper, Consumer<T> setter) {
        if (value != null) setter.accept(mapper.apply(value));
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ValidationException("Указана не правильная ID категории: " + categoryId));
    }



    public void applyUserUpdates(Event event, UpdateEventUserRequest update) {
        // Простые поля
        applyIfPresent(update.getAnnotation(),        event::setAnnotation);
        applyIfPresent(update.getDescription(),       event::setDescription);
        applyIfPresent(update.getEventDate(),         event::setEventDate);
        applyIfPresent(update.getPaid(),              event::setPaid);
        applyIfPresent(update.getParticipantLimit(),  event::setParticipantLimit);
        applyIfPresent(update.getRequestModeration(), event::setRequestModeration);
        applyIfPresent(update.getTitle(),             event::setTitle);

        // Поля, требующие преобразования/подтяжки извне
//        applyIfPresent(update.getLocation(), this::resolveLocation,       event::setLocation);
//        applyIfPresent(update.getCategory(), FeigenClient::getCategoryById, event::setCategory);

        // Спец‑логика
        if (update.getStateAction() != null) {
            updateState(update.getStateAction(), event);
        }
    }

//    public List<ParticipationRequestDto> getAndValidateRequests(Long eventId, List<Long> requestIds) {
//        List<ParticipationRequestDto> requests = requestRepository.findRequestByIdIn(requestIds);
//        eventValidator.validateRequestsBelongToEvent(requests, eventId);
//        return requests;
//    }


    private void updateState(StateAction stateAction, Event event) {
        if (stateAction == null) return;
        switch (stateAction) {
            case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
        }
    }

    public void applyAdminUpdates(Event event, UpdateEventAdminRequest update) {
        // Простые поля
        applyIfPresent(update.getAnnotation(),        event::setAnnotation);
        applyIfPresent(update.getDescription(),       event::setDescription);
        applyIfPresent(update.getEventDate(),         event::setEventDate);
        applyIfPresent(update.getPaid(),              event::setPaid);
        applyIfPresent(update.getParticipantLimit(),  event::setParticipantLimit);
        applyIfPresent(update.getRequestModeration(), event::setRequestModeration);
        applyIfPresent(update.getTitle(),             event::setTitle);

        // Поля, требующие преобразования
      //  applyIfPresent(update.getLocation(), LocationMapper::toEntity, event::setLocation);
       // applyIfPresent(update.getCategory(), FeigenClient::getCategoryById, event::setCategory);

        // Спец‑логика
        if (update.getStateAction() != null) {
            handleStateUpdateEventAdminRequest(update.getStateAction(), event);
        }
    }

    private static final EnumMap<StateAction, Consumer<Event>> STATE_TRANSITIONS =
            new EnumMap<>(StateAction.class);

    static {
        STATE_TRANSITIONS.put(StateAction.PUBLISH_EVENT, e -> {
            e.setState(EventState.PUBLISHED);
            e.setPublishedOn(java.time.LocalDateTime.now());
        });
        STATE_TRANSITIONS.put(StateAction.REJECT_EVENT, e -> {
            e.setState(EventState.CANCELED);
            e.setPublishedOn(null);
        });
    }


    private void handleStateUpdateEventAdminRequest(StateAction action, Event event) {
        if (event.getState() != EventState.PENDING) {
            throw new ConflictException("Изменение статуса возможно только для событий в состоянии PENDING");
        }

        var op = STATE_TRANSITIONS.get(action);
        if (op == null) {
            throw new UnsupportedOperationException("Неподдерживаемая операция: " + action);
        }
        op.accept(event);
    }
}
