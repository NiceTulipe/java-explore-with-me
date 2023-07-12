package ru.practicum.mapper;

import ru.practicum.dto.ViewStatDTO;
import ru.practicum.model.ViewStats;

import java.util.List;
import java.util.stream.Collectors;

public class ViewStatMapper {
    public static ViewStatDTO toViewStatsDto(ViewStats viewStats) {
        return ViewStatDTO.builder()
                .app(viewStats.getApp())
                .uri(viewStats.getUri())
                .hits(viewStats.getHits())
                .build();
    }
}
