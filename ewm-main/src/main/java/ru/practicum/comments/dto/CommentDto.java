package ru.practicum.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static ru.practicum.utility.UtilityClass.pattern;

@Getter
@Builder
public class CommentDto {
    private Long id;
    private String authorName;
    private String text;
    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = pattern)
    private LocalDateTime created;
}
