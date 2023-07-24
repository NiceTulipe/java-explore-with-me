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
@RequestMapping(path = "/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long id) {
        log.info("Delete comment by admin with commentId {}", id);
        commentService.deleteCommentByAdmin(id);
    }

    @PatchMapping("{id}")
    public CommentDto updateCommentByAdmin(@PathVariable Long id,
                                           @RequestBody @Valid NewCommentDto commentDto) {
        log.info("Update comment by admin with commentId {}, commentDto {}", id, commentDto);
        return commentService.updateCommentByAdmin(id, commentDto);
    }

}
