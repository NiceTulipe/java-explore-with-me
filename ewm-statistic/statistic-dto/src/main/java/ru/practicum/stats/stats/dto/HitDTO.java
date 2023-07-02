package ru.practicum.stats.stats.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Value
public class HitDTO {
    Long id;
    String app;
    String uri;
    String ip;
    LocalDateTime timestamp;
}