package ru.practicum.services.request.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.lib.exception.ValidationException;

@Configuration
public class FeignErrorConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 400 -> new ValidationException("Некорректные данные для Event");
            case 404 -> new NotFoundException("Событие не найдено");
            default -> new RuntimeException("Ошибка event-service: " + response.status());
        };
    }
}