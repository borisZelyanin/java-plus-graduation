package ru.practicum.services.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.lib.dto.request.UpdateRequestsStatusDto;
import ru.practicum.services.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/admin", produces = "application/json")
@RequiredArgsConstructor
@Validated
public class RequestPrivateController {

    private final RequestService requestService;

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventByRequests(
            @PathVariable @Positive Long eventId
    ) {
        log.info("Запрос заявок по событию: eventId={}", eventId);
        return ResponseEntity.ok(requestService.getParticipationEvent(eventId));
    }

    @GetMapping("/users/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getUsersByRequests(
            @RequestParam("userIds") @NotEmpty List<@Positive Long> userIds
    ) {
        log.info("Запрос заявок по пользователям: userIds={}", userIds);
        return ResponseEntity.ok(requestService.getUsersByRequests(userIds));
    }

    @PostMapping(path = "/batch", consumes = "application/json")
    public ResponseEntity<List<ParticipationRequestDto>> saveBatch(
            @RequestBody @NotEmpty List<@Valid ParticipationRequestDto> requests
    ) {
        log.info("Батч‑сохранение заявок: count={}", requests.size());
        var saved = requestService.saveBatch(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping(path = "/status", consumes = "application/json")
    public ResponseEntity<List<ParticipationRequestDto>> updateStatuses(
            @RequestBody @Valid UpdateRequestsStatusDto body
    ) {
        log.info("Обновление статусов: status={}, count={}", body.getStatus(), body.getRequests().size());
        // Лучше пусть сервис сам проставляет статус всем заявкам и сохраняет:
        var updated = requestService.updateStatusesForRequests(body.getRequests(), body.getStatus());
        return ResponseEntity.ok(updated);
    }
}
