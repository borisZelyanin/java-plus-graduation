package ru.practicum.services.event.support;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.lib.enums.EventState;
import ru.practicum.lib.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@Component
public class EventControllerHelperBean {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "id");
    private static final EnumSet<EventState> DEFAULT_STATES =
            EnumSet.of(EventState.PUBLISHED, EventState.CANCELED, EventState.PENDING);

    public LocalDateTime defaultStart(LocalDateTime rangeStart) {
        return rangeStart != null ? rangeStart : LocalDateTime.now();
    }

    public LocalDateTime defaultEnd(LocalDateTime rangeEnd) {
        return rangeEnd != null ? rangeEnd : LocalDateTime.now().plusYears(100);
    }

    public void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException("Временной промежуток задан неверно");
        }
    }

    public PageRequest toPageRequest(int from, int size) {
        int pageIndex = Math.floorDiv(from, size);
        return PageRequest.of(pageIndex, size, DEFAULT_SORT);
    }

    public List<EventState> parseStatesOrDefault(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.copyOf(DEFAULT_STATES);
        }
        try {
            return raw.stream()
                    .map(s -> EventState.valueOf(s.trim().toUpperCase(Locale.ROOT)))
                    .distinct()
                    .toList();
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Недопустимое значение статуса в запросе");
        }
    }
}