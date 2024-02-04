package ru.practicum.shareit.item.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.extraExceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.StorageException;
import ru.practicum.shareit.exceptions.extraExceptions.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.storage.InMemoryUserStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
public class InMemoryItemStorage implements ItemStorage {
    UserStorage userStorage;
    HashMap<Integer, Item> items;

    @Autowired
    public InMemoryItemStorage(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
        this.items = new HashMap<>();
    }

    @Override
    public Item addItem(Item item) {
        userStorage.getUserById(item.getOwnerId());  // Проверка наличия пользователя
        item.setId(ItemIdTicker.getNewId());
        if (items.containsKey(item.getId())) {
            throw new StorageException("Ошибка генерации id. Предмет с id:" + item.getId() + " уже существует.");
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item, Integer itemId) {
        if (items.get(itemId) == null) {
            throw new ItemNotFoundException("Вещь для обновления не найдена.");
        }
        Item oldItem = items.get(itemId);
        if (!Objects.equals(oldItem.getOwnerId(), item.getOwnerId())) {  // Проверка id владельца вещи
            throw new UserNotFoundException("Пользователь id:" + item.getOwnerId() + " не является владельцем вещи.");
        }
        item.setId(itemId);
        if (item.getName() == null) {
            item.setName(oldItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(oldItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(oldItem.getAvailable());
        }
        items.put(itemId, item);
        return item;
    }

    @Override
    public Item getItemById(Integer itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new ItemNotFoundException("Вещь с id:" + itemId + " не найдена.");
        }
        return item;
    }

    @Override
    public List<Item> getItemsByOwner(Integer ownerId) {
        userStorage.getUserById(ownerId);
        List<Item> ownerItems = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwnerId().equals(ownerId)) {
                ownerItems.add(item);
            }
        }
        return ownerItems;
    }

    @Override
    public List<Item> getItemsBySearch(String text) {
        String lowerCaseText = text.toLowerCase();
        List<Item> searchingItems = new ArrayList<>();
        for (Item item : items.values()) {
            if ((item.getName().toLowerCase().contains(lowerCaseText) ||
                    item.getDescription().toLowerCase().contains(lowerCaseText)) &&
                    item.getAvailable().equals(true)) {
                searchingItems.add(item);
            }
        }
        return searchingItems;
    }
}
