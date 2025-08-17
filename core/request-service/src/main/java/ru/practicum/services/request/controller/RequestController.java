package ru.practicum.services.request.controller;


import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.wrap.grpc.client.stats.CollectorClient;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.services.request.service.RequestService;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {

    private final RequestService requestService;
    private final CollectorClient collectorClient;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable @Positive Long userId) {
        log.info("Запрос на получение всех заявок участия пользователя с id {}", userId);
        return ResponseEntity.ok(requestService.getUserRequests(userId));
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<ParticipationRequestDto> createParticipationRequest(@PathVariable Long userId,
                                                                              @RequestParam Long eventId) {
        log.info("Запрос на создание заявки на участие пользователя с id {} в событии с id {}", userId, eventId);
        ParticipationRequestDto request =
                requestService.createParticipationRequest(userId, eventId);

        // после успешного создания запроса отправляем событие в Collector
        collectorClient.sendRegistrationEvent(userId, eventId);

        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelParticipationRequest(@PathVariable @Positive Long userId,
                                                                              @PathVariable @Positive Long requestId) {
        log.info("Запрос на отмену заявки на участие с id = {}", requestId);
        return ResponseEntity.ok(requestService.cancelParticipationRequest(userId, requestId));
    }


}
