package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto item, Integer ownerId);

    ItemDto updateItem(ItemDto item, Integer ownerId, Integer itemId);

    ItemDto getItemById(Integer itemId);

    List<ItemDto> getItemsByOwner(Integer ownerId);

    List<ItemDto> getItemsBySearch(String text);
}
