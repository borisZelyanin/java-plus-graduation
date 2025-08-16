package ru.practicum.lib.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.lib.dto.category.CategoryDto;
import ru.practicum.lib.dto.user.UserDto;


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
    private double rating;
}
