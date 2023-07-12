package ru.practicum.service;

import ru.practicum.dto.HitDTO;
import ru.practicum.dto.ViewStatDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface HitService {
    HitDTO creatHit(HitDTO hitDTO);

    List<ViewStatDTO> getStats(LocalDateTime start,
                               LocalDateTime end,
                               List<String> uris,
                               Boolean unique);
}
