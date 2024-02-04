package ru.practicum.shareit.user.storage;

public class UserIdTicker {
    private static Integer id = 0;

    public static int getNewId() {
        id += 1;
        return id;
    }
}
