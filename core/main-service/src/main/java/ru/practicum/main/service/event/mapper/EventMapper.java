package ru.practicum.main.service.event.mapper;

import ru.practicum.main.service.category.dto.CategoryDto;
import ru.practicum.main.service.category.model.Category;
import ru.practicum.main.service.event.dto.EventFullDto;
import ru.practicum.main.service.event.dto.EventShortDto;
import ru.practicum.main.service.event.dto.NewEventDto;
import ru.practicum.main.service.event.model.Event;
import ru.practicum.main.service.event.model.EventState;
import ru.practicum.main.service.user.dto.UserDto;
import ru.practicum.main.service.user.model.User;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEvent(NewEventDto newEventDto,
                                User initiator,
                                Category category) {

        return Event.builder()
                .initiator(initiator)
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .confirmedRequests(0)
                .build();
    }

    public static EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(new CategoryDto(
                        event.getCategory().getId(),
                        event.getCategory().getName()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(new UserDto(
                        event.getInitiator().getId(),
                        event.getInitiator().getName(),
                        "email"))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(0L)
                .build();
    }

    public static EventFullDto toFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(new CategoryDto(
                        event.getCategory().getId(),
                        event.getCategory().getName()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(new UserDto(
                        event.getInitiator().getId(),
                        event.getInitiator().getName(),
                        "email"))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(0L)
                .build();
    }
}
