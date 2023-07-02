package ru.practicum.stats.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.stats.dto.HitDTO;
import ru.practicum.stats.stats.dto.HitRequestDTO;
import ru.practicum.stats.stats.dto.ViewStatDTO;
import ru.practicum.stats.stats.service.HitService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
public class ServiceController {

    private final HitService hitService;

    @PostMapping("/hit")
    public HitDTO create(@RequestBody @Valid HitRequestDTO hitRequestDTO) {
        log.info("Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос " +
                "к эндпоинту: 'POST /hit");
        return hitService.creatHit(hitRequestDTO);
    }

    @GetMapping("/stats")
    public List<ViewStatDTO> getStats(@RequestParam("start") LocalDateTime start,
                                      @RequestParam("end") LocalDateTime end,
                                      @RequestParam(value = "uris", required = false,
                                              defaultValue = "") List<String> uris,
                                      @RequestParam(value = "unique", required = false,
                                              defaultValue = "false") Boolean unique) {
        log.info("Получение запрос к эндпоинту : GET /stats на вывод статистики по посещениям с {} по {}, " +
                "uris={}, unique={}", start, end, uris, unique);
        return hitService.getStats(start, end, uris, unique);
    }

}
