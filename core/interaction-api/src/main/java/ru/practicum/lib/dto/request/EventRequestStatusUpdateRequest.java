package ru.practicum.lib.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.lib.enums.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    @NotNull
    List<Long> requestIds;
    @NotNull
    RequestStatus status;
}
