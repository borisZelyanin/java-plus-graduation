package ru.practicum.main.service.category.service;

import ru.practicum.main.service.category.dto.CategoryDto;
import ru.practicum.main.service.category.dto.CategoryUpdateDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);

    List<CategoryDto> getAll();

    CategoryDto getById(Long id);

    void deleteById(Long id);

    List<CategoryDto> getAllPaged(int from, int size);

    CategoryDto update(Long catId, CategoryUpdateDto dto);
}
