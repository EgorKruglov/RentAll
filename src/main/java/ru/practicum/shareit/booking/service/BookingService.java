package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut add(Integer userId, BookingDto bookingDto);

    BookingDtoOut update(Integer userId, Integer bookingId, Boolean approved);

    BookingDtoOut findBookingByUserId(Integer userId, Integer bookingId);

    List<BookingDtoOut> findAll(Integer userId, String state, Integer from, Integer size);

    List<BookingDtoOut> findAllOwner(Integer userId, String state, Integer from, Integer size);
}
