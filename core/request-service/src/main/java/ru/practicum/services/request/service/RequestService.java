package ru.practicum.services.request.service;


import ru.practicum.lib.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createParticipationRequest(Long userId,
                                                       Long eventId);

    ParticipationRequestDto cancelParticipationRequest(Long userId,
                                                       Long requestId);

    ParticipationRequestDto getParticipationRequest(Long userId, Long requestId);
}
