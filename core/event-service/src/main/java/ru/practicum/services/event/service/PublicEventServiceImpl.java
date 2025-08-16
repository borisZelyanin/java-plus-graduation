package ru.practicum.services.event.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.EventShortDto;
import ru.practicum.lib.dto.event.SearchPublicEventsParamDto;
import ru.practicum.lib.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.lib.dto.request.UpdateRequestsStatusDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.enums.EventState;
import ru.practicum.lib.enums.RequestStatus;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.services.event.mapper.EventMapper;
import ru.practicum.services.event.model.Event;
import ru.practicum.services.event.repository.EventRepository;
import ru.practicum.services.event.support.EventServiceHelperBean;
import ru.practicum.services.event.support.EventValidator;
import ru.practicum.services.event.utils.FeigenClient;
import ru.practicum.wrap.grpc.client.stats.AnalyzerClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@Transactional
public class PublicEventServiceImpl implements  PublicEventService {

    private final EventRepository eventRepository;
    private final EventServiceHelperBean eventServiceHelperBean;
    private final EventValidator eventValidator;
    private final FeigenClient feignClient;
    private final AnalyzerClient analyzerClient;



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
        UserDto user = feignClient.getUserById(event.getInitiator());
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
                    UserDto    user     = feignClient.getUserById(event.getInitiator());
                    return EventMapper.toShortDto(event,  user);
                })
                .toList();
    }


    @Override
    public List<EventFullDto> getRecommendationsForUser(long userId, int maxResult) {
        UserDto user = feignClient.getUserById(userId); // проверим, что пользователь такой есть
        // получи список ID рекомендованных мероприятий для пользователя
        List<Long> eventIds = analyzerClient.getRecommendationsForUser(userId, maxResult)
                .map(RecommendedEventProto::getEventId).toList();
        // выгрузим мероприятия по этому списку
        List<Event> events = eventRepository.findByIdIn(eventIds);
        // получим ID инициаторов мероприятий
        Set<Long> initiatorIds = events.stream().map(Event::getInitiator).collect(Collectors.toSet());
        // выгрузим инициаторов в мапу
        Map<Long, UserDto> initiatorsMap = feignClient.getUserListById(initiatorIds.stream().toList()).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        // замапим мероприятия в список DTO
        List<EventFullDto> eventsFullDto = events.stream()
                .map(o -> EventMapper.toFullDto(o, initiatorsMap.get(o.getInitiator()))).toList();
        // загрузим статистику, рейтинги, запросы
        loadStatisticAndRequestForList(eventsFullDto);

        return eventsFullDto;
    }

    private List<EventFullDto> loadStatisticAndRequestForList(List<EventFullDto> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        // получим рейтинги мероприятий из сервиса
        Map<Long, Double> ratingsMap = analyzerClient.getInteractionsCount(events.stream().map(EventFullDto::getId).collect(Collectors.toList()));

        return events.stream()
                .peek(event -> event.setRating(ratingsMap.getOrDefault(event.getId(), 0.0)))
                .toList();
    }

    @Override
    public void checkUserRegistrationAtEvent(long userId, long eventId) {
        UserDto user = feignClient.getUserById(userId);
        Event event = eventServiceHelperBean.getEventById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " not published");
        }

        if (event.getRequestModeration() || event.getParticipantLimit() != 0) {
            if (event.getInitiator() != userId && feignClient.getParticipationRequest(userId, eventId)
                    .filter(o -> o.getStatus() == RequestStatus.CONFIRMED).isEmpty()) {
                throw new BadRequestException("User with id=" + userId + " cannot work with event ID=" + event);
            }
        }
    }

}
