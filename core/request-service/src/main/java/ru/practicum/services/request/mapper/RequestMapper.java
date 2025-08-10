package ru.practicum.services.request.mapper;


import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.services.request.model.Request;


public class RequestMapper {

    public static ParticipationRequestDto toRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequester())
                .event(request.getEvent())
                .status(request.getStatus().getName())
                .created(request.getCreated())
                .build();
    }
}
