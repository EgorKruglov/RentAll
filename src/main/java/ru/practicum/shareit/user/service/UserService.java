package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto user);

    UserDto updateUser(Integer userId, UserDto user);

    UserDto getUserById(Integer userId);

    List<UserDto> getUsers();

    void deleteUser(Integer userId);
}
