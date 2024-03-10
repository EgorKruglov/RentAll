package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exceptions.extraExceptions.RequestNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private final User user = User.builder()
            .id(1)
            .name("username")
            .email("email@email.com")
            .build();

    private final UserDto userDto = UserDto.builder()
            .id(1)
            .name("username")
            .email("email@email.com")
            .build();

    private final Item item = Item.builder()
            .id(1)
            .name("item name")
            .description("description")
            .available(true)
            .owner(user)
            .build();

    private final ItemRequest request = ItemRequest.builder()
            .id(1)
            .description("request description")
            .items(List.of(item))
            .build();

    @Test
    void addNewRequest() {
        ItemRequestDto requestDto = ItemRequestMapper.requestToDto(request);
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto actualRequestDto = requestService.add(user.getId(), requestDto);

        assertEquals(requestDto, actualRequestDto);
    }

    @Test
    void getUserRequests() {
        List<ItemRequestDto> expectedRequestsDto = List.of(ItemRequestMapper.requestToDto(request));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findAllByRequesterId(userDto.getId())).thenReturn(List.of(request));

        List<ItemRequestDto> actualRequestsDto = requestService.getUserRequests(userDto.getId());

        assertEquals(expectedRequestsDto, actualRequestsDto);
    }

    @Test
    void getAllRequests() {
        List<ItemRequestDto> expectedRequestsDto = List.of(ItemRequestMapper.requestToDto(request));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findAllByRequester_IdNotOrderByCreatedDesc(anyInt(), any(PageRequest.class)))
                .thenReturn(List.of(request));

        List<ItemRequestDto> actualRequestsDto = requestService.getAllRequests(userDto.getId(), 0, 10);

        assertEquals(expectedRequestsDto, actualRequestsDto);
    }

    @Test
    void getRequestById() {
        ItemRequestDto expectedRequestDto = ItemRequestMapper.requestToDto(request);
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        ItemRequestDto actualRequestDto = requestService.getRequestById(userDto.getId(), request.getId());

        assertEquals(expectedRequestDto, actualRequestDto);
    }

    @Test
    void getRequestByIdWhenRequestIdIsNotValidShouldThrowObjectNotFoundException() {
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.empty());

        RequestNotFoundException requestNotFoundException = assertThrows(RequestNotFoundException.class,
                () -> requestService.getRequestById(userDto.getId(), request.getId()));

        assertEquals(requestNotFoundException.getMessage(), String.format("Запроса с id:%s" +
                " не существует", request.getId()));
    }
}
