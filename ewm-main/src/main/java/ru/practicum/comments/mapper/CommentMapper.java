package ru.practicum.comments.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.model.EventComment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static CommentDto toCommentDto(EventComment eventComment) {
        return CommentDto.builder()
                .id(eventComment.getId())
                .authorName(eventComment.getAuthor().getName())
                .text(eventComment.getText())
                .created(eventComment.getCreated())
                .build();
    }

    public static EventComment toComment(NewCommentDto commentDto, Event event, User user) {
        return EventComment.builder()
                .event(event)
                .author(user)
                .text(commentDto.getText())
                .created(LocalDateTime.now())
                .build();
    }

    public static List<CommentDto> toCommentDtos(List<EventComment> comments) {
        return comments.stream()
                .map(comment -> CommentDto.builder()
                        .id(comment.getId())
                        .authorName(comment.getAuthor().getName())
                        .text(comment.getText())
                        .created(comment.getCreated())
                        .build())
                .collect(Collectors.toList());
    }
}