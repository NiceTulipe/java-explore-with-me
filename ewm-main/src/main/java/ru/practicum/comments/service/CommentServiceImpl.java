package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.SearchCommentParams;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.EventComment;
import ru.practicum.comments.dao.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.dao.EventsRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.enumies.State;
import ru.practicum.user.model.User;
import ru.practicum.user.dao.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.comments.mapper.CommentMapper.toComment;
import static ru.practicum.comments.mapper.CommentMapper.toCommentDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventsRepository eventsRepository;

    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto commentDto) {
        User user = checkUser(userId);
        Event event = findEventById(eventId);
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Cant add comment. Event not published yet");
        }
        EventComment eventComment = toComment(commentDto, event, user);
        EventComment newEventComment = commentRepository.save(eventComment);
        return toCommentDto(newEventComment);
    }

    public CommentDto getComment(Long commentId) {
        return toCommentDto(findCommentById(commentId));
    }

    @Transactional
    public void deleteComment(Long userId, Long eventId, Long commentId) {
        User user = checkUserAndEvent(userId, eventId);
        EventComment eventComment = findCommentById(commentId);
        if (!eventComment.getAuthor().getId().equals(user.getId())) {
            throw new ConflictException("Delete comment can only author");
        }
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto commentDto) {
        User user = checkUserAndEvent(userId, eventId);
        EventComment eventComment = findCommentById(commentId);
        if (!eventComment.getAuthor().getId().equals(user.getId())) {
            throw new ConflictException("Edit comment can only author");
        }
        eventComment.setText(commentDto.getText());
        EventComment updatedEventComment = eventComment;
        return toCommentDto(updatedEventComment);
    }

    public List<CommentDto> getCommentForEvent(Long idEvent, Integer from, Integer size) {
        Event event = findEventById(idEvent);
        PageRequest page = PageRequest.of(from, size);
        List<EventComment> eventComments = commentRepository.findByEvent(event, page);
        return eventComments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsByFilters(SearchCommentParams commentParams) {
        User user = null;
        Event event = null;
        PageRequest page = PageRequest.of(commentParams.getFrom(), commentParams.getSize());
        if (commentParams.getIdUser() != null) {
            user = checkUser(commentParams.getIdUser());
        }
        if (commentParams.getIdEvent() != null) {
            event = findEventById(commentParams.getIdEvent());
        }
        List<EventComment> eventComments = commentRepository.getCommentsByFilters(commentParams.getText(), user, event, page);
        return eventComments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCommentByAdmin(Long id) {
        commentRepository.deleteById(id);
    }

    @Transactional
    public CommentDto updateCommentByAdmin(Long commentId, NewCommentDto commentDto) {
        EventComment eventComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with ID not found"));
        eventComment.setText(commentDto.getText());
        EventComment updatedEventComment = eventComment;
        return toCommentDto(updatedEventComment);
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID not found"));
    }

    private Event findEventById(Long eventId) {
        return eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with ID not found"));
    }

    private User checkUserAndEvent(Long userId, Long eventId) {
        findEventById(eventId);
        return checkUser(userId);
    }

    private EventComment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with ID not found"));
    }
}
