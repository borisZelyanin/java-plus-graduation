package ru.practicum.services.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.lib.dto.compilation.CompilationDto;
import ru.practicum.services.event.service.CompilationService;


import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        return compilationService.getCompilationById(compId);
    }
}
