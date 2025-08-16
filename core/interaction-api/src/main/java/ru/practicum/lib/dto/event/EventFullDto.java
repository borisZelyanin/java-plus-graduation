package ru.practicum.lib.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.lib.dto.category.CategoryDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.enums.EventState;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class EventFullDto {
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Long id;
    private UserDto initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;   // <-- добавили формат

    private Boolean requestModeration;
    private EventState state;
    private String title;
    private double rating;
}