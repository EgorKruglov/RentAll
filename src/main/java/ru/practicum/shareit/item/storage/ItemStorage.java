package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addItem(Item item);

    Item updateItem(Item item, Integer itemId);

    Item getItemById(Integer itemId);

    List<Item> getItemsByOwner(Integer ownerId);

    List<Item> getItemsBySearch(String query);
}
