package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.client.StatClient;
import ru.practicum.dto.HitDTO;
import ru.practicum.dto.ViewStatDTO;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventsShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEvent;
import ru.practicum.event.model.Event;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repository.EventsRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.IncorrectStateException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.location.Location;
import ru.practicum.location.LocationRepository;
import ru.practicum.modelstate.Sort;
import ru.practicum.modelstate.State;
import ru.practicum.modelstate.StateAction;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.dao.UserRepository;

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

    /**
     * Добавление нового события
     */
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
                .orElseThrow(() -> new NotFoundException("Категория с таким id  не найдена"));
        User user = getUserModel(userId);
        locationRepository.save(dto.getLocation());
        Event event = toEvent(dto, category, user, nowDateTime);
        return toEventFullDto(eventRepository.save(event), 0L);
    }


    /**
     * Получение событий добавленных пользователем
     */
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

    /**
     * Получение полной информации о событии добавленном пользователем
     */
    public EventFullDto getEventWithOwner(Long userId, Long eventId) {
        checkUser(userId);
        Event event = findEventById(eventId);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, 0L);
        Map<Long, Long> hits = getStatisticFromListEvents(List.of(event));
        eventFullDto.setViews(hits.getOrDefault(event.getId(), 0L));
        return eventFullDto;
    }

    /**
     * Изменение события добавленном пользователем
     */
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEvent dto) {
        Event event = findEventById(eventId);
        checkUser(userId);
        if (dto.getEventDate() != null) {
            checkDateTimeForDto(LocalDateTime.now(), dto.getEventDate());
        }
        if (!(event.getState().equals(State.CANCELED) || event.getState().equals(State.PENDING))) {
            throw new IncorrectStateException("Некорректный статус. Изменить можно только отмененные " +
                    "события или события в состоянии ожидания модерации.");
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
                    throw new IncorrectStateException("Некорректный статус dto.");
            }
        }
        return getEventFullDto(dto, event);
    }

    /**
     * Поиск событий с фильтрами для администратора
     */
    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from, size);
        List<State> stateList = null;
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (states != null) {
            stateList = states.stream()
                    .map(State::valueOf)
                    .collect(Collectors.toList());
        }
        if (rangeStart != null) {
            start = rangeStart;
        }
        if (rangeEnd != null) {
            end = rangeEnd;
        }
        List<Event> events = eventRepository.getEventsWithUsersStatesCategoriesDateTime(
                users, stateList, categories, start, end, page);
        Map<Long, Long> hits = getStatisticFromListEvents(events);
        return events.stream()
                .map(e -> toEventFullDto(e, hits.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    /**
     * Редактирование данных события и его статуса (отклонение/публикация) администратором
     */
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEvent dto) {
        Event event = findEventById(eventId);
        if (dto.getEventDate() != null) {
            if (LocalDateTime.now().plusHours(1).isAfter(dto.getEventDate())) {
                throw new BadRequestException("Ошибка. Дата и время на которые намечено событие " +
                        "не может быть раньше, чем через час от текущего момента");
            }
        } else {
            if (dto.getStateAction() != null) {
                if (dto.getStateAction().equals(StateAction.PUBLISH_EVENT) &&
                        LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
                    throw new IncorrectStateException("Ошибка. Дата и время публикуемого события " +
                            "не может быть раньше, чем через час от текущего момента");
                }
                if (dto.getStateAction().equals(StateAction.PUBLISH_EVENT) && !(event.getState().equals(State.PENDING))) {
                    throw new IncorrectStateException("Некорректный статус. Событие можно публиковать, " +
                            "только если оно в состоянии ожидания публикации.");
                }
                if (dto.getStateAction().equals(StateAction.REJECT_EVENT) && event.getState().equals(State.PUBLISHED)) {
                    throw new IncorrectStateException("Некорректный статус. Событие можно отклонить, " +
                            "только если оно еще не опубликовано.");
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
                    throw new IncorrectStateException("Некорректный статус dto.");
            }
        }
        return getEventFullDto(dto, event);
    }

    /**
     * Получение событий с возможностью фильтрации (публичный доступ)
     */
    public List<EventsShortDto> getEventsWithFilters(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                                     Integer size, HttpServletRequest request) {
        PageRequest page = PageRequest.of(from, size);
        List<Event> events = new ArrayList<>();
        checkDateTime(rangeStart, rangeEnd);
        if (onlyAvailable) {
            if (sort == null) {
                events = eventRepository.getAvailableEventsWithFiltersDateSorted(
                        text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
            } else {
                switch (Sort.valueOf(sort)) {
                    case EVENT_DATE:
                        events = eventRepository.getAvailableEventsWithFiltersDateSorted(
                                text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
                        addStatistic(request);
                        Map<Long, Long> hits = getStatisticFromListEvents(events);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .peek(e -> e.setViews(hits.get(e.getId())))
                                .collect(Collectors.toList());
                    case VIEWS:
                        events = eventRepository.getAvailableEventsWithFilters(
                                text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
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
            if (sort == null) {
                events = eventRepository.getAllEventsWithFiltersDateSorted(
                        text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
            } else {
                switch (Sort.valueOf(sort)) {
                    case EVENT_DATE:
                        events = eventRepository.getAllEventsWithFiltersDateSorted(
                                text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
                        addStatistic(request);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .collect(Collectors.toList());
                    case VIEWS:
                        events = eventRepository.getAllEventsWithFilters(
                                text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
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

    /**
     * Получение полной информации об опубликованном событии по его идентификатору
     */
    public EventFullDto getEventWithFullInfoById(Long id, HttpServletRequest request) {
        Event event = findEventById(id);
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Событие еще не опубликовано");
        }
        addStatistic(request);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, 0L);
        Map<Long, Long> hits = getStatisticFromListEvents(List.of(event));
        eventFullDto.setViews(hits.get(event.getId()));
        return eventFullDto;
    }

    /**
     * Получение информации о запросах на участие в событии текущего пользователя
     */
    public List<ParticipationRequestDto> getRequestsForUserForThisEvent(Long userId, Long eventId) {
        checkUser(userId);
        checkEvent(eventId);
        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Изменение статуса (подтверждение/отмена) заявок на участие в событии текущего пользователя
     */
    @Transactional
    public EventRequestStatusUpdateResult changeRequestsStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        checkUser(userId);
        Event event = findEventById(eventId);
        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0L)) {
            throw new ConflictException("Подтверждение заявок не требуется");
        }
        long limitBalance = event.getParticipantLimit() - event.getConfirmedRequests();
        if (event.getParticipantLimit() != 0 && limitBalance <= 0) {
            throw new ConflictException("У события достигнут лимит запросов на участие.");
        }
        if (dto.getStatus().equals(State.REJECTED.toString())) {
            for (Long requestId : dto.getRequestIds()) {
                Request request = requestRepository.findById(requestId)
                        .orElseThrow(() -> new NotFoundException("Запроса c id = " + requestId + " не найдено."));
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
                        .orElseThrow(() -> new NotFoundException("Запроса c id = " + finalI1 + " не найдено."));
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
                        .orElseThrow(() -> new NotFoundException("Запроса c id = " + finalI + " не найдено."));
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

    /**
     * Добавление информации о просмотре события в сервис статистики
     */
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

    /**
     * Получение информации о просмотрах из сервиса статистики
     */
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
            throw new BadRequestException("Некорректный запрос. Дата окончания события задана позже даты старта");
        }
    }

    private Event updateEventFields(Event event, UpdateEvent dto) {
        ofNullable(dto.getAnnotation()).ifPresent(event::setAnnotation);
        ofNullable(dto.getCategory()).ifPresent(category -> event.setCategory(categoryRepository.findById(category)
                .orElseThrow(() -> new NotFoundException("Категория с таким id  не найдена"))));
        ofNullable(dto.getDescription()).ifPresent(event::setDescription);
        ofNullable(dto.getEventDate()).ifPresent(
                event::setEventDate);
        if (dto.getLocation() != null) {
            List<Location> location = locationRepository.findByLatAndLon(dto.getLocation().getLat(), dto.getLocation().getLon());
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
            throw new BadRequestException("Ошибка. Дата и время на которые намечено событие " +
                    "не может быть раньше, чем через два часа от текущего момента");
        }
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private User getUserModel(Long idUser) {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id  не найден"));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено"));
    }

    private void checkEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие не найдено");
        }
    }
}
