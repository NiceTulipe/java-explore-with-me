package ru.practicum.stats.stats.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.stats.stats.dto.HitDTO;
import ru.practicum.stats.stats.dto.HitRequestDTO;
import ru.practicum.stats.stats.model.EndpointHit;

@UtilityClass
public class HitMapper {
    public static EndpointHit toHit(HitRequestDTO hitRequestDTO) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(hitRequestDTO.getApp());
        endpointHit.setUri(hitRequestDTO.getUri());
        endpointHit.setIp(hitRequestDTO.getIp());
        endpointHit.setTimestamp(hitRequestDTO.getTimestamp());
        return endpointHit;
    }

    public static HitDTO toDto(EndpointHit endpointHit) {
        return new HitDTO(
                endpointHit.getId(),
                endpointHit.getApp(),
                endpointHit.getUri(),
                endpointHit.getIp(),
                endpointHit.getTimestamp());
    }
}
