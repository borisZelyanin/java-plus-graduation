package ru.practicum.services.event.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.services.event.model.LocationEntity;
import ru.practicum.services.event.repository.LocationRepository;

@RequiredArgsConstructor
@Component
public class LocationCalc {
    private  final LocationRepository locationRepository;

    public LocationEntity ResolveLocation(LocationEntity requestLocation) {
        LocationEntity mayBeExistingLocation = null;
        if (requestLocation.getId() == null) {
            mayBeExistingLocation = locationRepository
                    .findByLatAndLon(requestLocation.getLat(), requestLocation.getLon())
                    .orElseGet(() -> locationRepository.save(requestLocation));
        }
        return mayBeExistingLocation;
    }

}
