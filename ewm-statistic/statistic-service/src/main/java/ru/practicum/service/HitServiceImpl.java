package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.HitRepository;
import ru.practicum.mapper.HitMapper;
import ru.practicum.mapper.ViewStatMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.dto.HitDTO;
import ru.practicum.dto.ViewStatDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    @Override
    public HitDTO creatHit(HitDTO hitDTO) {
        EndpointHit endpointHit = HitMapper.toHit(hitDTO);

        return HitMapper
                .toDto(hitRepository.save(endpointHit));
    }

    @Override
    public List<ViewStatDTO> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        checkDate(start, end);
        if (uris == null) {
            if (unique) {
                return hitRepository.getStatisticsWithUniqueIp(start, end).stream()
                        .map(ViewStatMapper::toViewStatsDto)
                        .collect(Collectors.toList());
            } else {
                return hitRepository.getAllStatistics(start, end).stream()
                        .map(ViewStatMapper::toViewStatsDto)
                        .collect(Collectors.toList());
            }
        } else {
            if (unique) {
                return hitRepository.getStatisticsWithUniqueIpAndUris(start, end, uris).stream()
                        .map(ViewStatMapper::toViewStatsDto)
                        .collect(Collectors.toList());
            } else {
                return hitRepository.getAllStatisticsWithUris(start, end, uris).stream()
                        .map(ViewStatMapper::toViewStatsDto)
                        .collect(Collectors.toList());
            }
        }
    }
