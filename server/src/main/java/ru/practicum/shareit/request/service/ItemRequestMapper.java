package ru.practicum.shareit.request.service;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemDtoMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {
    public ItemRequest dtoToRequest(User user, ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .build();
    }

    public ItemRequestDto requestToDto(ItemRequest request) {
        List<ItemDto> itemsDto = new ArrayList<>();

        if (!Objects.isNull(request.getItems())) {
            itemsDto = request.getItems().stream()
                    .map(ItemDtoMapper::itemToDto)
                    .collect(Collectors.toList());
        }
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemsDto)
                .build();
    }
}
