package ru.practicum.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitDTO;
import ru.practicum.dto.ViewStatDTO;
import ru.practicum.stats.mapper.HitMapper;
import ru.practicum.stats.service.HitService;


import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
public class ServiceController {

    private final HitService hitService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitDTO create(@RequestBody HitDTO hitDTO) {
        log.info("Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос " +
                "к эндпоинту: 'POST /hit");
        return hitService.creatHit(hitDTO);
    }

    @GetMapping("/stats")
    public List<ViewStatDTO> getStats(
            @RequestParam(name = "start", required = false) @DateTimeFormat(pattern = HitMapper.pattern) LocalDateTime start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(pattern = HitMapper.pattern) LocalDateTime end,
            @RequestParam(name = "uris", required = false) List<String> uris,
            @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        log.info("Получение запрос к эндпоинту : GET /stats на вывод статистики по посещениям с {} по {}, " +
                "uris={}, unique={}", start, end, uris, unique);
        return hitService.getStats(start, end, uris, unique);
    }

}
