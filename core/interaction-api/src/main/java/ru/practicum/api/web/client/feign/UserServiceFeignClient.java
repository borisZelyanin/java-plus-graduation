package ru.practicum.api.web.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.lib.dto.user.UserDto;

import java.util.List;

@FeignClient(
        name = "user-service",          // имя сервиса из spring.application.name
        path = "/admin/users"           // общий префикс контроллера
)
public interface UserServiceFeignClient {

    @PostMapping
    UserDto createUser(@RequestBody UserDto userDto);

    @GetMapping
    List<UserDto> getAllUsers(
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size
    );

    @GetMapping(params = "ids")
    List<UserDto> getUserById(@RequestParam("ids") Long ids);


    @DeleteMapping("/{userId}")
    void deleteUser(@PathVariable("userId") Long userId);
}