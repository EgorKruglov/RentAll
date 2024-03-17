package ru.practicum.shareit.exceptions.extraExceptions;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
