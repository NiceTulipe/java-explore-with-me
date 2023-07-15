package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dao.HitRepository;
import ru.practicum.stats.exeption.StartEndRangeException;
import ru.practicum.stats.mapper.HitMapper;
import ru.practicum.stats.mapper.ViewStatMapper;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.dto.HitDTO;
import ru.practicum.dto.ViewStatDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

        private final HitRepository repository;

        @Transactional
        public HitDTO creatHit(HitDTO hitDto) {
            EndpointHit endpointHit  = HitMapper.toHit(hitDto);
            return HitMapper.toDto(repository.save(endpointHit));
        }

        public List<ViewStatDTO> getStats(LocalDateTime start, LocalDateTime end,
                List<String> uris, Boolean unique) {
            checkDate(start, end);
            if (uris == null) {
                if (unique) {
                    return repository.getViewStatsWithoutUriUniqIp(start, end).stream()
                            .map(ViewStatMapper::toViewStatsDto)
                            .collect(Collectors.toList());
                } else {
                    return repository.getViewStatsWithoutUri(start, end).stream()
                            .map(ViewStatMapper::toViewStatsDto)
                            .collect(Collectors.toList());
                }
            } else {
                if (unique) {
                    return repository.getViewStatsWithUniqIp(start, end, uris).stream()
                            .map(ViewStatMapper::toViewStatsDto)
                            .collect(Collectors.toList());
                } else {
                    return repository.getViewStatsAll(start, end, uris).stream()
                            .map(ViewStatMapper::toViewStatsDto)
                            .collect(Collectors.toList());
                }
            }
        }

        private void checkDate(LocalDateTime startTime, LocalDateTime endTime) {
            if (startTime == null || endTime == null) {
                throw new StartEndRangeException("Ошибка времени начала и конца диапазона");
            }
            if (startTime.isAfter(endTime)) {
                throw new StartEndRangeException("Ошибка времени начала и конца диапазона");
            }
        }
    }
