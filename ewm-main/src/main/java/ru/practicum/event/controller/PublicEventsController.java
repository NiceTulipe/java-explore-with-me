package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventsShortDto;
import ru.practicum.event.service.EventsService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utility.UtilityClass.pattern;

@Slf4j
@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class PublicEventsController {
    private final EventsService service;

    @GetMapping("{id}")
    public EventFullDto getEventWithFullInfoById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Get Event by id {}", id);
        return service.getEventWithFullInfoById(id, request);
    }

    @GetMapping
    public List<EventsShortDto> getEventsWithFilters(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = pattern) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = pattern) LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request) {
        log.info("Get Events from text {}, categories {}, paid {}, rangeStart {}, rangeEnd {}, onlyAvailable {}, " +
                        "sort {}, from {}, size {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return service.getEventsWithFilters(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from,
                size, request);
    }
}
