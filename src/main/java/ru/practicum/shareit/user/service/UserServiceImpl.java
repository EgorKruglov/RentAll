package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.extraExceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDto addUser(UserDto user) {
        log.info("Добавление нового пользователя: {}", user);
        if (user.getEmail() == null) {
            throw new ValidationException("Email не может быть пустым.");
        }
        User resultUser = userRepository.save(UserDtoMapper.dtoToUser(user));
        return UserDtoMapper.userToDto(resultUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Integer userId, UserDto user) {
        log.info("Обновление пользователя: {}", user);
        if (userId < 0) {
            throw new UserNotFoundException("Id пользователя должен быть неотрицательным.");
        }
        User resultUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                            return new UserNotFoundException("Пользователя с " + userId + " не существует");
                        }
                );
        String name = user.getName();
        if (name != null && !name.isBlank()) {
            resultUser.setName(name);
        }
        String email = user.getEmail();
        if (email != null && !email.isBlank()) {
            resultUser.setEmail(email);
        }
        return UserDtoMapper.userToDto(resultUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Integer userId) {
        log.info("Получение пользователя по id: {}", userId);
        if (userId < 0) {
            throw new UserNotFoundException("Id пользователя должен быть неотрицательным.");
        }
        User resultUser = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден")
        );
        return UserDtoMapper.userToDto(resultUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        log.info("Получение списка всех пользователей.");
        List<User> usersList = userRepository.findAll();
        List<UserDto> resultList = new ArrayList<>();
        for (User user : usersList) {
            resultList.add(UserDtoMapper.userToDto(user));
        }
        return resultList;
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        log.info("Удаление пользователя c id:" + userId);
        if (userId < 0) {
            throw new UserNotFoundException("Id пользователя должен быть неотрицательным.");
        }
        userRepository.deleteById(userId);
    }
}
