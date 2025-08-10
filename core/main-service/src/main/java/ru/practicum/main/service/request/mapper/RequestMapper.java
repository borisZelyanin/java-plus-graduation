package ru.practicum.main.service.request.mapper;

import ru.practicum.main.service.request.dto.ParticipationRequestDto;
import ru.practicum.main.service.request.model.Request;

public class RequestMapper {

    public static ParticipationRequestDto toRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequester().getId())
                .event(request.getEvent().getId())
                .status(request.getStatus().getName())
                .created(request.getCreated())
                .build();
    }
}
