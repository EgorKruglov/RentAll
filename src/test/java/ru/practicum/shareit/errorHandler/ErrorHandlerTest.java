package ru.practicum.shareit.errorHandler;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exceptions.ErrorHandler;
import ru.practicum.shareit.exceptions.ErrorResponse;
import ru.practicum.shareit.exceptions.extraExceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.RequestNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.extraExceptions.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void testHandleUserNotFoundException() {
        UserNotFoundException exception = new UserNotFoundException("User not found");
        ErrorResponse response = errorHandler.handleUserNotFoundException(exception);
        assertNotNull(response);
        assertEquals("User not found", response.getError());
    }

    @Test
    void testHandleValidationException() {
        ValidationException exception = new ValidationException("Validation failed");
        ErrorResponse response = errorHandler.handleValidationException(exception);
        assertNotNull(response);
        assertEquals("Validation failed", response.getError());
    }

    @Test
    void testHandleItemNotFoundException() {
        ItemNotFoundException exception = new ItemNotFoundException("Item not found");
        ErrorResponse response = errorHandler.handleItemNotFoundException(exception);
        assertNotNull(response);
        assertEquals("Item not found", response.getError());
    }

    @Test
    void testHandleBookingNotFoundException() {
        BookingNotFoundException exception = new BookingNotFoundException("Booking not found");
        ErrorResponse response = errorHandler.handleBookingNotFoundException(exception);
        assertNotNull(response);
        assertEquals("Booking not found", response.getError());
    }

    @Test
    void testHandleRequestNotFoundException() {
        RequestNotFoundException exception = new RequestNotFoundException("Request not found");
        ErrorResponse response = errorHandler.handleRequestNotFoundException(exception);
        assertNotNull(response);
        assertEquals("Request not found", response.getError());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
        ErrorResponse response = errorHandler.handleIllegalArgumentException(exception);
        assertNotNull(response);
        assertEquals("Invalid argument", response.getError());
    }
}