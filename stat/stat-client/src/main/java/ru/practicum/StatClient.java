package ru.practicum;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.util.List;

@FeignClient(name = "stats-server", path = "/", configuration = FeignConfig.class)
public interface StatClient {

    @PostMapping("/hit")
    void saveStatEvent(@RequestBody EndpointHitDto endpointHitDto);

    @GetMapping("/stats")
    List<ViewStatsDto> getStats(@RequestParam String start,
                                @RequestParam String end,
                                @RequestParam(required = false) List<String> uris,
                                @RequestParam boolean unique);
}