package ru.practicum.services.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.EventShortDto;
import ru.practicum.lib.dto.event.SearchPublicEventsParamDto;

import java.util.List;

public interface PublicEventService {
    List<EventShortDto> searchPublicEvents(SearchPublicEventsParamDto searchPublicEventsParamDto);
    EventFullDto getPublicEvent(Long eventId, HttpServletRequest request);
    List<EventFullDto> getRecommendationsForUser(long userId, int maxResult);
    void checkUserRegistrationAtEvent(long userId, long eventId);
}