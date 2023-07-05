package ru.practicum.stats.stats.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class HitDTO {
    Long id;
    String app;
    String uri;
    String ip;
    LocalDateTime timestamp;
}