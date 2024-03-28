package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto item, Integer ownerId);

    ItemDto updateItem(ItemDto item, Integer ownerId, Integer itemId);

    ItemDto getItemById(Integer itemId, Integer userId);

    List<ItemDto> getItemsByOwner(Integer ownerId, Integer from, Integer size);

    List<ItemDto> getItemsBySearch(Integer userId, String text, Integer from, Integer size);

    CommentDto createComment(Integer userId, CommentDto commentDto, Integer itemId);
}
