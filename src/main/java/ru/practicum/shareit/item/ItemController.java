package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemServiceImpl itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemDto item,
                           @RequestHeader("X-Sharer-User-Id") Integer ownerId,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.append(error.getDefaultMessage()).append("\n")
            );
            throw new ValidationException("Ошибка валидации вещи: " + errors);
        }
        ItemDto resultItem = itemService.addItem(item, ownerId);
        log.info("Вещь добавлена id:" + resultItem.getId());
        return resultItem;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@Valid @RequestBody ItemDto item,
                              @RequestHeader("X-Sharer-User-Id") Integer ownerId,
                              @PathVariable Integer itemId,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.append(error.getDefaultMessage()).append("\n")
            );
            throw new ValidationException("Ошибка валидации вещи: " + errors);
        }
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
    public List<ItemDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        List<ItemDto> ownerItems = itemService.getItemsByOwner(ownerId);
        log.info("Список вещей отправлен владельцу id:" + ownerId);
        return ownerItems;
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearch(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                          @RequestParam String text) {
        List<ItemDto> searchingItems = itemService.getItemsBySearch(userId, text);
        log.info("Отправлен список вещей по поисковому запросу: '{}'", text);
        return searchingItems;
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                    @Valid @RequestBody CommentDto commentDto,
                                    @PathVariable Integer itemId,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.append(error.getDefaultMessage()).append("\n")
            );
            throw new ValidationException("Ошибка валидации отзыва: " + errors);
        }
        log.info("Добавлен комментарий пользователем id:" + userId);
        return itemService.createComment(userId, commentDto, itemId);
    }
}