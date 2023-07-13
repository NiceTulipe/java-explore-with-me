package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventsRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.enumies.State;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.request.mapper.RequestMapper.toRequestDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventsRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Добавление запроса от текущего пользователя на участие в событии
     */
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (userId == null || eventId == null) {
            throw new BadRequestException("Некорректный запрос");
        }
        User user = checkUser(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено."));
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии.");
        }
        if (event.getState().equals(State.PENDING) || event.getState().equals(State.CANCELED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии.");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= event.getConfirmedRequests())
            throw new ConflictException("У события достигнут лимит запросов на участие.");
        Request request;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request = Request.builder()
                    .eventId(eventId)
                    .created(LocalDateTime.now())
                    .requester(user)
                    .status(State.CONFIRMED)
                    .build();
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            request = Request.builder()
                    .eventId(eventId)
                    .created(LocalDateTime.now())
                    .requester(user)
                    .status(State.PENDING)
                    .build();
        }
        eventRepository.save(event);
        return toRequestDto(requestRepository.save(request));
    }

    /**
     * Получение информации о заявках текущего пользователя на участие в чужих событиях
     */
    public List<ParticipationRequestDto> getRequestsForUser(Long userId) {
        User user = checkUser(userId);
        List<Request> requests = requestRepository.findByRequester(user);
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Отмена своего запроса на участие в событии
     */
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUser(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запроса с текущим id не найдено."));
        request.setStatus(State.CANCELED);
        return toRequestDto(requestRepository.save(request));
    }

    private User checkUser(Long idUser) {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id  не найден"));
    }
}
