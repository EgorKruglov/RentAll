package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingDtoMapper;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.extraExceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final User user = User.builder()
            .id(1)
            .name("username")
            .email("email@email.com")
            .build();

    private final User owner = User.builder()
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
            .owner(owner)
            .build();

    private final Booking booking = Booking.builder()
            .id(1)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .status(BookingStatus.APPROVED)
            .item(item)
            .booker(user)
            .build();

    private final Booking bookingWaiting = Booking.builder()
            .id(1)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .status(BookingStatus.WAITING)
            .item(item)
            .booker(user)
            .build();

    private final BookingDto bookingDto = BookingDto.builder()
            .itemId(1)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .build();

    private final BookingDto bookingDtoEndBeforeStart = BookingDto.builder()
            .itemId(1)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().minusDays(1L))
            .build();

    @Test
    void create() {
        BookingDtoOut expectedBookingDtoOut = BookingDtoMapper.toBookingOut(BookingDtoMapper.toBooking(user, item, bookingDto));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(BookingDtoMapper.toBooking(user, item, bookingDto));

        BookingDtoOut actualBookingDtoOut = bookingService.add(userDto.getId(), bookingDto);

        assertEquals(expectedBookingDtoOut, actualBookingDtoOut);
    }

    @Test
    void createWhenEndIsBeforeStartShouldThrowValidationException() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        ValidationException bookingValidationException = assertThrows(ValidationException.class,
                () -> bookingService.add(userDto.getId(), bookingDtoEndBeforeStart));

        assertEquals(bookingValidationException.getMessage(), "Дата окончания не может быть раньше или равна дате начала");
    }

    @Test
    void createWhenItemIsNotAvailableShouldThrowValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        ValidationException bookingValidationException = assertThrows(ValidationException.class,
                () -> bookingService.add(userDto.getId(), bookingDto));

        assertEquals(bookingValidationException.getMessage(), "Вещь не доступна для бронирования.");
    }

    @Test
    void createWhenItemOwnerEqualsBookerShouldThrowValidationException() {
        item.setOwner(user);
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        ItemNotFoundException itemNotFoundException = assertThrows(ItemNotFoundException.class,
                () -> bookingService.add(userDto.getId(), bookingDto));

        assertEquals(itemNotFoundException.getMessage(), "Вещь не найдена.");
    }

    @Test
    void update() {
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(bookingWaiting));
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingWaiting);

        BookingDtoOut actualBookingDtoOut = bookingService.update(owner.getId(), bookingWaiting.getId(), true);

        assertEquals(BookingStatus.APPROVED, actualBookingDtoOut.getStatus());
    }

    @Test
    void updateWhenStatusNotApproved() {
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(bookingWaiting));
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingWaiting);

        BookingDtoOut actualBookingDtoOut = bookingService.update(owner.getId(), bookingWaiting.getId(), false);

        assertEquals(BookingStatus.REJECTED, actualBookingDtoOut.getStatus());
    }

    @Test
    void updateShouldStatusNotWaiting() {
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(booking));

        ValidationException bookingValidationException = assertThrows(ValidationException.class,
                () -> bookingService.update(owner.getId(), booking.getId(), false));

        assertEquals(bookingValidationException.getMessage(), "Бронь не cо статусом WAITING");
    }

    @Test
    void updateWhenUserIsNotItemOwnerShouldThrowNotFoundException() {
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(booking));

        BookingNotFoundException bookingNotFoundException = assertThrows(BookingNotFoundException.class,
                () -> bookingService.update(userDto.getId(), booking.getId(), true));

        assertEquals(bookingNotFoundException.getMessage(), "Пользователь не является владельцем");
    }

    @Test
    void getById() {
        BookingDtoOut expectedBookingDtoOut = BookingDtoMapper.toBookingOut(booking);
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(booking));

        BookingDtoOut actualBookingDtoOut = bookingService.findBookingByUserId(user.getId(), booking.getId());

        assertEquals(expectedBookingDtoOut, actualBookingDtoOut);
    }

    @Test
    void getByIdWhenBookingIdIsNotValidShouldThrowObjectNotFoundException() {
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.empty());

        BookingNotFoundException bookingNotFoundException = assertThrows(BookingNotFoundException.class,
                () -> bookingService.findBookingByUserId(1, booking.getId()));

        assertEquals(bookingNotFoundException.getMessage(), "Бронь не найдена.");
    }

    @Test
    void getByIdWhenUserIsNotItemOwnerShouldThrowObjectNotFoundException() {
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(booking));

        BookingNotFoundException bookingNotFoundException = assertThrows(BookingNotFoundException.class,
                () -> bookingService.findBookingByUserId(3, booking.getId()));

        assertEquals(bookingNotFoundException.getMessage(), "Пользователь не владелeц и не автор бронирования ");
    }

    @Test
    void getAllByBookerWhenBookingStateAll() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllBookingsByBookerId(anyInt(), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAll(user.getId(), "ALL", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByBooker_whenBookingStateCURRENT() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllCurrentBookingsByBookerId(anyInt(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAll(user.getId(), "CURRENT", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByBookerWhenBookingStatePAST() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllPastBookingsByBookerId(anyInt(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAll(user.getId(), "PAST", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByBookerWhenBookingStateFUTURE() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllFutureBookingsByBookerId(anyInt(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAll(user.getId(), "FUTURE", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByBookerWhenBookingStateWAITING() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllWaitingBookingsByBookerId(anyInt(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAll(user.getId(), "WAITING", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }


    @Test
    void getAllByBookerWhenBookingStateIsNotValidShouldThrowIllegalArgumentException() {
        assertThrows(UserNotFoundException.class,
                () -> bookingService.findAll(user.getId(), "ERROR", 0, 10));
    }

    @Test
    void getAllByOwnerWhenBookingStateAll() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllBookingsByOwnerId(anyInt(), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAllOwner(user.getId(), "ALL", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByOwnerWhenBookingStateCURRENT() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllCurrentBookingsByOwnerId(anyInt(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAllOwner(user.getId(), "CURRENT", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByOwnerWhenBookingStatePAST() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllPastBookingsByOwnerId(anyInt(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAllOwner(user.getId(), "PAST", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByOwnerWhenBookingStateFUTURE() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllFutureBookingsByOwnerId(anyInt(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAllOwner(user.getId(), "FUTURE", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByOwnerWhenBookingStateWAITING() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllWaitingBookingsByOwnerId(anyInt(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAllOwner(user.getId(), "WAITING", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByOwnerWhenBookingStateREJECTED() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllRejectedBookingsByOwnerId(anyInt(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAllOwner(user.getId(), "REJECTED", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);

    }

    @Test
    void getAllByBookerWhenBookingStateREJECTED() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(BookingDtoMapper.toBookingOut(booking));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllRejectedBookingsByBookerId(anyInt(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.findAll(user.getId(), "REJECTED", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    void getAllByOwnerWhenBookingStateIsNotValidThenThrowIllegalArgumentException() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.ofNullable(user));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.findAllOwner(user.getId(), "ERROR", 0, 10));
    }
}
