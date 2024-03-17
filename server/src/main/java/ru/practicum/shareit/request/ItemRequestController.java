package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@Validated
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {
    private final ItemRequestService requestService;

    @Autowired
    public ItemRequestController(ItemRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ItemRequestDto add(@RequestHeader("X-Sharer-User-Id") Integer userId,
                              @RequestBody ItemRequestDto requestDto) {
        ItemRequestDto resultRequest = requestService.add(userId, requestDto);
        log.info("Создан запрос вещи id:{}", resultRequest.getId());
        return resultRequest;
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto get(@RequestHeader("X-Sharer-User-Id") Integer userId,
                              @PathVariable Integer requestId) {
        ItemRequestDto resultRequest = requestService.getRequestById(userId, requestId);
        log.info("Отправлен запрос вещи id:{} пользователю id:{}", requestId, userId);
        return resultRequest;
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        List<ItemRequestDto> resultList = requestService.getUserRequests(userId);
        log.info("Отправлен список запросов вещей пользователя id:{}", userId);
        return resultList;
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                               @RequestParam(name = "from", defaultValue = "0") Integer from,
                                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        List<ItemRequestDto> resultList = requestService.getAllRequests(userId, from, size);
        log.info("Отправлен список всех запросов вещей пользователю id:{}", userId);
        return resultList;
    }
}
