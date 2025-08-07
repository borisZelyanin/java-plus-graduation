package ru.practicum.main.service.user.service;

import ru.practicum.main.service.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    List<UserDto> getAll(int from, int size);

    List<UserDto> getById(Long id);

    void deleteUser(Long userId);
}
