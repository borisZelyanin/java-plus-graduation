package ru.practicum.main.service.compilation.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.main.service.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
public class CompilationDto {
    private Long id;
    private String title;
    private boolean pinned;
    private List<EventShortDto> events;
}
