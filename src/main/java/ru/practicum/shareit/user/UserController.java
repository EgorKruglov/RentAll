package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.append(error.getDefaultMessage()).append("\n")
            );
            throw new ValidationException("Ошибка валидации пользователя: " + errors);
        }
        UserDto resultUser = userService.addUser(user);
        log.info("Пользователь добавлен id:" + user.getId());
        return resultUser;
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@Valid @RequestBody UserDto user,
                           @PathVariable Integer userId,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.append(error.getDefaultMessage()).append("\n")
            );
            throw new ValidationException("Ошибка валидации пользователя: " + errors);
        }
        UserDto resultUser = userService.updateUser(userId, user);
        log.info("Данные пользователя обновлены id:" + userId);
        return resultUser;
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Integer userId) {
        UserDto user = userService.getUserById(userId);
        log.info("Отправлена информация о пользователе id:" + userId);
        return user;
    }

    @GetMapping
    public List<UserDto> getUsers() {
        List<UserDto> users = userService.getUsers();
        log.info("Отправлен список всех пользователей");
        return users;
    }

    @DeleteMapping("/{userId}")
    public Map<String, String> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        log.info("Удалён пользователь id:" + userId);
        return Map.of("message", "Удалён пользователь id:" + userId);
    }
}
