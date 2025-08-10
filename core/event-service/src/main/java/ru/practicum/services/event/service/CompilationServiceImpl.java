package ru.practicum.services.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.lib.dto.compilation.CompilationDto;
import ru.practicum.lib.dto.compilation.NewCompilationDto;
import ru.practicum.lib.dto.compilation.UpdateCompilationRequest;
import ru.practicum.lib.dto.event.EventShortDto;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.services.event.mapper.CompilationMapper;
import ru.practicum.services.event.mapper.EventMapper;
import ru.practicum.services.event.model.Compilation;
import ru.practicum.services.event.model.Event;
import ru.practicum.services.event.repository.CompilationRepository;
import ru.practicum.services.event.repository.EventRepository;
import ru.practicum.services.event.utils.FeigenClient;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final FeigenClient feigenClient;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Set<Event> events = dto.getEvents() == null
                ? Collections.emptySet()
                : new HashSet<>(eventRepository.findAllById(dto.getEvents()));

        Compilation compilation = CompilationMapper.toEntity(dto, events);
        Compilation saved = compilationRepository.save(compilation);

        return CompilationMapper.toDto(saved,getEventDtos(compilation));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        if (updateRequest.getEvents() != null) {
            Set<Event> updatedEvents = new HashSet<>(eventRepository.findAllById(updateRequest.getEvents()));
            compilation.setEvents(updatedEvents);
        }

        Compilation updated = compilationRepository.save(compilation);

        return CompilationMapper.toDto(updated,getEventDtos(compilation));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с id=" + compId + " не найдена");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Compilation> all = compilationRepository.findAll(page).getContent();

        return all.stream()
                .filter(c -> pinned == null || c.isPinned() == pinned)
                .map( comp -> CompilationMapper.toDto(comp,getEventDtos(comp)))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
        return CompilationMapper.toDto(compilation,getEventDtos(compilation));
    }

    private  List<EventShortDto> getEventDtos(Compilation compilation) {
        return  compilation.getEvents().stream()
                .map( event -> {
                    var user = feigenClient.getUserById(event.getInitiator());
                    return EventMapper.toShortDto(event, user);
                })
                .collect(Collectors.toList());
    }
}