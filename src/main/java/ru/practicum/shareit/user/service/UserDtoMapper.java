package ru.practicum.shareit.user.service;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class UserDtoMapper {
    public User dtoToUser(UserDto userDto) {
        return new User(null, userDto.getName(), userDto.getEmail());
    }

    public UserDto userToDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
