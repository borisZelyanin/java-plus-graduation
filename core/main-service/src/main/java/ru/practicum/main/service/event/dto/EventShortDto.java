package ru.practicum.main.service.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.main.service.category.dto.CategoryDto;
import ru.practicum.main.service.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventShortDto {
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Long id;
    private UserDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
