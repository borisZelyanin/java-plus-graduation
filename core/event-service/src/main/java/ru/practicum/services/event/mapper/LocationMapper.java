package ru.practicum.services.event.mapper;

import ru.practicum.lib.dto.event.LocationDto;
import ru.practicum.services.event.model.LocationEntity;

public class LocationMapper {

    public static LocationDto toDto(LocationEntity entity) {
        if (entity == null) {
            return null;
        }
        return LocationDto.builder()
                .id(entity.getId())
                .lat(entity.getLat())
                .lon(entity.getLon())
                .build();
    }

    public static LocationEntity toEntity(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        LocationEntity entity = new LocationEntity();
        entity.setId(dto.getId());
        entity.setLat(dto.getLat());
        entity.setLon(dto.getLon());
        return entity;
    }
}