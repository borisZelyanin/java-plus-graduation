package ru.practicum.services.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.lib.dto.category.CategoryDto;
import ru.practicum.lib.dto.category.CategoryUpdateDto;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.services.event.repository.CategoryRepository;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        ru.practicum.services.event.model.Category category = ru.practicum.services.event.mapper.CategoryMapper.toEntity(categoryDto);
        return ru.practicum.services.event.mapper.CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto update(Long categoryId, CategoryUpdateDto dto) {
        ru.practicum.services.event.model.Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена: " + categoryId));

        category.setName(dto.getName());

        return ru.practicum.services.event.mapper.CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(ru.practicum.services.event.mapper.CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getAllPaged(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(ru.practicum.services.event.mapper.CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long categoryId) {
        ru.practicum.services.event.model.Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        return ru.practicum.services.event.mapper.CategoryMapper.toDto(category);
    }

    @Override
    public void deleteById(Long categoryId) {
        ru.practicum.services.event.model.Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        categoryRepository.deleteById(categoryId);
    }

}
