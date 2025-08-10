package ru.practicum.services.event.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.EventShortDto;
import ru.practicum.lib.dto.event.NewEventDto;
import ru.practicum.lib.dto.event.UpdateEventUserRequest;
import ru.practicum.lib.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.lib.dto.request.UpdateRequestsStatusDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.enums.EventState;
import ru.practicum.lib.enums.RequestStatus;
import ru.practicum.services.event.mapper.EventMapper;
import ru.practicum.services.event.mapper.LocationMapper;
import ru.practicum.services.event.model.Category;
import ru.practicum.services.event.model.Event;
import ru.practicum.services.event.model.LocationEntity;
import ru.practicum.services.event.repository.EventRepository;
import ru.practicum.services.event.support.EventServiceHelperBean;
import ru.practicum.services.event.utils.FeigenClient;
import ru.practicum.services.event.utils.LocationCalc;
import ru.practicum.services.event.support.EventValidator;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@Transactional
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final FeigenClient feigenClient;
    private final LocationCalc locationCalc;
    private final EventValidator eventValidator;
    private final EventServiceHelperBean eventServiceHelperBean;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Pageable pageable) {
        eventValidator.validateUserExists(userId);
        return eventRepository.findByInitiator(userId, pageable)
                .stream()
                .map(  event -> {
                    UserDto    user     = feigenClient.getUserById(event.getInitiator());
                    return EventMapper.toShortDto(event,  user);
                })
                .collect(Collectors.toList());
    }

    public EventFullDto createEvent(Long userId, NewEventDto request) {
        Category category = eventServiceHelperBean.getCategoryById(request.getCategory());
        LocationEntity location = locationCalc.ResolveLocation(LocationMapper.toEntity(request.getLocation()));
        UserDto    user     = feigenClient.getUserById(userId);
        Event event = EventMapper.toEvent(request, user.getId(), category);
        event.setLocation(location);
        event.setState(EventState.PENDING);

        Event savedEvent = eventRepository.save(event);
        log.info("Событие успешно добавлено под id {} со статусом {} и ожидается подтверждение",
                user.getId(), event.getState());
        return EventMapper.toFullDto(savedEvent,user);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventServiceHelperBean.getEventById(eventId);
        eventValidator.validateEventOwnership(event, userId);
        UserDto    user     = feigenClient.getUserById(event.getInitiator());
        return EventMapper.toFullDto(event,user);
    }

    @Override
    public EventFullDto updateUserEvent(Long userId,
                                        Long eventId,
                                        UpdateEventUserRequest updateDto) {

        UserDto user = feigenClient.getUserById(userId);
        Event event = eventServiceHelperBean.getEventById(eventId);


        eventValidator.validateUserUpdate(event, user, updateDto);
        eventServiceHelperBean.applyUserUpdates(event, updateDto);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие успешно обновлено под id {} и дожидается подтверждения", eventId);
        return EventMapper.toFullDto(updatedEvent, user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventServiceHelperBean.getEventById(eventId);
        eventValidator.validateEventOwnership(event, userId);

        return  feigenClient.getRequestByEvent(eventId);
    }

    @Override
    public Map<String, List<ParticipationRequestDto>> approveRequests(Long userId,
                                                                      Long eventId,
                                                                      EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        UserDto user = feigenClient.getUserById(userId);
        Event event = eventServiceHelperBean.getEventById(eventId);
        eventValidator.validateInitiator(event, user);

        List<ParticipationRequestDto> requests = eventServiceHelperBean.getAndValidateRequests(eventId, eventRequestStatusUpdateRequest.getRequestIds());
        RequestStatus status = eventRequestStatusUpdateRequest.getStatus();

        if (status == RequestStatus.CONFIRMED) {
            eventValidator.validateParticipantLimit(event);
        }

        eventValidator.validateNoConfirmedRequests(requests);

        if (status == RequestStatus.REJECTED) {
            updateRequestStatuses(requests, RequestStatus.REJECTED);
            List<ParticipationRequestDto> rejectedRequests = feigenClient.saveBatchRequests(requests);
            return Map.of("rejectedRequests", rejectedRequests);
        }

        int availableSlots = event.getParticipantLimit() - event.getConfirmedRequests();
        List<ParticipationRequestDto> confirmed = requests.stream().limit(availableSlots).toList();
        List<ParticipationRequestDto> rejected = requests.stream().skip(availableSlots).toList();

        updateRequestStatuses(confirmed, RequestStatus.CONFIRMED);
        updateRequestStatuses(rejected, RequestStatus.REJECTED);

        feigenClient.saveBatchRequests(requests);
        event.setConfirmedRequests(event.getConfirmedRequests() + confirmed.size());
        eventRepository.save(event);

        return Map.of(
                "confirmedRequests", confirmed,
                "rejectedRequests", rejected
        );

    }

    public void updateRequestStatuses(List<ParticipationRequestDto> requests,
                                      RequestStatus status) {
        UpdateRequestsStatusDto body = new UpdateRequestsStatusDto();
        body.setStatus(status);
        body.setRequests(requests);
        feigenClient.updateBatchRequestStatuses(body);
    }

}
