package ru.practicum.comments.dto;

import lombok.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

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
