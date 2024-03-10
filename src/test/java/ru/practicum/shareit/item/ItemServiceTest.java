package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.extraExceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.CommentDtoMapper;
import ru.practicum.shareit.item.service.ItemDtoMapper;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;

    private final User user = User.builder()
            .id(1)
            .name("username")
            .email("email@email.com")
            .build();

    private final User user2 = User.builder()
            .id(2)
            .name("username2")
            .email("email2@email.com")
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

    private final ItemDto itemDto = ItemDto.builder()
            .id(1)
            .name("item name")
            .description("description")
            .available(true)
            .comments(Collections.emptyList())
            .build();
    private final ItemDto itemDtoUpdate = ItemDto.builder()
            .build();

    private final Comment comment = Comment.builder()
            .id(1)
            .text("comment")
            .created(LocalDateTime.now())
            .author(user)
            .item(item)
            .build();

    private final Booking booking = Booking.builder()
            .id(1)
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .start(LocalDateTime.now().minusDays(1L))
            .end(LocalDateTime.now().plusDays(1L))
            .build();

    private final Booking lastBooking = Booking.builder()
            .id(2)
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .start(LocalDateTime.now().minusDays(2L))
            .end(LocalDateTime.now().minusDays(1L))
            .build();

    private final Booking pastBooking = Booking.builder()
            .id(3)
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .start(LocalDateTime.now().minusDays(10L))
            .end(LocalDateTime.now().minusDays(9L))
            .build();

    private final Booking nextBooking = Booking.builder()
            .id(4)
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .build();

    private final Booking futureBooking = Booking.builder()
            .id(5)
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .start(LocalDateTime.now().plusDays(10L))
            .end(LocalDateTime.now().plusDays(20L))
            .build();

    @Test
    void addNewItemWhenInvoked() {
        Item itemSaveTest = Item.builder()
                .name("test item name")
                .description("test description")
                .available(true)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.save(itemSaveTest)).thenReturn(itemSaveTest);

        ItemDto actualItemDto = itemService.addItem(ItemDtoMapper.itemToDto(itemSaveTest), userDto.getId());

        assertEquals(actualItemDto.getName(), "test item name");
        assertEquals(actualItemDto.getDescription(), "test description");
    }

    @Test
    void getItemById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemDto actualItemDto = itemService.getItemById(item.getId(), user.getId());

        assertEquals(itemDto, actualItemDto);
    }


    @Test
    void updateItem() {
        ItemRequest itemRequest = new ItemRequest(1, "description", user, LocalDateTime.now(), null);
        Item updatedItem = Item.builder()
                .id(1)
                .name("updated name")
                .description("updated description")
                .available(false)
                .owner(user)
                .request(itemRequest)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(updatedItem));

        ItemDto savedItem = itemService.updateItem(ItemDtoMapper.itemToDto(updatedItem), user.getId(), itemDto.getId());

        assertEquals("updated name", savedItem.getName());
        assertEquals("updated description", savedItem.getDescription());
    }

    @Test
    void updateItemWhenUserIsNotItemOwnerShouldThrowException() {
        Item updatedItem = Item.builder()
                .id(1)
                .name("updated name")
                .description("updated description")
                .available(false)
                .owner(user2)
                .build();

        when(itemRepository.findById(anyInt())).thenReturn(Optional.ofNullable(updatedItem));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        ItemNotFoundException itemNotFoundException = assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(ItemDtoMapper.itemToDto(updatedItem), user.getId(), itemDto.getId()));

        assertEquals(itemNotFoundException.getMessage(), "Пользователь с id = " + user.getId() +
                " не является собственником вещи id = " + item.getId());
    }

    @Test
    void updateItemWhenItemIdIsNotValid() {
        when(itemRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        ItemNotFoundException itemNotFoundException = assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(ItemDtoMapper.itemToDto(item), user.getId(), itemDto.getId()));
        assertEquals(itemNotFoundException.getMessage(), "Вещи с " + item.getId() + " не существует");
    }

    @Test
    void getAllComments() {
        List<CommentDto> expectedCommentsDto = List.of(CommentDtoMapper.toCommentDto(comment));
        when(commentRepository.findAllByItemId(item.getId())).thenReturn(List.of(comment));

        List<CommentDto> actualComments = itemService.getAllItemComments(item.getId());

        assertEquals(actualComments.size(), 1);
        assertEquals(actualComments, expectedCommentsDto);
    }

    @Test
    void searchItems() {
        Page<Item> items = new PageImpl<>(List.of(item));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(anyInt(), any(Pageable.class))).thenReturn(List.of(item));

        List<ItemDto> actualItemsDto = itemService.getItemsByOwner(1, 0, 10);

        assertEquals(1, actualItemsDto.size());
        assertEquals(1, actualItemsDto.get(0).getId());
        assertEquals("item name", actualItemsDto.get(0).getName());
    }

    @Test
    void createComment() {
        CommentDto expectedCommentDto = CommentDtoMapper.toCommentDto(comment);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByUserBookings(anyInt(), anyInt(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto actualCommentDto = itemService.createComment(user.getId(), CommentDtoMapper.toCommentDto(comment), item.getId());

        assertEquals(expectedCommentDto, actualCommentDto);
    }

    @Test
    void createComment_whenItemIdIsNotValid_thenThrowObjectNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        ItemNotFoundException itemNotFoundException = assertThrows(ItemNotFoundException.class,
                () -> itemService.createComment(user.getId(), CommentDtoMapper.toCommentDto(comment), item.getId()));

        assertEquals(itemNotFoundException.getMessage(), "У пользователя с id = " + user.getId() + " не " +
                "существует вещи с id = " + item.getId());
    }

    @Test
    void createCommentWhenUserHaveNotAnyBookingsShouldThrowValidationException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByUserBookings(anyInt(), anyInt(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> itemService.createComment(user.getId(), CommentDtoMapper.toCommentDto(comment), item.getId()));

        assertEquals(validationException.getMessage(), "У пользователя с id   " + user.getId() + " должно быть хотя бы одно бронирование предмета с id " + item.getId());

    }
}
