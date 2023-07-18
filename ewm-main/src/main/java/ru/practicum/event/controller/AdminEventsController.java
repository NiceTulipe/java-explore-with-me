package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.SearchEventParamsAdmin;
import ru.practicum.event.dto.UpdateEvent;
import ru.practicum.event.service.EventsService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class AdminEventsController {
    private final EventsService service;

    @GetMapping
    public List<EventFullDto> getEventsForAdmin(@Valid SearchEventParamsAdmin paramsAdmin) {
        log.info("Get Events from users {}, states {}, categories {}, rangeStart {}, rangeEnd {}, from {}, size {}",
                paramsAdmin.getUsers(), paramsAdmin.getStates(),
                paramsAdmin.getCategories(), paramsAdmin.getRangeStart(),
                paramsAdmin.getRangeEnd(), paramsAdmin.getFrom(),
                paramsAdmin.getSize());
        return service.getEventsForAdmin(paramsAdmin);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @RequestBody @Valid UpdateEvent dto) {
        log.info("Update event by admin with eventId {} dto {}", eventId, dto);
        return service.updateEventByAdmin(eventId, dto);
    }
}
