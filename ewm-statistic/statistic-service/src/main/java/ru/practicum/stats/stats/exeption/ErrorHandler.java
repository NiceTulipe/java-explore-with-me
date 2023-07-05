package ru.practicum.stats.stats.exeption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handlerValidationException(final ValidationException exception) {
        log.warn("Error! Validation fault, server status: '{}' text message: '{}'",
                HttpStatus.BAD_REQUEST, exception.getMessage());
        return Map.of("Validation fault, check your actions", exception.getMessage());
    }
}
