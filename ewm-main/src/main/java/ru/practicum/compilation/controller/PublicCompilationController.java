package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    private final CompilationService service;

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        log.info("Get compilation with id {}", compId);
        return service.getCompilation(compId);
    }

    @GetMapping
    public List<CompilationDto> getCompilationsByFilters(@RequestParam(required = false) Boolean pinned,
                                                         @RequestParam(required = false, defaultValue = "0") Integer from,
                                                         @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Get compilations with pined {}, from {}, size {}", pinned, from, size);
        return service.getCompilationsByFilters(pinned, from, size);
    }
}
