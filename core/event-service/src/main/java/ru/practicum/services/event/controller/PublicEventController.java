package ru.practicum.services.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.wrap.grpc.client.stats.CollectorClient;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.EventShortDto;
import ru.practicum.lib.dto.event.SearchPublicEventsParamDto;
import ru.practicum.lib.exception.ValidationException;
import ru.practicum.lib.enums.EventSort;
import ru.practicum.services.event.service.PublicEventService;


import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_TEXT = "";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_START = 0;
    private final PublicEventService eventService;
    private final CollectorClient collectorClient;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> searchPublicEvents(
            @RequestParam(defaultValue = DEFAULT_TEXT) String text,
            @RequestParam(required = false) List<Long> categoriesIds,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) EventSort eventSort,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_START) @PositiveOrZero int from,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) @Positive int size,
            HttpServletRequest request) {
        log.info("Запрос на получение опубликованных событий: text='{}', " +
                        "categoriesIds={}, paid={}, start={}, end={}, onlyAvailable={}, eventSort={}",
                text, categoriesIds, paid, rangeStart, rangeEnd, onlyAvailable, eventSort);
        validateTimeRange(rangeStart, rangeEnd);
        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);

        PageRequest pageRequest = createPageRequest(from, size, eventSort);
        SearchPublicEventsParamDto searchPublicEventsParamDto =
                SearchPublicEventsParamDto.builder().text(text)
                        .categoriesIds(categoriesIds)
                        .paid(paid)
                        .rangeStart(rangeStart)
                        .rangeEnd(rangeEnd)
                        .onlyAvailable(onlyAvailable)
                        .pageRequest(pageRequest)
                        .build();

        List<EventShortDto> eventShortDtos = eventService.searchPublicEvents(searchPublicEventsParamDto);

        return ResponseEntity.ok(eventShortDtos);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEvent(
            @PathVariable @Positive Long eventId,
            @RequestHeader("X-EWM-USER-ID") long userId,
            HttpServletRequest request) {
        log.info("Запрос на получение опубликованого события с id {}", eventId);
        EventFullDto eventFullDto = eventService.getPublicEvent(eventId, request);
        collectorClient.sendPreviewEvent(userId, eventId);
        return ResponseEntity.ok(eventFullDto);
    }

    @PutMapping("/events/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likeEvent(@RequestHeader("X-EWM-USER-ID") long userId, @PathVariable long eventId) {
        eventService.checkUserRegistrationAtEvent(userId, eventId);
        collectorClient.sendLikeEvent(userId, eventId);
    }

    @GetMapping("/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getRecommendationsForUser(@RequestHeader("X-EWM-USER-ID") long userId, @RequestParam int maxResults) {
        return eventService.getRecommendationsForUser(userId, maxResults);
    }


    private PageRequest createPageRequest(int from, int size, EventSort sort) {
        int page = from / size;
        Sort sorting = (sort == EventSort.VIEWS)
                ? Sort.by(Sort.Direction.DESC, "views")
                : Sort.by(Sort.Direction.ASC, "eventDate");

        return PageRequest.of(page, size, sorting);
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Время начала должно быть до окончания");
        }
    }
}