package ru.practicum.stats.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<EndpointHit, Long> {

    @Query( "select new ru.practicum.stats.model.ViewStats(app, uri, count(distinct ip) as hits) from EndpointHit " +
            "where timestamp between ?1 and ?2 and uri in(?3) " +
            "group by app, uri order by hits desc" )
    List<ViewStats> getStatisticsWithUniqueIpAndUris(LocalDateTime start, LocalDateTime end, List<String> uri);

    @Query(" select new ru.practicum.stats.model.ViewStats(app, uri, count(ip) as hits) from EndpointHit " +
            "where timestamp between ?1 and ?2 and uri in(?3) " +
            "group by app, uri order by hits desc")
    List<ViewStats> getAllStatisticsWithUris(LocalDateTime start, LocalDateTime end, List<String> uri);

    @Query(" select new ru.practicum.stats.model.ViewStats(app, uri, count(distinct ip) as hits) from EndpointHit " +
            "where timestamp between ?1 and ?2 " +
            "group by app, uri order by hits desc")
    List<ViewStats> getStatisticsWithUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query(" select new ru.practicum.stats.model.ViewStats(app, uri, count(ip) as hits) from EndpointHit " +
            "where timestamp between ?1 and ?2 " +
            "group by app, uri order by hits desc")
    List<ViewStats> getAllStatistics(LocalDateTime start, LocalDateTime end);

}