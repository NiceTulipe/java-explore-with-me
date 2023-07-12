package ru.practicum.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HitDTO {
    @NotBlank
    @Size(max = 256)
    private String app;
    @NotBlank
    @Size(max = 512)
    private String uri;
    @NotBlank
    @Size(max = 64)
    private String ip;
    private String timestamp;
}