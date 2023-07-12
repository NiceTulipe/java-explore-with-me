package ru.practicum.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatDTO {
    private String app;
    private String uri;
    private Long hits;
}
