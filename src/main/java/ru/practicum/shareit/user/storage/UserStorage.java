package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User updateUser(Integer userId, User user);

    User getUserById(Integer userId);

    List<User> getUsers();

    void deleteUser(Integer userId);
}
