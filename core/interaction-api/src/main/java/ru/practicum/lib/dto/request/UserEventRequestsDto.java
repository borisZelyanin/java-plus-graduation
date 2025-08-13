package ru.practicum.lib.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.enums.RequestStatus;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEventRequestsDto {

    private EventFullDto event; // DTO события
    private List<ParticipationRequestDto> requests; // список заявок
    private RequestStatus status; // статус заявок
}