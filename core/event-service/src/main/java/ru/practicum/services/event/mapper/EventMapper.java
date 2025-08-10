package ru.practicum.services.event.mapper;

import ru.practicum.lib.dto.category.CategoryDto;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.EventShortDto;
import ru.practicum.lib.dto.event.NewEventDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.services.event.model.Category;
import ru.practicum.services.event.model.Event;
import ru.practicum.lib.enums.EventState;
import ru.practicum.services.event.utils.FeigenClient;
import ru.practicum.services.event.utils.LocationCalc;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEvent(NewEventDto newEventDto,
                                Long initiator,
                                Category category) {

        return Event.builder()
                .initiator(initiator)
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(LocationMapper.toEntity(newEventDto.getLocation()))
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .confirmedRequests(0)
                .build();
    }

    public static Event toEventFull(EventFullDto event) {

        return Event.builder()
                .initiator(event.getInitiator().getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toEntity(event.getCategory()))
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .location(LocationMapper.toEntity(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .title(event.getTitle())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .confirmedRequests(0)
                .build();
    }
    public static EventShortDto toShortDto(Event event, UserDto user ) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(user)
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(0L)
                .build();
    }

    public static EventFullDto toFullDto(Event event,  UserDto user ) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(user)
                .location(LocationMapper.toDto(event.getLocation()))
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
