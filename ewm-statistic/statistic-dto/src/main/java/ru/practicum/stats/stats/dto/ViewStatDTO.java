package ru.practicum.stats.stats.dto;

import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
public class ViewStatDTO {
    String app;
    String uri;
    Long hits;
}
