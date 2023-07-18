package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventsShortDto;
import ru.practicum.event.dto.SearchEventParamsPublic;
import ru.practicum.event.service.EventsService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

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
    public List<EventsShortDto> getEventsWithFilters(@Valid SearchEventParamsPublic paramsPublic,
                                                     HttpServletRequest request) {
        log.info("Get Events from text {}, categories {}, paid {}, rangeStart {}, rangeEnd {}, onlyAvailable {}, " +
                        "sort {}, from {}, size {}",
                paramsPublic.getText(), paramsPublic.getCategories(),
                paramsPublic.getPaid(), paramsPublic.getRangeStart(),
                paramsPublic.getRangeEnd(), paramsPublic.getOnlyAvailable(),
                paramsPublic.getSort(), paramsPublic.getFrom(),
                paramsPublic.getSize());
        return service.getEventsWithFilters(paramsPublic, request);
    }
}
