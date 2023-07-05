package ru.practicum.stats.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.stats.dao.HitRepository;
import ru.practicum.stats.stats.dto.HitDTO;
import ru.practicum.stats.stats.dto.HitRequestDTO;
import ru.practicum.stats.stats.dto.ViewStatDTO;
import ru.practicum.stats.stats.mapper.HitMapper;
import ru.practicum.stats.stats.mapper.ViewStatMapper;
import ru.practicum.stats.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    @Override
    public HitDTO creatHit(HitRequestDTO hitRequestDTO) {
        EndpointHit endpointHit = HitMapper.toHit(hitRequestDTO);

        return HitMapper
                .toDto(hitRepository.save(endpointHit));
    }

    @Override
    public List<ViewStatDTO> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (uris.isEmpty()) {
            return ViewStatMapper
                    .toViewStatsDTO(hitRepository.getViewStatsWithoutUri(start, end));
        }
        if (unique) {
            return ViewStatMapper.toViewStatsDTO((hitRepository.getViewStatsWithUniqIp(uris, start, end)));
        } else {
            return ViewStatMapper
                    .toViewStatsDTO(hitRepository.getViewStatsAll(uris, start, end));
        }
    }
}
