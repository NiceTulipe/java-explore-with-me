package ru.practicum.comments.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.comments.model.EventComment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<EventComment, Long> {

    List<EventComment> findByEvent(Event event, Pageable pageable);

    @Query("select c from EventComment as c " +
            "where (:text is null or upper(c.text) like upper(concat('%', :text, '%'))) " +
            "and (:user is null or c.author = :user) " +
            "and (:event is null or c.event = :event) " +
            "order by c.created")
    List<EventComment> getCommentsByFilters(@Param("text") String text,
                                            @Param("user") User user,
                                            @Param("event") Event event,
                                            Pageable pageable);
}
