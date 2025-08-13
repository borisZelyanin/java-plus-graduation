package ru.practicum.services.request.utils;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.api.web.client.feign.AdminEventFeignClient;
import ru.practicum.api.web.client.feign.UserServiceFeignClient;
import ru.practicum.lib.dto.category.CategoryDto;
import ru.practicum.lib.dto.event.EventFullDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.lib.exception.ValidationException;

@Component
@RequiredArgsConstructor
public class FeigenClient {

    private  final UserServiceFeignClient UserServiceFeignClient;
    private  final AdminEventFeignClient AdminEventFeignClient;
    public  UserDto getUserById(Long userId) {
        try {
            return UserServiceFeignClient.getUserById(userId).getFirst();
        }
        catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователя с id = " + userId + " нет.");
        }
    }

    public EventFullDto getEventById(Long eventId) {
        try {
            return AdminEventFeignClient.getEventById(eventId);
        } catch (FeignException.NotFound e) {
            throw new ValidationException("Указан неправильный ID категории: " + eventId);
        }
    }

    public  CategoryDto getCategoryById(Long categoryId) {
        try {
            return AdminEventFeignClient.getCategoryById(categoryId);
        } catch (FeignException.NotFound e) {
            throw new ValidationException("Указан неправильный ID категории: " + categoryId);
        }
    }

    public  EventFullDto saveEvent(EventFullDto event) {
        try {
            return AdminEventFeignClient.save(event);
        } catch (FeignException.NotFound e) {
            throw new ValidationException("Указан неправильный ID категории: " + event);
        }
    }

}
