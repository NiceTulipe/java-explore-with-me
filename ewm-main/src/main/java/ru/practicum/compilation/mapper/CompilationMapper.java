package ru.practicum.compilation.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventsShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class CompilationMapper {

    public static CompilationDto toCompilationDto(Compilation compilation, List<EventsShortDto> eventsShortDtos) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(eventsShortDtos)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents().stream()
                        .map(EventMapper::toEventShortDto)
                        .collect(Collectors.toList()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static Compilation toCompilation(NewCompilationDto dto, Set<Event> eventSet) {
        return Compilation.builder()
                .events(eventSet)
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }
}
