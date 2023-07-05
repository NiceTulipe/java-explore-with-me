package ru.practicum.stats.stats.mapper;

import ru.practicum.stats.stats.dto.ViewStatDTO;
import ru.practicum.stats.stats.model.ViewStats;

import java.util.List;
import java.util.stream.Collectors;

public class ViewStatMapper {
    public static List<ViewStatDTO> toViewStatsDTO(List<ViewStats> viewStats) {
        return viewStats
                .stream()
                .map(viewStat -> new ViewStatDTO(
                        viewStat.getApp(),
                        viewStat.getUri(),
                        viewStat.getHits()))
                .collect(Collectors.toList());
    }
}
