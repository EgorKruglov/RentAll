package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemServiceImpl itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemDto item,
                           @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        ItemDto resultItem = itemService.addItem(item, ownerId);
        log.info("Вещь добавлена id:" + resultItem.getId());
        return resultItem;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@Valid @RequestBody ItemDto item,
                              @RequestHeader("X-Sharer-User-Id") Integer ownerId,
                              @PathVariable Integer itemId) {
        ItemDto resultItem = itemService.updateItem(item, ownerId, itemId);
        log.info("Данные вещи обновлены id:" + itemId);
        return resultItem;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader("X-Sharer-User-Id") Integer userId,
                           @PathVariable Integer itemId) {
        ItemDto item = itemService.getItemById(itemId, userId);
        log.info("Отправлена информация о вещи id:{} пользователю id:{}", itemId, userId);
        return item;
    }

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Integer ownerId,
                                         @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                         @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        List<ItemDto> ownerItems = itemService.getItemsByOwner(ownerId, from, size);
        log.info("Список вещей отправлен владельцу id:" + ownerId);
        return ownerItems;
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearch(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                          @RequestParam String text,
                                          @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                          @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        List<ItemDto> searchingItems = itemService.getItemsBySearch(userId, text, from, size);
        log.info("Отправлен список вещей по поисковому запросу: '{}'", text);
        return searchingItems;
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                    @Valid @RequestBody CommentDto commentDto,
                                    @PathVariable Integer itemId) {
        log.info("Добавлен комментарий пользователем id:" + userId);
        return itemService.createComment(userId, commentDto, itemId);
    }
}