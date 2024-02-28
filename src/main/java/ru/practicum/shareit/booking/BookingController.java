package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;

import javax.validation.Valid;
import java.util.List;



/**
 * TODO Sprint add-bookings.
 */
@RestController
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingServiceImpl bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDtoOut create(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                @Valid @RequestBody BookingDto bookingDto,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.append(error.getDefaultMessage()).append("\n")
            );
            throw new ValidationException("Ошибка валидации запроса бронирования: " + errors);
        }
        log.info("Пользователь с id:{} создал запрос на бронирование вещи", userId);
        return bookingService.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOut updateStatus(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                      @PathVariable("bookingId") Integer bookingId,
                                      @RequestParam(name = "approved") Boolean approved) {
        log.info("Владелец id:{} обновил статус бронирования вещи", userId);
        return bookingService.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOut findBookingById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                         @PathVariable("bookingId") Integer bookingId) {
        log.info("Отправлены данные о бронировании вещи id:{} пользователю id:{}", bookingId, userId);
        return bookingService.findBookingByUserId(userId, bookingId);
    }

    @GetMapping
    public List<BookingDtoOut> findAll(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                       @RequestParam(value = "state", defaultValue = "ALL") String bookingState) {
        log.info("Отправлен список всех бронирований со статусом {} пользователю id:{}", bookingState, userId);
        return bookingService.findAll(userId, bookingState);
    }

    @GetMapping("/owner")
    public List<BookingDtoOut> getAllOwner(@RequestHeader("X-Sharer-User-Id") Integer ownerId,
                                           @RequestParam(value = "state", defaultValue = "ALL") String bookingState) {
        log.info("Отправлен список всех бронирований со статусом {} владельцу вещей с id:{}", bookingState, ownerId);
        return bookingService.findAllOwner(ownerId, bookingState);
    }
}
