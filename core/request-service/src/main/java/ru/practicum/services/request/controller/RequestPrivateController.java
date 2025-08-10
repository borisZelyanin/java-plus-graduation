package ru.practicum.services.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.services.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events/")
@RequiredArgsConstructor
@Validated
public class RequestPrivateController {

    private final RequestService requestService;

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(
            @PathVariable @Positive Long eventId) {
        log.info("Запрос на получение всех заявок на участие для события с id = {}", eventId);
        return ResponseEntity.ok(requestService.getParticipationEvent(eventId));
    }
}
