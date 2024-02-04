package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.extraExceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.InMemoryUserStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto addUser(UserDto user) {
        log.info("Добавление нового пользователя: {}", user);
        if (user.getEmail() == null) {
            throw new ValidationException("Email не может быть пустым.");
        }
        User resultUser = userStorage.addUser(UserDtoMapper.dtoToUser(user));
        return UserDtoMapper.userToDto(resultUser);
    }

    @Override
    public UserDto updateUser(Integer userId, UserDto user) {
        log.info("Обновление пользователя: {}", user);
        if (userId < 0) {
            throw new UserNotFoundException("Id пользователя должен быть неотрицательным.");
        }
        User resultUser = userStorage.updateUser(userId, UserDtoMapper.dtoToUser(user));
        return UserDtoMapper.userToDto(resultUser);
    }

    @Override
    public UserDto getUserById(Integer userId) {
        log.info("Получение пользователя по id: {}", userId);
        if (userId < 0) {
            throw new UserNotFoundException("Id пользователя должен быть неотрицательным.");
        }
        User resultUser = userStorage.getUserById(userId);
        return UserDtoMapper.userToDto(resultUser);
    }

    @Override
    public List<UserDto> getUsers() {
        log.info("Получение списка всех пользователей.");
        List<User> usersList = userStorage.getUsers();
        List<UserDto> resultList = new ArrayList<>();
        for (User user : usersList) {
            resultList.add(UserDtoMapper.userToDto(user));
        }
        return resultList;
    }

    @Override
    public void deleteUser(Integer userId) {
        log.info("Удаление пользователя c id:" + userId);
        if (userId < 0) {
            throw new UserNotFoundException("Id пользователя должен быть неотрицательным.");
        }
        userStorage.deleteUser(userId);
    }
}
