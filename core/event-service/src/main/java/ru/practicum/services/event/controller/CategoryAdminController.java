package ru.practicum.services.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.lib.dto.category.CategoryDto;
import ru.practicum.lib.dto.category.CategoryUpdateDto;
import ru.practicum.services.event.service.CategoryService;


@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryDto dto) {
        return new ResponseEntity<>(categoryService.createCategory(dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Valid @PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(
            @PathVariable Long catId,
            @RequestBody @Valid CategoryUpdateDto dto
    ) {
        return categoryService.update(catId, dto);
    }
}

