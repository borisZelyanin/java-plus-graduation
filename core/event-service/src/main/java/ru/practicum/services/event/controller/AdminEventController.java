package ru.practicum.services.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.NewEventDto;
import ru.practicum.lib.dto.event.SearchAdminEventsParamDto;
import ru.practicum.lib.dto.event.UpdateEventAdminRequest;
import ru.practicum.lib.enums.EventState;
import ru.practicum.services.event.model.Event;
import ru.practicum.services.event.service.AdminEventService;
import ru.practicum.services.event.support.EventControllerHelperBean;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final AdminEventService eventService;
    private final EventControllerHelperBean helper;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> searchEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> stateStrings,
            @RequestParam(required = false) List<Long> categoriesIds,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {

        LocalDateTime start = helper.defaultStart(rangeStart);
        LocalDateTime end = helper.defaultEnd(rangeEnd);
        helper.validateTimeRange(start, end);

        PageRequest pageRequest = helper.toPageRequest(from, size);
        List<EventState> states = helper.parseStatesOrDefault(stateStrings);

        log.info("Админ поиск события: users={}, states={}, categoriesIds={}, rangeStart={}, rangeEnd={}",
                users, states, categoriesIds, start, end);

        SearchAdminEventsParamDto searchParams = SearchAdminEventsParamDto.builder()
                .users(users)
                .eventStates(states)
                .categoriesIds(categoriesIds)
                .rangeStart(start)
                .rangeEnd(end)
                .pageRequest(pageRequest)
                .build();

        return ResponseEntity.ok(eventService.searchEventsByAdmin(searchParams));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @PostMapping
    public ResponseEntity<EventFullDto> saveEeventFull(@RequestBody EventFullDto event) {
        EventFullDto saved = eventService.saveForce(event);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventByAdmin(
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Редактирование события id={} с изменениями: {}", eventId, updateEventAdminRequest);
        return ResponseEntity.ok(eventService.updateEventByAdmin(eventId, updateEventAdminRequest));
    }
}