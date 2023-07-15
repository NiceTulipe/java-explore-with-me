package ru.practicum.stats.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.dto.HitDTO;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
public class HitMapper {

    public static final String pattern = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

    public static EndpointHit toHit(HitDTO hitRequestDTO) {
        return EndpointHit.builder()
                .app(hitRequestDTO.getApp())
                .uri(hitRequestDTO.getUri())
                .ip(hitRequestDTO.getIp())
                .timestamp(LocalDateTime.parse(hitRequestDTO.getTimestamp(), formatter))
                .build();
    }

    public static HitDTO toDto(EndpointHit endpointHit) {
        return HitDTO.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp().format(formatter))
                .build();
    }
}
