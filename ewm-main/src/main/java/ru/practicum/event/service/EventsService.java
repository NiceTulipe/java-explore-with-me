package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventsService {
    EventFullDto createEvent(Long userId, NewEventDto dto);

    List<EventsShortDto> getEventsFromUser(Long userId, Integer from, Integer size);

    EventFullDto getEventWithOwner(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEvent dto);

    List<EventFullDto> getEventsForAdmin(SearchEventParamsAdmin paramsAdmin);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEvent dto);

    List<ParticipationRequestDto> getRequestsForUserForThisEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestsStatus(Long userId,
                                                        Long eventId,
                                                        EventRequestStatusUpdateRequest dto);

    List<EventsShortDto> getEventsWithFilters(SearchEventParamsPublic paramsPublic,
                                              HttpServletRequest request);

    EventFullDto getEventWithFullInfoById(Long id, HttpServletRequest request);
}
