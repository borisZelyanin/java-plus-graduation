package ru.practicum.services.event.service;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.EventShortDto;
import ru.practicum.lib.dto.event.NewEventDto;
import ru.practicum.lib.dto.event.UpdateEventUserRequest;
import ru.practicum.lib.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.lib.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface PrivateEventService {
    List<EventShortDto> getUserEvents(Long userId, Pageable pageable);
    EventFullDto createEvent(Long userId, NewEventDto dto);
    EventFullDto getUserEventById(Long userId, Long eventId);
    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto);
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);
    Map<String, List<ParticipationRequestDto>> approveRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}