package ru.practicum.stats.mapper;

import ru.practicum.dto.ViewStatDTO;
import ru.practicum.stats.model.ViewStats;

public class ViewStatMapper {
    public static ViewStatDTO toViewStatsDto(ViewStats viewStats) {
        return ViewStatDTO.builder()
                .app(viewStats.getApp())
                .uri(viewStats.getUri())
                .hits(viewStats.getHits())
                .build();
    }
}
