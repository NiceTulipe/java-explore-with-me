package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventsRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.IncorrectStateException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static ru.practicum.category.mapper.CategoryMapper.toCategory;
import static ru.practicum.category.mapper.CategoryMapper.toCategoryDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventsRepository eventsRepository;

    /**
     * Добавление новой категории
     */
    @Transactional
    public NewCategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (newCategoryDto != null) {
            Category category = toCategory(newCategoryDto);
            return saveCategory(category);
        }
        return null;
    }


    /**
     * Удаление категории
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryModel(id);
        List<Event> events = eventsRepository.findByCategory(category);
        if (!events.isEmpty()) {
            throw new ConflictException("Нельзя удалить категорию. Существуют события, связанные с категорией.");
        }
        categoryRepository.deleteById(id);

    }

    /**
     * Изменение категории
     */
    @Transactional
    public NewCategoryDto updateCategory(Long id, NewCategoryDto newCategoryDto) {
        Category category = getCategoryModel(id);
        ofNullable(newCategoryDto.getName()).ifPresent(category::setName);
        try {
            return CategoryMapper.toCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            log.warn("Нарушена уникальность имени категории {} уже используется", newCategoryDto.getName());
            throw new IncorrectStateException("Имя категории должно быть уникальным, "
                    + newCategoryDto.getName() + " уже используется");
        } catch (Exception e) {
            log.warn("Запрос на добавлении категории {} составлен не корректно", newCategoryDto.getName());
            throw new BadRequestException("Запрос на добавлении категории " + newCategoryDto.getName() + " составлен не корректно ");
        }

    }

    /**
     * Получение категорий
     */
    public List<NewCategoryDto> getCategories(Integer from, Integer size) {
        PageRequest page = PageRequest.of(from, size);
        return categoryRepository.findAll(page).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение информации о категории по ее идентификатору
     */
    public NewCategoryDto getCategory(Long id) {
        Category category = getCategoryModel(id);
        return toCategoryDto(category);
    }

    private Category getCategoryModel(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категории с таким id не найдено"));
    }

    private NewCategoryDto saveCategory(Category category) {
        try {
            return CategoryMapper.toCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            log.warn("Нарушена уникальность имени категории {} уже используется", category.getName());
            throw new IncorrectStateException("Имя категории должно быть уникальным, "
                    + category.getName() + " уже используется");
        }
    }

}
