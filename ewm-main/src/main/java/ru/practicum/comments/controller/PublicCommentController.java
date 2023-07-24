package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.SearchCommentParams;
import ru.practicum.comments.service.CommentService;
import ru.practicum.event.dto.SearchEventParamsAdmin;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/comments")
@RequiredArgsConstructor
public class PublicCommentController {
    private final CommentService service;

    @GetMapping("/{id}")
    public CommentDto getComment(@PathVariable Long id) {
        log.info("Get comment with id {}", id);
        return service.getComment(id);
    }

    @GetMapping("/event/{idEvent}")
    public List<CommentDto> getCommentForEvent(@PathVariable(name = "idEvent") Long idEvent,
                                               @RequestParam(required = false, defaultValue = "0") Integer from,
                                               @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Get comments for Event idEvent {}, from {}, size {}", idEvent, from, size);
        return service.getCommentForEvent(idEvent, from, size);
    }

    @GetMapping
    public List<CommentDto> getCommentsByFilters(@Valid SearchCommentParams commentParams) {
//            @RequestParam(required = false, name = "text") String text,
//                                                 @RequestParam(required = false, name = "idEvent") Long idEvent,
//                                                 @RequestParam(required = false, name = "idUser") Long idUser,
//                                                 @RequestParam(required = false, defaultValue = "0") Integer from,
//                                                 @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Get comments by Filters text {}, idEvent {}, idUser {}, from {}, size {}",
                commentParams.getText(), commentParams.getIdEvent(), commentParams.getIdUser(),
                commentParams.getFrom(), commentParams.getSize());
        return service.getCommentsByFilters(commentParams);
    }

}
