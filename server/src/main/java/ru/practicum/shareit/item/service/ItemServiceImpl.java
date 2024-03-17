package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingDtoMapper;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.extraExceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemServiceImpl(ItemRepository itemRepository,
                           ItemRequestRepository itemRequestRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public ItemDto addItem(ItemDto itemDto, Integer ownerId) {
        log.info("Добавление новой вещи: {} пользователем id:{}", itemDto, ownerId);
        if (ownerId <= 0) {
            throw new ValidationException("Id владельца должен быть положительным");
        }
        if (itemDto.getName() == null || itemDto.getDescription() == null || itemDto.getAvailable() == null
        || "".equals(itemDto.getName()) || "".equals(itemDto.getDescription())) {
            throw new ValidationException("Поля предмета должны быть заполнены");
        }
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    return new UserNotFoundException("Пользователь не найден");
                }
        );
        Item item = ItemDtoMapper.dtoToItem(itemDto);
        item.setOwner(user);
        if (itemDto.getRequestId() != null) {
            item.setRequest(itemRequestRepository.getReferenceById(itemDto.getRequestId()));
        }
        Item resultItem = itemRepository.save(item);
        return ItemDtoMapper.itemToDto(resultItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Integer ownerId, Integer itemId) {
        log.info("Обновление вещи {} пользователя id:{}", itemDto, ownerId);
        if (ownerId <= 0 || itemId <= 0) {
            throw new ValidationException("Id должен быть положительным");
        }
        if ("".equals(itemDto.getName()) || "".equals(itemDto.getDescription())) {
            throw new ValidationException("Поля предмета должны быть заполнены");
        }
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    return new UserNotFoundException("Владельца вещи с " + itemId + " не существует");
                });
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                            return new ItemNotFoundException("Вещи с " + itemId + " не существует");
                        }
                );
        if (!user.equals(item.getOwner())) {
            throw new ItemNotFoundException("Пользователь с id = " + ownerId +
                    " не является собственником вещи id = " + itemId);
        }

        Boolean isAvailable = itemDto.getAvailable();
        if (isAvailable != null) {
            item.setAvailable(isAvailable);
        }
        String description = itemDto.getDescription();
        if (description != null && !description.isBlank()) {
            item.setDescription(description);
        }
        String name = itemDto.getName();
        if (name != null && !name.isBlank()) {
            item.setName(name);
        }
        return ItemDtoMapper.itemToDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Integer itemId, Integer userId) {
        log.info("Получение вещи по id:{}", itemId);
        if (itemId <= 0) {
            throw new ValidationException("Id должен быть положительным");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> {
                            return new UserNotFoundException("Пользователь не найден");
                        }
                );
        Optional<Item> itemGet = itemRepository.findById(itemId);
        if (itemGet.isEmpty()) {
            throw new UserNotFoundException("У пользователя с id = " + userId + " не " +
                    "существует вещи с id = " + itemId);
        }
        Item item = itemGet.get();
        ItemDto itemDto = ItemDtoMapper.itemToDto(item);
        itemDto.setComments(getAllItemComments(itemId));
        if (!item.getOwner().getId().equals(userId)) {
            return itemDto;
        }
        List<Booking> bookings = bookingRepository.findAllByItemAndStatusOrderByStartAsc(item, BookingStatus.APPROVED);
        List<BookingDtoOut> bookingDTOList = bookings
                .stream()
                .map(BookingDtoMapper::toBookingOut)
                .collect(toList());

        itemDto.setLastBooking(getLastBooking(bookingDTOList, LocalDateTime.now()));
        itemDto.setNextBooking(getNextBooking(bookingDTOList, LocalDateTime.now()));
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByOwner(Integer ownerId, Integer from, Integer size) {
        log.info("Отправление списка вещей владельца id:{}", ownerId);
        if (ownerId <= 0) {
            throw new ValidationException("Id должен быть положительным");
        }
        userRepository.findById(ownerId)
                .orElseThrow(() -> {
                            return new UserNotFoundException("Пользователь не найден");
                        }
                );
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        List<Item> itemList = itemRepository.findAllByOwnerId(ownerId, pageable);
        List<Integer> idList = itemList.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        Map<Integer, List<CommentDto>> comments = commentRepository.findAllByItemIdIn(idList)
                .stream()
                .map(CommentDtoMapper::toCommentDto)
                .collect(groupingBy(CommentDto::getItemId, toList()));

        Map<Integer, List<BookingDtoOut>> bookings = bookingRepository.findAllByItemInAndStatusOrderByStartAsc(itemList,
                        BookingStatus.APPROVED)
                .stream()
                .map(BookingDtoMapper::toBookingOut)
                .collect(groupingBy(BookingDtoOut::getItemId, toList()));

        return itemList
                .stream()
                .map(item -> ItemDtoMapper.itemToDto(
                        item,
                        getLastBooking(bookings.get(item.getId()), LocalDateTime.now()),
                        comments.get(item.getId()),
                        getNextBooking(bookings.get(item.getId()), LocalDateTime.now())
                ))
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsBySearch(Integer userId, String text, Integer from, Integer size) {
        log.info("Отправление списка вещей по поисковому запросу: '{}'", text);
        if (text.isBlank()) {
            return List.of();
        }
        userRepository.findById(userId)
                .orElseThrow(() -> {
                            return new UserNotFoundException("Пользователь не найден");
                        }
                );
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> itemList = itemRepository.search(text, pageable);
        return itemList.stream()
                .map(ItemDtoMapper::itemToDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(Integer userId, CommentDto commentDto, Integer itemId) {
        log.info("Создание комментария для вещи id{}", itemId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                            return new UserNotFoundException("Пользователь не найден");
                        }
                );
        Optional<Item> itemById = itemRepository.findById(itemId);

        if (itemById.isEmpty()) {

            throw new ItemNotFoundException("У пользователя с id = " + userId + " не " +
                    "существует вещи с id = " + itemId);
        }
        Item item = itemById.get();

        List<Booking> userBookings = bookingRepository.findAllByUserBookings(userId, itemId, LocalDateTime.now());

        if (userBookings.isEmpty()) {
            throw new ValidationException("У пользователя с id   " + userId + " должно быть хотя бы одно бронирование предмета с id " + itemId);
        }

        return CommentDtoMapper.toCommentDto(commentRepository.save(CommentDtoMapper.toComment(commentDto, item, user)));
    }

    public List<CommentDto> getAllItemComments(Integer itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);

        return comments.stream()
                .map(CommentDtoMapper::toCommentDto)
                .collect(toList());
    }

    public BookingDtoOut getLastBooking(List<BookingDtoOut> bookings, LocalDateTime time) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings
                .stream()
                .filter(bookingDTO -> !bookingDTO.getStart().isAfter(time))
                .reduce((booking1, booking2) -> booking1.getStart().isAfter(booking2.getStart()) ? booking1 : booking2)
                .orElse(null);
    }

    public BookingDtoOut getNextBooking(List<BookingDtoOut> bookings, LocalDateTime time) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings
                .stream()
                .filter(bookingDTO -> bookingDTO.getStart().isAfter(time))
                .findFirst()
                .orElse(null);
    }
}
