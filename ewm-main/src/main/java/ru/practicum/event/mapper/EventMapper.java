package ru.practicum.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventsShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.modelstate.State;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

import static ru.practicum.category.mapper.CategoryMapper.toCategoryDto;
import static ru.practicum.user.mapper.UserMapper.toUserShortDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static EventFullDto toEventFullDto(Event event, Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(toUserShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn((event.getPublishedOn() == null) ? null : event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static Event toEvent(NewEventDto newEventDto, Category category, User user, LocalDateTime dateTime) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .confirmedRequests(0L)
                .description(newEventDto.getDescription())
                .createdOn(dateTime)
                .eventDate(newEventDto.getEventDate())
                .initiator(user)
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .state(State.PENDING)
                .title(newEventDto.getTitle())
                .build();
    }

    public static EventsShortDto toEventShortDto(Event event) {
        return EventsShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .initiator(toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .build();
    }
}
