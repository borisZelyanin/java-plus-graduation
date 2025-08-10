package ru.practicum.lib.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.lib.enums.RequestStatus;

import java.util.List;

@Data
public class UpdateRequestsStatusDto {

    @NotNull
    private RequestStatus status;

    @NotEmpty
    private List<@Valid ParticipationRequestDto> requests;
}