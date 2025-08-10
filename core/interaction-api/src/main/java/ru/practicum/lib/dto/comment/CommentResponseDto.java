package ru.practicum.lib.dto.comment;

import lombok.Builder;
import lombok.Data;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.event.EventShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponseDto {
    private Long id;
    private String text;
    private String authorName;
    private EventFullDto event;
    private LocalDateTime created;
}
