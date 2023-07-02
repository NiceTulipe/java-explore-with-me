package ru.practicum.stats.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
public class HitRequestDTO {
    @NotBlank
    @Size(max = 255)
    String app;
    @NotBlank
    @Size(max = 255)
    String uri;
    @NotBlank
    @Size(max = 255)
    String ip;
    @NotNull
    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
}
