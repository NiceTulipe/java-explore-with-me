package ru.practicum.stats.stats.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.stats.model.EndpointHit;
import ru.practicum.stats.stats.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT new ViewStats(EH.uri, EH.app, COUNT(DISTINCT EH.ip))" +
            "FROM EndpointHit AS EH " +
            "WHERE EH.timestamp BETWEEN :start AND :end " +
            "AND EH.uri IN :uris " +
            "GROUP BY EH.app, EH.uri " +
            "ORDER BY COUNT (EH.ip) DESC ")
    List<ViewStats> getViewStatsWithUniqIp(@Param("uris") List<String> uris,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);


    @Query("SELECT new ViewStats(EH.uri, EH.app, COUNT(EH.ip))" +
            "FROM EndpointHit AS EH " +
            "WHERE EH.timestamp BETWEEN :start AND :end " +
            "AND EH.uri IN :uris " +
            "GROUP BY EH.app, EH.uri " +
            "ORDER BY COUNT (EH.ip) DESC ")
    List<ViewStats> getViewStatsAll(@Param("uris") List<String> uris,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("SELECT new ViewStats(EH.uri, EH.app, COUNT(EH.ip))" +
            "FROM EndpointHit AS EH " +
            "WHERE EH.timestamp BETWEEN :start AND :end " +
            "GROUP BY EH.app, EH.uri " +
            "ORDER BY COUNT (EH.ip) DESC ")
    List<ViewStats> getViewStatsWithoutUri(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}