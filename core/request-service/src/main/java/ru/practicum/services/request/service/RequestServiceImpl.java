package ru.practicum.services.request.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.services.request.mapper.RequestMapper;
import ru.practicum.services.request.model.Request;
import ru.practicum.lib.enums.RequestStatus;
import ru.practicum.services.request.repository.RequestRepository;
import ru.practicum.services.request.support.EventServiceHelperBean;
import ru.practicum.services.request.utils.FeigenClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventServiceHelperBean EventServiceHelperBean;
    private final RequestValidator requestValidator;
    private final FeigenClient FeigenClient;


    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.debug("Запрос на получение всех заявок участия пользователя с ID: {}", userId);
        return requestRepository.findByRequester( userId).stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        final UserDto user = FeigenClient.getUserById(userId);
        final EventFullDto event = FeigenClient.getEventById(eventId);

        requestValidator.validateRequestCreation(user, event);

        final Request request = EventServiceHelperBean.buildNewRequest(user, event);
        EventServiceHelperBean.determineInitialStatus(event, request);

        final Request savedRequest = requestRepository.save(request);
        EventServiceHelperBean.updateEventStatistics(event, request.getStatus().getName());

        log.info("Заявка на участие сохранена со статусом с ID: {} и статусом: {}",
                savedRequest.getId(), savedRequest.getStatus());
        return RequestMapper.toRequestDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        final UserDto user = FeigenClient.getUserById(userId);
        final Request request = EventServiceHelperBean.getRequestById(requestId);
        final EventFullDto eventDto = FeigenClient.getEventById(request.getEvent());

        requestValidator.validateRequestOwnership(user, request);
        EventServiceHelperBean.updateRequestStatus(request, RequestStatus.CANCELED);

        if (request.getStatus().getName() == RequestStatus.CONFIRMED) {
            EventServiceHelperBean.adjustEventConfirmedRequests(eventDto, -1);
        }

        log.info("Заявка на участие с id = {} отменена пользователем ID: {}", requestId, userId);
        return RequestMapper.toRequestDto(request);
    }

    @Override
    public ParticipationRequestDto getParticipationRequest(Long userId, Long eventId) {
        Request request = requestRepository.findByRequesterAndEvent(userId,eventId)
                .orElseThrow(() ->
                        new NotFoundException("Заявка с id=" + eventId + " у пользователя id=" + userId + " не найдена"));
        return RequestMapper.toRequestDto(request);
    }


}

