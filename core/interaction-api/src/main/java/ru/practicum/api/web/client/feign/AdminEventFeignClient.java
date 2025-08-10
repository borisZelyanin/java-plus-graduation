package ru.practicum.api.web.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.lib.dto.category.CategoryDto;
import ru.practicum.lib.dto.category.CategoryUpdateDto;
import ru.practicum.lib.dto.event.EventFullDto;

import java.util.List;

@FeignClient(
        name = "event-service",
        path = "/"
)
public interface AdminEventFeignClient {

    @GetMapping("/admin/events/{eventId}")
    EventFullDto getEventById(@PathVariable("eventId") Long eventId);

    @PostMapping(path = "/admin/events", consumes = "application/json")
    EventFullDto save(@RequestBody EventFullDto event);


    @PostMapping("/admin/categories")
    ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto dto);

    @DeleteMapping("/admin/categories/{id}")
    ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id);

    @PatchMapping("/admin/categories/{catId}")
    CategoryDto updateCategory(@PathVariable("catId") Long catId,
                               @RequestBody CategoryUpdateDto dto);

    // ----- PUBLIC -----

    @GetMapping("/categories/{id}")
    CategoryDto getCategoryById(@PathVariable("id") Long id);

    @GetMapping("/categories")
    ResponseEntity<List<CategoryDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );
}