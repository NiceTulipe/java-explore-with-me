package ru.practicum.stats.model;

import lombok.*;

import javax.persistence.Id;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ViewStats {
    @Id
    private String app;
    private String uri;
    private Long hits;
}
