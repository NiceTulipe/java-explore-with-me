package ru.practicum.comments.service;

import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.SearchCommentParams;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewCommentDto commentDto);

    CommentDto getComment(Long idComment);

    void deleteComment(Long userId, Long eventId, Long id);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto commentDto);

    List<CommentDto> getCommentForEvent(Long idEvent, Integer from, Integer size);

    List<CommentDto> getCommentsByFilters(SearchCommentParams commentParams);

    void deleteCommentByAdmin(Long id);

    CommentDto updateCommentByAdmin(Long commentId, NewCommentDto commentDto);
}
