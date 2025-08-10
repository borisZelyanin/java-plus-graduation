package ru.practicum.services.event.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.lib.dto.event.*;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.services.event.mapper.EventMapper;
import ru.practicum.services.event.model.Event;
import ru.practicum.services.event.repository.CategoryRepository;
import ru.practicum.services.event.repository.EventRepository;
import ru.practicum.services.event.support.EventServiceHelperBean;
import ru.practicum.services.event.utils.FeigenClient;
import ru.practicum.services.event.support.EventValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final EventServiceHelperBean eventServiceHelperBean;
    private final EventValidator eventValidator;
    private final FeigenClient feigenClient;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> searchEventsByAdmin(SearchAdminEventsParamDto searchParams) {

        return eventRepository.findAll((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    // Фильтр по пользователям
                    if (searchParams.getUsers() != null && !searchParams.getUsers().isEmpty()) {
                        predicates.add(root.get("initiator").in(searchParams.getUsers()));
                    }

                    // Фильтр по состояниям
                    if (searchParams.getEventStates() != null && !searchParams.getEventStates().isEmpty()) {
                        predicates.add(root.get("state").in(searchParams.getEventStates()));
                    }

                    // Фильтр по категориям
                    if (searchParams.getCategoriesIds() != null && !searchParams.getCategoriesIds().isEmpty()) {
                        predicates.add(root.get("category").in(searchParams.getCategoriesIds()));
                    }

                    // Фильтр по датам
                    predicates.add(cb.between(root.get("eventDate"), searchParams.getRangeStart(),
                            searchParams.getRangeEnd()));

                    return cb.and(predicates.toArray(new Predicate[0]));
                }, searchParams.getPageRequest()).stream()
                .map(event -> {
                    UserDto    user     = feigenClient.getUserById(event.getInitiator());
                    return EventMapper.toFullDto(event, user);
                })
                .collect(Collectors.toList());
    }

    public EventFullDto getEventById(Long eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие с ID: " + eventId));
        UserDto user = feigenClient.getUserById(event.getInitiator());
        return EventMapper.toFullDto(event,user);
    }

    @Transactional
    @Override
    public EventFullDto saveForce(EventFullDto eventDto) {
        Event eventEntity = EventMapper.toEventFull(eventDto);
        // сохраняем в БД
        Event savedEvent = eventRepository.save(eventEntity);
        // возвращаем обратно в формате DTO
        UserDto user = feigenClient.getUserById(savedEvent.getInitiator());
        return EventMapper.toFullDto(savedEvent,user);
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(Long eventId,
                                           UpdateEventAdminRequest updateEventAdminRequest) {
        Event oldEvent = eventServiceHelperBean.getEventById(eventId);
        eventValidator.validateAdminPublishedEventDate(updateEventAdminRequest.getEventDate(), oldEvent);
        eventValidator.validateAdminEventDate(oldEvent);
        eventValidator.validateAdminEventUpdateState(oldEvent.getState());
        eventServiceHelperBean.applyAdminUpdates(oldEvent, updateEventAdminRequest);
        Event event = eventRepository.save(oldEvent);
        log.info("Событие успешно обновлено администратором");
        UserDto user = feigenClient.getUserById(event.getInitiator());
        return EventMapper.toFullDto(event,user);
    }
}