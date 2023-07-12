package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService service;

    @GetMapping
    public List<NewCategoryDto> getCategories(@RequestParam(required = false, defaultValue = "0") Integer from,
                                              @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Get categories from {}, size {}", from, size);
        return service.getCategories(from, size);
    }

    @GetMapping("/{id}")
    public NewCategoryDto getCategory(@PathVariable Long id) {
        log.info("Get category with id {}", id);
        return service.getCategory(id);
    }
}
