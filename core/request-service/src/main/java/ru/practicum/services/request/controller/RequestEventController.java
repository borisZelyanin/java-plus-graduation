package ru.practicum.services.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.services.request.service.RequestService;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class RequestEventController {

    private final RequestService requestService;
    @GetMapping("/{eventId}/requests")
    public ResponseEntity<ParticipationRequestDto> GetParticipationRequest(@PathVariable @Positive Long userId,
                                                                           @PathVariable @Positive Long eventId) {
        log.info("Запрос на отмену заявки на участие с id = {}", eventId);
        return ResponseEntity.ok(requestService.getParticipationRequest(userId, eventId));
    }


}
