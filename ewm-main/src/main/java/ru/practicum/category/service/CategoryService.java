package ru.practicum.category.service;

import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    NewCategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);

    NewCategoryDto updateCategory(Long id, NewCategoryDto newCategoryDto);

    List<NewCategoryDto> getCategories(Integer from, Integer size);

    NewCategoryDto getCategory(Long id);
}
