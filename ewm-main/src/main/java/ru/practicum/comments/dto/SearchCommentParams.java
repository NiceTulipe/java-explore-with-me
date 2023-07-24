package ru.practicum.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utility.UtilityClass.pattern;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCommentParams {
    private String text;
    private Long idEvent;
    private Long idUser;
    @PositiveOrZero
    private Integer from = 0;
    @Positive
    private Integer size = 10;
}
