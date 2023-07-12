package ru.practicum.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewStats {
    @Id
    private String app;
    private String uri;
    private Long hits;
}
