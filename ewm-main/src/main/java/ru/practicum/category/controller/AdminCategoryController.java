package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NewCategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Create category {}", newCategoryDto);
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        log.info("Delete category {}", id);
        categoryService.deleteCategory(id);
    }

    @PatchMapping("{id}")
    public NewCategoryDto updateCategory(@PathVariable Long id,
                                         @RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Update category id {}", id);
        return categoryService.updateCategory(id, newCategoryDto);
    }

}
