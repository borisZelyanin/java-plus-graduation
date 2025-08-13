package ru.practicum.api.web.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.lib.dto.request.UpdateRequestsStatusDto;

import java.util.List;

@FeignClient(
        name = "request-service",
        path = "/"
)
public interface RequestServiceFeignClient {
    @GetMapping("/{userId}/requests")
    List<ParticipationRequestDto> getUserRequests(@PathVariable("userId") Long userId);

    @GetMapping("/users/{userId}/requests/{eventId}")
    ParticipationRequestDto getParticipationRequest(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    );

    @PostMapping("/{userId}/requests")
    ParticipationRequestDto createParticipationRequest(@PathVariable("userId") Long userId,
                                                       @RequestParam("eventId") Long eventId);

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelParticipationRequest(@PathVariable("userId") Long userId,
                                                       @PathVariable("requestId") Long requestId);

    @GetMapping("/admin/events/{eventId}/requests")
    List<ParticipationRequestDto> getByEvent(@PathVariable("eventId") Long eventId);

    @GetMapping("/admin/users/requests")
    List<ParticipationRequestDto> getByUsers(@RequestParam("userIds") List<Long> userIds);

    @PostMapping(value = "/admin/batch", consumes = "application/json", produces = "application/json")
    List<ParticipationRequestDto> saveBatch(@RequestBody List<ParticipationRequestDto> requests);

    @PostMapping(path = "/admin/status", consumes = "application/json", produces = "application/json")
    List<ParticipationRequestDto> updateStatuses(@RequestBody UpdateRequestsStatusDto body);
}
