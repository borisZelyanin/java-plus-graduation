package ru.practicum.services.event.service;



import ru.practicum.lib.dto.compilation.CompilationDto;
import ru.practicum.lib.dto.compilation.NewCompilationDto;
import ru.practicum.lib.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    void deleteCompilation(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long compId);
}
