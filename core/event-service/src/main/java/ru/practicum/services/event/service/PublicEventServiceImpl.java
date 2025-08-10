package ru.practicum.services.event.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.lib.dto.category.CategoryDto;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.EventShortDto;
import ru.practicum.lib.dto.event.SearchPublicEventsParamDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.enums.EventState;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.services.event.mapper.EventMapper;
import ru.practicum.services.event.model.Event;
import ru.practicum.services.event.repository.EventRepository;
import ru.practicum.services.event.support.EventServiceHelperBean;
import ru.practicum.services.event.support.EventValidator;
import ru.practicum.services.event.utils.FeigenClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@Transactional
public class PublicEventServiceImpl implements  PublicEventService {

    private final EventRepository eventRepository;
    private final EventServiceHelperBean eventServiceHelperBean;
    private final EventValidator eventValidator;
    private final FeigenClient feigenClient;


    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> searchPublicEvents(SearchPublicEventsParamDto searchParams) {

        Specification<Event> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Базовые условия
            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), searchParams.getRangeStart()));
            predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), searchParams.getRangeEnd()));
            predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

            // Фильтр по тексту
            if (StringUtils.hasText(searchParams.getText())) {
                String searchTerm = "%" + searchParams.getText().toLowerCase() + "%";
                Predicate annotationLike = cb.like(cb.lower(root.get("annotation")), searchTerm);
                Predicate descriptionLike = cb.like(cb.lower(root.get("description")), searchTerm);
                predicates.add(cb.or(annotationLike, descriptionLike));
            }

            // Фильтр по категориям
            if (searchParams.getCategoriesIds() != null && !searchParams.getCategoriesIds().isEmpty()) {
                predicates.add(root.get("category").get("id").in(searchParams.getCategoriesIds()));
            }

            // Фильтр по paid
            if (searchParams.getPaid() != null) {
                predicates.add(cb.equal(root.get("paid"), searchParams.getPaid()));
            }

            // Фильтр по доступности
            if (searchParams.isOnlyAvailable()) {
                predicates.add(cb.gt(root.get("participantLimit"), root.get("confirmedRequests")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Event> eventsPage = eventRepository.findAll(
                specification,
                PageRequest.of(searchParams.getPageRequest().getPageNumber(),
                        searchParams.getPageRequest().getPageSize(),
                        searchParams.getPageRequest().getSort())
        );

        List<Event> events = eventsPage.getContent();
        if (events.isEmpty()) {
            return new ArrayList<>();
        }
        return paginateAndMap(events, searchParams.getPageRequest());
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventServiceHelperBean.getEventById(eventId);
        UserDto user = feigenClient.getUserById(event.getInitiator());
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("У события должен быть статус <PUBLISHED>");
        }
        return EventMapper.toFullDto(event,user);
    }

    private List<EventShortDto> paginateAndMap(List<Event> events, PageRequest pageRequest) {
        List<Event> paginatedEvents = events.stream()
                .skip(pageRequest.getOffset())
                .toList();

        return paginatedEvents.stream()
                .map(  event -> {
                    UserDto    user     = feigenClient.getUserById(event.getInitiator());
                    return EventMapper.toShortDto(event,  user);
                })
                .toList();
    }
}
