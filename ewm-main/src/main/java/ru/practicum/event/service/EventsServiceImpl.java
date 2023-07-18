package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.client.StatClient;
import ru.practicum.dto.HitDTO;
import ru.practicum.dto.ViewStatDTO;
import ru.practicum.enumies.Sort;
import ru.practicum.enumies.State;
import ru.practicum.enumies.StateAction;
import ru.practicum.event.dao.EventsRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.IncorrectStateException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.location.Location;
import ru.practicum.location.LocationRepository;
import ru.practicum.request.dao.RequestRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.model.User;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static ru.practicum.event.mapper.EventMapper.toEvent;
import static ru.practicum.event.mapper.EventMapper.toEventFullDto;
import static ru.practicum.request.mapper.RequestMapper.toRequestDto;
import static ru.practicum.utility.UtilityClass.formatter;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@ComponentScan(basePackages = {"ru.practicum.client"})
public class EventsServiceImpl implements EventsService {
    private final EventsRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatClient statisticClient;

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        if (dto.getPaid() == null) {
            dto.setPaid(false);
        }
        if (dto.getParticipantLimit() == null) {
            dto.setParticipantLimit(0L);
        }
        if (dto.getRequestModeration() == null) {
            dto.setRequestModeration(true);
        }
        LocalDateTime nowDateTime = LocalDateTime.now();
        checkDateTimeForDto(nowDateTime, dto.getEventDate());
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id not found"));
        User user = getUserModel(userId);
        locationRepository.save(dto.getLocation());
        Event event = toEvent(dto, category, user, nowDateTime);
        return toEventFullDto(eventRepository.save(event), 0L);
    }

    public List<EventsShortDto> getEventsFromUser(Long userId, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from, size);
        User user = getUserModel(userId);
        List<Event> events = eventRepository.findByInitiator(user, page);
        Map<Long, Long> hits = getStatisticFromListEvents(events);
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .peek(e -> e.setViews(hits.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public EventFullDto getEventWithOwner(Long userId, Long eventId) {
        checkUser(userId);
        Event event = findEventById(eventId);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, 0L);
        Map<Long, Long> hits = getStatisticFromListEvents(List.of(event));
        eventFullDto.setViews(hits.getOrDefault(event.getId(), 0L));
        return eventFullDto;
    }

    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEvent dto) {
        Event event = findEventById(eventId);
        checkUser(userId);
        if (dto.getEventDate() != null) {
            checkDateTimeForDto(LocalDateTime.now(), dto.getEventDate());
        }
        if (!(event.getState().equals(State.CANCELED) || event.getState().equals(State.PENDING))) {
            throw new IncorrectStateException("Incorrect status, cannot be updated.");
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(State.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(State.CANCELED);
                    break;
                default:
                    throw new IncorrectStateException("Incorrect status dto.");
            }
        }
        return getEventFullDto(dto, event);
    }

    public List<EventFullDto> getEventsForAdmin(SearchEventParamsAdmin paramsAdmin) {
        PageRequest page = PageRequest.of(paramsAdmin.getFrom(), paramsAdmin.getSize());
        List<State> stateList = null;
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (paramsAdmin.getStates() != null) {
            stateList = paramsAdmin.getStates().stream()
                    .map(State::valueOf)
                    .collect(Collectors.toList());
        }
        if (paramsAdmin.getRangeStart() != null) {
            start = paramsAdmin.getRangeStart();
        }
        if (paramsAdmin.getRangeEnd() != null) {
            end = paramsAdmin.getRangeEnd();
        }
        List<Event> events = eventRepository.getEventsWithUsersStatesCategoriesDateTime(
                paramsAdmin.getUsers(), stateList, paramsAdmin.getCategories(), start, end, page);
        Map<Long, Long> hits = getStatisticFromListEvents(events);
        return events.stream()
                .map(e -> toEventFullDto(e, hits.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEvent dto) {
        Event event = findEventById(eventId);
        if (dto.getEventDate() != null) {
            if (LocalDateTime.now().plusHours(1).isAfter(dto.getEventDate())) {
                throw new BadRequestException("Cannot be updated, the date for event " +
                        "must be 1 hour later from the current moment");
            }
        } else {
            if (dto.getStateAction() != null) {
                if (dto.getStateAction().equals(StateAction.PUBLISH_EVENT) &&
                        LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
                    throw new IncorrectStateException("Cannot be updated, the date for published event " +
                            "must be 1 hour later from the current moment");
                }
                if (dto.getStateAction().equals(StateAction.PUBLISH_EVENT) && !(event.getState().equals(State.PENDING))) {
                    throw new IncorrectStateException("Incorrect status. An event can be published only " +
                            "if state = waiting for publication");
                }
                if (dto.getStateAction().equals(StateAction.REJECT_EVENT) && event.getState().equals(State.PUBLISHED)) {
                    throw new IncorrectStateException("Incorrect status. An event can be rejected only " +
                            "if event not published");
                }
            }
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case REJECT_EVENT:
                    event.setState(State.CANCELED);
                    break;
                case PUBLISH_EVENT:
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                default:
                    throw new IncorrectStateException("Incorrect status dto.");
            }
        }
        return getEventFullDto(dto, event);
    }

    public List<EventsShortDto> getEventsWithFilters(SearchEventParamsPublic paramsPublic,
                                                     HttpServletRequest request) {
        PageRequest page = PageRequest.of(paramsPublic.getFrom(), paramsPublic.getSize());
        List<Event> events = new ArrayList<>();
        checkDateTime(paramsPublic.getRangeStart(), paramsPublic.getRangeEnd());
        if (paramsPublic.getOnlyAvailable()) {
            if (paramsPublic.getSort() == null) {
                events = eventRepository.getAvailableEventsWithFiltersDateSorted(
                        paramsPublic.getText(), State.PUBLISHED, paramsPublic.getCategories(),
                        paramsPublic.getPaid(), paramsPublic.getRangeStart(),
                        paramsPublic.getRangeEnd(), page);
            } else {
                switch (Sort.valueOf(paramsPublic.getSort())) {
                    case EVENT_DATE:
                        events = eventRepository.getAvailableEventsWithFiltersDateSorted(
                                paramsPublic.getText(), State.PUBLISHED, paramsPublic.getCategories(),
                                paramsPublic.getPaid(), paramsPublic.getRangeStart(),
                                paramsPublic.getRangeEnd(), page);
                        addStatistic(request);
                        Map<Long, Long> hits = getStatisticFromListEvents(events);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .peek(e -> e.setViews(hits.get(e.getId())))
                                .collect(Collectors.toList());
                    case VIEWS:
                        events = eventRepository.getAvailableEventsWithFilters(
                                paramsPublic.getText(), State.PUBLISHED, paramsPublic.getCategories(),
                                paramsPublic.getPaid(), paramsPublic.getRangeStart(),
                                paramsPublic.getRangeEnd(), page);
                        addStatistic(request);
                        Map<Long, Long> hits3 = getStatisticFromListEvents(events);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .sorted(Comparator.comparing(EventsShortDto::getViews))
                                .peek(e -> e.setViews(hits3.get(e.getId())))
                                .collect(Collectors.toList());
                }
            }
        } else {
            if (paramsPublic.getSort() == null) {
                events = eventRepository.getAllEventsWithFiltersDateSorted(
                        paramsPublic.getText(), State.PUBLISHED, paramsPublic.getCategories(),
                        paramsPublic.getPaid(), paramsPublic.getRangeStart(),
                        paramsPublic.getRangeEnd(), page);
            } else {
                switch (Sort.valueOf(paramsPublic.getSort())) {
                    case EVENT_DATE:
                        events = eventRepository.getAllEventsWithFiltersDateSorted(
                                paramsPublic.getText(), State.PUBLISHED, paramsPublic.getCategories(),
                                paramsPublic.getPaid(), paramsPublic.getRangeStart(),
                                paramsPublic.getRangeEnd(), page);
                        addStatistic(request);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .collect(Collectors.toList());
                    case VIEWS:
                        events = eventRepository.getAllEventsWithFilters(
                                paramsPublic.getText(), State.PUBLISHED, paramsPublic.getCategories(),
                                paramsPublic.getPaid(), paramsPublic.getRangeStart(),
                                paramsPublic.getRangeEnd(), page);
                        addStatistic(request);
                        Map<Long, Long> hits = getStatisticFromListEvents(events);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .sorted(Comparator.comparing(EventsShortDto::getViews))
                                .peek(e -> e.setViews(hits.get(e.getId())))
                                .collect(Collectors.toList());
                }
            }
        }
        addStatistic(request);
        Map<Long, Long> hits = getStatisticFromListEvents(events);
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .peek(e -> e.setViews(hits.get(e.getId())))
                .collect(Collectors.toList());
    }

    public EventFullDto getEventWithFullInfoById(Long id, HttpServletRequest request) {
        Event event = findEventById(id);
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Event not published yet");
        }
        addStatistic(request);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, 0L);
        Map<Long, Long> hits = getStatisticFromListEvents(List.of(event));
        eventFullDto.setViews(hits.get(event.getId()));
        return eventFullDto;
    }

    public List<ParticipationRequestDto> getRequestsForUserForThisEvent(Long userId, Long eventId) {
        checkUser(userId);
        checkEvent(eventId);
        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult changeRequestsStatus(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest dto) {
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        checkUser(userId);
        Event event = findEventById(eventId);
        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0L)) {
            throw new ConflictException("Approved don't need");
        }
        long limitBalance = event.getParticipantLimit() - event.getConfirmedRequests();
        if (event.getParticipantLimit() != 0 && limitBalance <= 0) {
            throw new ConflictException("Event has reached the limit of requests");
        }
        if (dto.getStatus().equals(State.REJECTED.toString())) {
            for (Long requestId : dto.getRequestIds()) {
                Request request = requestRepository.findById(requestId)
                        .orElseThrow(() -> new NotFoundException("request with id = " + requestId + " not found"));
                if (request.getStatus().equals(State.PENDING)) {
                    request.setStatus(State.REJECTED);
                    rejectedRequests.add(toRequestDto(request));
                }
            }
        }
        for (int i = 0; i < dto.getRequestIds().size(); i++) {
            if (limitBalance != 0) {
                int finalI1 = i;
                Request request = requestRepository.findById(dto.getRequestIds().get(i))
                        .orElseThrow(() -> new NotFoundException("request with id = " + finalI1 + " not found"));
                if (request.getStatus().equals(State.PENDING)) {
                    request.setStatus(State.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    eventRepository.save(event);
                    confirmedRequests.add(toRequestDto(request));
                    limitBalance--;
                }
            } else {
                int finalI = i;
                Request request = requestRepository.findById(dto.getRequestIds().get(i))
                        .orElseThrow(() -> new NotFoundException("request with id  = " + finalI + " not found"));
                if (request.getStatus().equals(State.PENDING)) {
                    request.setStatus(State.REJECTED);
                    rejectedRequests.add(toRequestDto(request));
                }
            }
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private void addStatistic(HttpServletRequest request) {
        String app = "ewm-main";
        statisticClient.addStatistic(HitDTO.builder()
                .app(app)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(formatter))
                .build());
    }

    private EventFullDto getEventFullDto(UpdateEvent dto, Event event) {
        Event updatedEvent = updateEventFields(event, dto);
        Event updatedEventFromDB = eventRepository.save(updatedEvent);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, 0L);
        Map<Long, Long> hits = getStatisticFromListEvents(List.of(updatedEventFromDB));
        eventFullDto.setViews(hits.getOrDefault(event.getId(), 0L));
        return eventFullDto;
    }

    private Map<Long, Long> getStatisticFromListEvents(List<Event> events) {
        List<Long> idEvents = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        String start = LocalDateTime.now().minusYears(100).format(formatter);
        String end = LocalDateTime.now().format(formatter);
        String eventsUri = "/events/";
        List<String> uris = idEvents.stream().map(id -> eventsUri + id).collect(Collectors.toList());
        List<ViewStatDTO> viewStatDto = statisticClient.getStatistic(start, end, uris, true);
        Map<Long, Long> hits = new HashMap<>();
        for (ViewStatDTO statsDto : viewStatDto) {
            String uri = statsDto.getUri();
            hits.put(Long.parseLong(uri.substring(eventsUri.length())), statsDto.getHits());
        }
        return hits;
    }

    private void checkDateTime(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            start = LocalDateTime.now().minusYears(100);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }
        if (start.isAfter(end)) {
            throw new BadRequestException("Incorrect request, start date > end date");
        }
    }

    private Event updateEventFields(Event event, UpdateEvent dto) {
        ofNullable(dto.getAnnotation()).ifPresent(event::setAnnotation);
        ofNullable(dto.getCategory()).ifPresent(category -> event.setCategory(categoryRepository.findById(category)
                .orElseThrow(() -> new NotFoundException("category with id not found"))));
        ofNullable(dto.getDescription()).ifPresent(event::setDescription);
        ofNullable(dto.getEventDate()).ifPresent(
                event::setEventDate);
        if (dto.getLocation() != null) {
            List<Location> location = locationRepository.findByLatAndLon(dto.getLocation().getLat(),
                    dto.getLocation().getLon());
            if (location.isEmpty()) {
                locationRepository.save(dto.getLocation());
            }
            event.setLocation(dto.getLocation());
        }
        ofNullable(dto.getPaid()).ifPresent(event::setPaid);
        ofNullable(dto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        ofNullable(dto.getRequestModeration()).ifPresent(event::setRequestModeration);
        ofNullable(dto.getTitle()).ifPresent(event::setTitle);
        return event;
    }

    private void checkDateTimeForDto(LocalDateTime nowDateTime, LocalDateTime dtoDateTime) {
        if (nowDateTime.plusHours(2).isAfter(dtoDateTime)) {
            throw new BadRequestException("error the date for event " +
                    "must be 2 hour later from the current moment");
        }
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("user not found");
        }
    }

    private User getUserModel(Long idUser) {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("user with id not found"));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id not found"));
    }

    private void checkEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found");
        }
    }
}
