package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

public class UserDtoMapper {
    public static User dtoToUser(UserDto userDto) {
        return new User(null, userDto.getName(), userDto.getEmail());
    }

    public static UserDto userToDto (User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
