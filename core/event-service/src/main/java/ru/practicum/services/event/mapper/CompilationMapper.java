package ru.practicum.services.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.lib.dto.compilation.CompilationDto;
import ru.practicum.lib.dto.compilation.NewCompilationDto;
import ru.practicum.lib.dto.event.EventShortDto;
import ru.practicum.services.event.model.Compilation;
import ru.practicum.services.event.model.Event;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public CompilationDto toDto(Compilation compilation ,List<EventShortDto> eventDtos  ) {
          return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(eventDtos)
                .build();
    }

    public Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.isPinned())
                .events(events)
                .build();
    }
}