package ru.practicum.main.service.category.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.main.service.validation.NotOnlySpaces;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    @Size(min = 1, max = 50, message = "Категория должна быть от 1 до 50 символов")
    @NotOnlySpaces
    private String name;
}
