package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.extraExceptions.RequestNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserDtoMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserService userService;
    private final ItemRequestRepository requestRepository;

    public ItemRequestServiceImpl(UserServiceImpl userService, ItemRequestRepository requestRepository) {
        this.userService = userService;
        this.requestRepository = requestRepository;
    }

    @Override
    public ItemRequestDto add(Integer userId, ItemRequestDto itemRequestDto) {
        log.info("Добавление запроса на вещь пользователем id:{}", userId);
        if (itemRequestDto.getDescription() == null) {
            throw new ValidationException("Поля запроса должны быть заполнены");
        }
        User user = UserDtoMapper.dtoToUser(userService.getUserById(userId));
        if (user == null) {
            throw new UserNotFoundException("неть");
        }
        ItemRequest request = ItemRequestMapper.dtoToRequest(user, itemRequestDto);
        request.setRequester(user);
        return ItemRequestMapper.requestToDto(requestRepository.save(request));
    }

    @Override
    public ItemRequestDto getRequestById(Integer userId, Integer requestId) {
        log.info("Получение запроса на вещь по id:{}", requestId);
        userService.getUserById(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                            return new RequestNotFoundException("Запроса с id:" + requestId + " не существует");
                        }
                );
        return ItemRequestMapper.requestToDto(request);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Integer userId) {
        log.info("Получение списка запросов вещей пользователя id:{}", userId);
        userService.getUserById(userId);
        List<ItemRequest> itemRequestList = requestRepository.findAllByRequesterId(userId);
        return itemRequestList.stream()
                .map(ItemRequestMapper::requestToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Integer userId, Integer from, Integer size) {
        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> itemRequestList = requestRepository.findAllByRequester_IdNotOrderByCreatedDesc(userId, pageable);
        return itemRequestList.stream()
                .map(ItemRequestMapper::requestToDto)
                .collect(Collectors.toList());
    }
}
