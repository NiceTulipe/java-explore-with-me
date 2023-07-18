package ru.practicum.compilation.model;

import lombok.*;
import ru.practicum.event.model.Event;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Compilation")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean pinned;
    private String title;
    @ManyToMany
    @JoinTable(name = "Compilations_Events", joinColumns = {@JoinColumn(name = "compilation_id")}, inverseJoinColumns = {@JoinColumn(name = "event_id")})
    Set<Event> events;
}