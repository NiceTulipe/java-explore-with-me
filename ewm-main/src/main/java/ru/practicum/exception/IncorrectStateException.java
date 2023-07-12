package ru.practicum.exception;

public class IncorrectStateException extends RuntimeException {
    public IncorrectStateException(final String message) {
        super(message);
    }
}

