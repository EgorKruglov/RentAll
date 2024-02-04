package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.InMemoryItemStorage;
import ru.practicum.shareit.item.storage.ItemStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;

    public ItemServiceImpl(InMemoryItemStorage itemStorage) {
        this.itemStorage = itemStorage;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Integer ownerId) {
        log.info("Добавление новой вещи: {} пользователем id:{}", itemDto, ownerId);
        if (ownerId <= 0) {
            throw new ValidationException("Id владельца должен быть положительным");
        }
        if (itemDto.getName() == null || itemDto.getDescription() == null || itemDto.getAvailable() == null
        || "".equals(itemDto.getName()) || "".equals(itemDto.getDescription())) {
            throw new ValidationException("Поля предмета должны быть заполнены");
        }
        Item resultItem = itemStorage.addItem(ItemDtoMapper.dtoToItem(itemDto, ownerId));
        return ItemDtoMapper.itemToDto(resultItem);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Integer ownerId, Integer itemId) {
        log.info("Обновление вещи: {} пользователя id:{}", itemDto, ownerId);
        if (ownerId <= 0 || itemId <=0) {
            throw new ValidationException("Id должен быть положительным");
        }
        if ("".equals(itemDto.getName()) || "".equals(itemDto.getDescription())) {
            throw new ValidationException("Поля предмета должны быть заполнены");
        }
        Item resultItem = itemStorage.updateItem(ItemDtoMapper.dtoToItem(itemDto, ownerId), itemId);
        return ItemDtoMapper.itemToDto(resultItem);
    }

    @Override
    public ItemDto getItemById(Integer itemId) {
        log.info("Получение вещи по id:{}", itemId);
        if (itemId <=0) {
            throw new ValidationException("Id должен быть положительным");
        }
        Item resultItem = itemStorage.getItemById(itemId);
        return ItemDtoMapper.itemToDto(resultItem);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Integer ownerId) {
        log.info("Отправление списка вещей владельца id:{}", ownerId);
        if (ownerId <=0) {
            throw new ValidationException("Id должен быть положительным");
        }
        List<Item> ownerItems = itemStorage.getItemsByOwner(ownerId);
        List<ItemDto> resultList = new ArrayList<>();
        for (Item item : ownerItems) {
            resultList.add(ItemDtoMapper.itemToDto(item));
        }
        return resultList;
    }

    @Override
    public List<ItemDto> getItemsBySearch(String text) {
        log.info("Отправление списка вещей по поисковому запросу: '{}'", text);
        if (text.isBlank()) {
            return List.of();
        }
        List<Item> searchingItems = itemStorage.getItemsBySearch(text);
        List<ItemDto> resultList = new ArrayList<>();
        for (Item item : searchingItems) {
            resultList.add(ItemDtoMapper.itemToDto(item));
        }
        return resultList;
    }
}
