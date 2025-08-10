package ru.practicum.services.event.utils;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.api.web.client.feign.RequestServiceFeignClient;
import ru.practicum.api.web.client.feign.UserServiceFeignClient;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.exception.NotFoundException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeigenClient {

    private final UserServiceFeignClient UserServiceFeignClient;
    private final RequestServiceFeignClient RequestServiceFeignClient;

    public  UserDto getUserById(Long userId) {
        try {
            return UserServiceFeignClient.getUserById(userId).getFirst();
        }
        catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователя с id = " + userId + " нет.");
        }
    }

    public List<ParticipationRequestDto> getRequestByEvent(Long eventId) {
        try {
            return RequestServiceFeignClient.getByEvent( eventId);
        }
        catch (FeignException.NotFound e) {
            throw new NotFoundException(" event = " + eventId + " нет.");
        }
    }

}
