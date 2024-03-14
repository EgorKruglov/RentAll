package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.extraExceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public BookingDtoOut add(Integer userId, BookingDto bookingDto) {
        log.info("Создание запроса на бронирование вещи пользователем id:{}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> {
                    return new UserNotFoundException("Пользователя с " + userId + " не существует");
                }
        );
        Optional<Item> itemById = itemRepository.findById(bookingDto.getItemId());
        if (itemById.isEmpty()) {
            throw new ItemNotFoundException("Вещь не найдена.");
        }
        Item item = itemById.get();
        bookingValidation(bookingDto, user, item);
        Booking booking = BookingDtoMapper.toBooking(user, item, bookingDto);
        return BookingDtoMapper.toBookingOut(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoOut update(Integer userId, Integer bookingId, Boolean approved) {
        log.info("Обновление статуса бронирования вещи от владельца с id:{}", userId);
        Booking booking = validateBookingDetails(userId, bookingId, 1);
        assert booking != null;
        BookingStatus newStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newStatus);
        return BookingDtoMapper.toBookingOut(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDtoOut findBookingByUserId(Integer userId, Integer bookingId) {
        log.info("Получение данных о бронировании вещи пользователем с id:{}", userId);
        Booking booking = validateBookingDetails(userId, bookingId, 2);
        assert booking != null;
        return BookingDtoMapper.toBookingOut(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoOut> findAll(Integer bookerId, String state, Integer from, Integer size) {
        log.info("получение списка всех бронирований текущего пользователя с id: {} и статусом {}", bookerId, state);
        userRepository.findById(bookerId).orElseThrow(() -> {
                    return new UserNotFoundException("Пользователя с " + bookerId + " не существует");
                }
        );
        Pageable pageable = PageRequest.of(from / size, size);
        switch (validState(state)) {
            case ALL:
                return bookingRepository.findAllBookingsByBookerId(bookerId, pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllCurrentBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());

            case PAST:
                return bookingRepository.findAllPastBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());

            case FUTURE:
                return bookingRepository.findAllFutureBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());

            case WAITING:
                return bookingRepository.findAllWaitingBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());

            case REJECTED:
                return bookingRepository.findAllRejectedBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoOut> findAllOwner(Integer ownerId, String state, Integer from, Integer size) {
        log.info("получение списка всех бронирований текущего владельца с id: {} и статусом {}", ownerId, state);
        userRepository.findById(ownerId).orElseThrow(() -> {
                    return new UserNotFoundException("Пользователя с " + ownerId + " не существует");
                }
        );
        Pageable pageable = PageRequest.of(from / size, size);
        switch (validState(state)) {
            case ALL:
                return bookingRepository.findAllBookingsByOwnerId(ownerId, pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllCurrentBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());

            case PAST:
                return bookingRepository.findAllPastBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());

            case FUTURE:
                return bookingRepository.findAllFutureBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());

            case WAITING:
                return bookingRepository.findAllWaitingBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());

            case REJECTED:
                return bookingRepository.findAllRejectedBookingsByOwnerId(ownerId, pageable).stream()
                        .map(BookingDtoMapper::toBookingOut)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private void bookingValidation(BookingDto bookingDto, User user, Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь не доступна для бронирования.");
        }
        if (user.getId().equals(item.getOwner().getId())) {
            throw new ItemNotFoundException("Вещь не найдена.");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Дата окончания не может быть раньше или равна дате начала");
        }
    }

    private BookingState validState(String bookingState) {
        BookingState state = BookingState.from(bookingState);
        if (state == null) {
            throw new IllegalArgumentException("Unknown state: " + bookingState);
        }
        return state;
    }

    private Booking validateBookingDetails(Integer userId, Integer bookingId, Integer number) {
        Optional<Booking> bookingById = bookingRepository.findById(bookingId);
        if (bookingById.isEmpty()) {
            throw new BookingNotFoundException("Бронь не найдена.");
        }
        Booking booking = bookingById.get();
        switch (number) {
            case 1:
                if (!booking.getItem().getOwner().getId().equals(userId)) {
                    throw new BookingNotFoundException("Пользователь не является владельцем");
                }
                if (!booking.getStatus().equals(BookingStatus.WAITING)) {
                    throw new ValidationException("Бронь не cо статусом WAITING");
                }
                return booking;
            case 2:
                if (!booking.getBooker().getId().equals(userId)
                        && !booking.getItem().getOwner().getId().equals(userId)) {
                    throw new BookingNotFoundException("Пользователь не владелeц и не автор бронирования ");
                }
                return booking;
        }
        return null;
    }
}
