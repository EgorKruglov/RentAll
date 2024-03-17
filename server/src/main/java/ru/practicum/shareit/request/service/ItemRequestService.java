package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(Integer userId, ItemRequestDto requestDto);

    List<ItemRequestDto> getUserRequests(Integer userId);

    List<ItemRequestDto> getAllRequests(Integer userId, Integer from, Integer size);

    ItemRequestDto getRequestById(Integer userId, Integer requestId);
}
