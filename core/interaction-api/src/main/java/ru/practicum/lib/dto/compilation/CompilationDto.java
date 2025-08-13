package ru.practicum.lib.dto.compilation;

import lombok.Builder;
import lombok.Data;
import ru.practicum.lib.dto.event.EventShortDto;


import java.util.List;

@Data
@Builder
public class CompilationDto {
    private Long id;
    private String title;
    private boolean pinned;
    private List<EventShortDto> events;
}
