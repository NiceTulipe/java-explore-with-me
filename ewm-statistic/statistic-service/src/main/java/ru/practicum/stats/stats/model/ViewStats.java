package ru.practicum.stats.stats.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewStats {
    @Id
    private String app;
    private String uri;
    private Long hits;
}
