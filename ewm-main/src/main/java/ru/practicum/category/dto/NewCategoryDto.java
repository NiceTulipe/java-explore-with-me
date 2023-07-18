package ru.practicum.category.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Builder
public class NewCategoryDto {
    private Long id;
    @NotBlank
    @Size(max = 50)
    private String name;
}
