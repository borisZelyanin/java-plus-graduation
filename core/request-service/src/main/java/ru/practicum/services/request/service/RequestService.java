package ru.practicum.services.request.service;


import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.lib.enums.RequestStatus;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createParticipationRequest(Long userId,
                                                       Long eventId);
    ParticipationRequestDto cancelParticipationRequest(Long userId,
                                                       Long requestId);
    ParticipationRequestDto getParticipationRequest(Long userId, Long requestId);

    List<ParticipationRequestDto>  getParticipationEvent(Long eventId);

    List<ParticipationRequestDto> getUsersByRequests(List<Long> userId);

    List<ParticipationRequestDto> saveBatch(List<ParticipationRequestDto> requests);

    List<ParticipationRequestDto> updateStatusesForRequests(List<ParticipationRequestDto> requests, RequestStatus status);

}
