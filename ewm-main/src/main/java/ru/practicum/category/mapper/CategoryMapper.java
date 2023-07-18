package ru.practicum.category.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

@NoArgsConstructor
public class CategoryMapper {
    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .id(newCategoryDto.getId())
                .name(newCategoryDto.getName())
                .build();
    }

    public static NewCategoryDto toCategoryDto(Category category) {
        return NewCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
