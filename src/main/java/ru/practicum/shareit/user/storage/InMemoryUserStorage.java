package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.extraExceptions.ObjectAdditionException;
import ru.practicum.shareit.exceptions.extraExceptions.StorageException;
import ru.practicum.shareit.exceptions.extraExceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
public class InMemoryUserStorage implements UserStorage {
    HashMap<Integer, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        for (User savedUser : users.values()) {  // Проверка уникальности email
            if (savedUser.getEmail().equals(user.getEmail())) {
                throw new ObjectAdditionException("Пользователь с email " + user.getEmail() + " уже существует.");
            }
        }
        user.setId(UserIdTicker.getNewId());
        if (users.containsKey(user.getId())) {
            throw new StorageException("Ошибка генерации id. Пользователь с id:" + user.getId() + " уже существует.");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(Integer userId, User user) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь c id:" + userId + " не найден.");
        }
        if (user.getEmail() != null) {
            for (User savedUser : users.values()) {  // Проверка уникальности email
                if (savedUser.getEmail().equals(user.getEmail()) && !Objects.equals(savedUser.getId(), userId)) {
                    throw new ObjectAdditionException("Пользователь с email " + user.getEmail() + " уже существует.");
                }
            }
        }
        if (user.getEmail() == null) {  // Если не была передана почта
            user.setEmail(users.get(userId).getEmail());
        }
        if (user.getName() == null) {  // Если не было передано имя
            user.setName(users.get(userId).getName());
        }
        user.setId(userId);
        users.put(userId, user);
        return user;
    }

    @Override
    public User getUserById(Integer userId) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь c id:" + userId + " не найден.");
        }
        return users.get(userId);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUser(Integer userId) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь c id:" + userId + " не найден.");
        }
        users.remove(userId);
    }
}
