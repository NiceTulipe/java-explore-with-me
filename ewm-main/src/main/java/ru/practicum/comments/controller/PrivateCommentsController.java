package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
public class PrivateCommentsController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid NewCommentDto commentDto) {
        log.info("Create Comment from userId {}, eventId {}, commentDto {}", userId, eventId, commentDto);
        return commentService.createComment(userId, eventId, commentDto);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long eventId,
                              @PathVariable Long id) {
        log.info("Delete comment with userId {}, eventId {}, commentId {}", userId, eventId, id);
        commentService.deleteComment(userId, eventId, id);
    }

    @PatchMapping("{id}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @PathVariable Long id,
                                    @RequestBody @Valid NewCommentDto commentDto) {
        log.info("Update comment with userId {}, eventId {}, commentId {}, commentDto {}",
                userId, eventId, id, commentDto);
        return commentService.updateComment(userId, eventId, id, commentDto);
    }
}
