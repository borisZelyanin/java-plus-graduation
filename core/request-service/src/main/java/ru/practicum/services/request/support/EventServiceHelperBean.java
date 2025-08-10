package ru.practicum.services.request.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.api.web.client.feign.UserServiceFeignClient;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.enums.RequestStatus;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.lib.exception.ValidationException;
import ru.practicum.services.request.model.Request;
import ru.practicum.services.request.model.RequestStatusEntity;
import ru.practicum.services.request.repository.RequestRepository;
import ru.practicum.services.request.repository.RequestStatusRepository;
import ru.practicum.services.request.utils.FeigenClient;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class EventServiceHelperBean {

    private final FeigenClient FeigenClient;
    private final RequestRepository requestRepository;
    private final RequestStatusRepository requestStatusRepository;


    public Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найдена заявка с ID: " + requestId));
    }

    public RequestStatusEntity getRequestStatusEntityByRequestStatus(RequestStatus newStatus) {
        return requestStatusRepository.findByName(newStatus)
                .orElseThrow(() -> new NotFoundException("Не найден статус: " + newStatus.name()));
    }

    public Request buildNewRequest(UserDto user, EventFullDto event) {
        RequestStatusEntity requestStatusEntity = getRequestStatusEntityByRequestStatus(RequestStatus.PENDING);
        return Request.builder()
                .requester(user.getId())
                .event(event.getId())
                .created(LocalDateTime.now())
                .status(requestStatusEntity)
                .build();
    }

    public void determineInitialStatus(EventFullDto event, Request request) {
        if (shouldAutoConfirm(event)) {
            request.setStatus(getRequestStatusEntityByRequestStatus(RequestStatus.CONFIRMED));
        } else if (isEventFull(event)) {
            request.setStatus(getRequestStatusEntityByRequestStatus(RequestStatus.REJECTED));
        }
    }

    private boolean shouldAutoConfirm(EventFullDto event) {
        return event.getParticipantLimit() == 0 ||
                (!event.getRequestModeration() && hasAvailableSlots(event));
    }

    private boolean isEventFull(EventFullDto event) {
        return event.getParticipantLimit() > 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit();
    }

    private  boolean hasAvailableSlots(EventFullDto event) {
        return event.getConfirmedRequests() < event.getParticipantLimit();
    }

    public void updateEventStatistics(EventFullDto event, RequestStatus status) {
        if (status == RequestStatus.CONFIRMED) {
            adjustEventConfirmedRequests(event, 1);
        }
    }

    public void adjustEventConfirmedRequests(EventFullDto event, int delta) {
        event.setConfirmedRequests(event.getConfirmedRequests() + delta);
        FeigenClient.saveEvent(event);
    }

    public void updateRequestStatus(Request request, RequestStatus newStatus) {
        String currentStatusName = request.getStatus().getName().name();
        if (currentStatusName.equals(newStatus.name())) {
            throw new ValidationException("Статус уже установлен: " + newStatus);
        }
        request.setStatus(getRequestStatusEntityByRequestStatus(newStatus));
    }
}
