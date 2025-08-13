package ru.practicum.services.request.mapper;


import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.services.request.model.Request;
import ru.practicum.services.request.model.RequestStatusEntity;

import java.util.Map;


public class RequestMapper {

    public static ParticipationRequestDto toRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequester())
                .event(request.getEvent())
                .status(request.getStatus() != null ? request.getStatus().getName() : null)
                .created(request.getCreated())
                .build();
    }

    public static Request toEntity(ParticipationRequestDto dto) {
        Request request = new Request();
        request.setId(dto.getId());
        request.setRequester(dto.getRequester());
        request.setEvent(dto.getEvent());
        request.setCreated(dto.getCreated());
        // status не трогаем — установим в сервисе
        return request;
    }

    public static Request toEntity(ParticipationRequestDto dto, RequestStatusEntity statusEntity) {
        Request request = toEntity(dto);
        request.setStatus(statusEntity);
        return request;
    }
}
