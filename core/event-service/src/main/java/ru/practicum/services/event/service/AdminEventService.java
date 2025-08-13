package ru.practicum.services.event.service;

import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.SearchAdminEventsParamDto;
import ru.practicum.lib.dto.event.UpdateEventAdminRequest;

import java.util.List;

public interface AdminEventService {
    List<EventFullDto> searchEventsByAdmin(SearchAdminEventsParamDto params);
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);
    EventFullDto getEventById(Long eventId);
    EventFullDto saveForce(EventFullDto event);
}