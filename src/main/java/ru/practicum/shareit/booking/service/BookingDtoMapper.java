package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserDtoMapper;

public class BookingDtoMapper {
    public static Booking toBooking(User user, Item item, BookingDto bookingDto) {
        return new Booking(
                item,
                bookingDto.getStart(),
                bookingDto.getEnd(),
                user,
                BookingStatus.WAITING);
    }

    public static BookingDtoOut toBookingOut(Booking booking) {
        return new BookingDtoOut(
                booking.getId(),
                ItemDtoMapper.itemToDto(booking.getItem()),
                booking.getStart(),
                booking.getEnd(),
                UserDtoMapper.userToDto(booking.getBooker()),
                booking.getStatus());
    }
}
