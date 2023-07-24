package ru.practicum.comments.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Builder
public class NewCommentDto {
    private Long id;
    @NotBlank
    @Size(max = 1000)
    private String text;
}
