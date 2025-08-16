package ru.practicum.services.event.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.api.web.client.feign.RequestServiceFeignClient;
import ru.practicum.api.web.client.feign.UserServiceFeignClient;
import ru.practicum.lib.dto.request.ParticipationRequestDto;
import ru.practicum.lib.dto.request.UpdateRequestsStatusDto;
import ru.practicum.lib.dto.user.UserDto;
import ru.practicum.lib.exception.NotFoundException;
import ru.practicum.lib.exception.ValidationException;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeigenClient {

    private final UserServiceFeignClient userServiceFeignClient;
    private final RequestServiceFeignClient requestServiceFeignClient;
    private final ObjectMapper objectMapper;

    public  UserDto getUserById(Long userId) {
        try {
            return userServiceFeignClient.getUserById(userId).getFirst();
        }
        catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователя с id = " + userId + " нет.");
        }
    }

    public List<ParticipationRequestDto> getRequestByEvent(Long eventId) {
        try {
            return requestServiceFeignClient.getByEvent( eventId);
        }
        catch (FeignException.NotFound e) {
            throw new NotFoundException(" event = " + eventId + " нет.");
        }
    }

    public List<ParticipationRequestDto> getRequestByUser(List<Long> userId) {
        try {
            return requestServiceFeignClient.getByUsers(userId);
        }
        catch (FeignException.NotFound e) {
            throw new NotFoundException(" event = " + userId + " нет.");
        }
    }


    public List<UserDto> getUserListById(List<Long> userIds) {
        try {
            return userServiceFeignClient.getUserListById(userIds);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователи с id " + userIds + " не найдены.");
        } catch (FeignException e) {
            throw new RuntimeException("Ошибка при обращении к user-service: " + e.getMessage(), e);
        }
    }


    public Optional<ParticipationRequestDto> getParticipationRequest(Long userId, Long eventId) {
        try {
            ParticipationRequestDto dto = requestServiceFeignClient.getParticipationRequest(userId, eventId);
            return Optional.ofNullable(dto);
        } catch (FeignException.NotFound e) {
            return Optional.empty(); // ✅ нет заявки — пустой Optional
        } catch (FeignException e) {
            throw new RuntimeException(
                    "Ошибка при запросе заявки пользователя id=" + userId + " на событие id=" + eventId + ": " + e.getMessage(),
                    e
            );
        }
    }

    public List<ParticipationRequestDto> saveBatchRequests(List<ParticipationRequestDto> requests) {
        try {
            return requestServiceFeignClient.saveBatch(requests);
        } catch (FeignException.BadRequest e) {
            throw new ValidationException(
                    "Ошибка валидации при батч-сохранении заявок: " + e.contentUTF8()
            );
        } catch (FeignException.NotFound e) {
            throw new NotFoundException(
                    "Не удалось сохранить заявки — сервис не найден."
            );
        } catch (FeignException e) {
            throw new RuntimeException(
                    "Ошибка при батч-сохранении заявок: " + e.contentUTF8()
            );
        }
    }

    public void updateBatchRequestStatuses(UpdateRequestsStatusDto body) {
        try {
            // Логируем тело запроса в JSON
            try {
                log.info("📤 Sending updateBatchRequestStatuses request: {}",
                        objectMapper.writeValueAsString(body));
            } catch (JsonProcessingException e) {
                log.warn("Не удалось сериализовать тело для лога", e);
            }
            requestServiceFeignClient.updateStatuses(body);
        } catch (FeignException.BadRequest e) {
            log.error("❌ Validation error response: {}", e.contentUTF8());

        } catch (FeignException.NotFound e) {
            log.error("❌ Not found response: {}", e.contentUTF8());

        } catch (FeignException e) {
            log.error("❌ General Feign error: {}", e.contentUTF8());
        }
    }

}
