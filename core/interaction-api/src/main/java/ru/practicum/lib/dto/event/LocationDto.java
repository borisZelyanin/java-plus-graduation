package ru.practicum.lib.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class LocationDto {
    private Long id;
    @NotNull
    private float lat;
    @NotNull
    private float lon;
}
