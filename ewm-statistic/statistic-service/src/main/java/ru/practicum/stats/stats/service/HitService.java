package ru.practicum.stats.stats.service;

import ru.practicum.stats.stats.dto.HitDTO;
import ru.practicum.stats.stats.dto.HitRequestDTO;
import ru.practicum.stats.stats.dto.ViewStatDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface HitService {
    HitDTO creatHit(HitRequestDTO hitRequestDTO);

    List<ViewStatDTO> getStats(LocalDateTime start,
                               LocalDateTime end,
                               List<String> uris,
                               Boolean unique);
}
