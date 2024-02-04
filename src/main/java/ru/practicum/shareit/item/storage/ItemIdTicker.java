package ru.practicum.shareit.item.storage;

public class ItemIdTicker {
    private static Integer id = 0;

    public static int getNewId() {
        id += 1;
        return id;
    }
}
