package ru.practicum.main.service.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.service.category.dto.CategoryDto;
import ru.practicum.main.service.category.dto.CategoryUpdateDto;
import ru.practicum.main.service.category.mapper.CategoryMapper;
import ru.practicum.main.service.category.model.Category;
import ru.practicum.main.service.category.repository.CategoryRepository;
import ru.practicum.main.service.exceptions.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = CategoryMapper.toEntity(categoryDto);
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto update(Long categoryId, CategoryUpdateDto dto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена: " + categoryId));

        category.setName(dto.getName());

        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getAllPaged(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        return CategoryMapper.toDto(category);
    }

    @Override
    public void deleteById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        categoryRepository.deleteById(categoryId);
    }

}
