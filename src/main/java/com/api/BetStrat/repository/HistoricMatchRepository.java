package com.api.BetStrat.repository;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
@RepositoryDefinition(domainClass = HistoricMatch.class, idClass = Long.class)
public interface HistoricMatchRepository extends JpaRepository<HistoricMatch, Long>, JpaSpecificationExecutor<HistoricMatch> {

    //@Cacheable(value="List<HistoricMatch>", key="{#root.methodName}")
    @Override
    List<HistoricMatch> findAll();

    //@Cacheable(value="List<HistoricMatch>", key="{#root.methodName, #team.name, #season}")
    @Query(value = "SELECT * FROM historic_matches t WHERE t.team_id = ?1 AND t.season = ?2 AND t.ft_result != 'null' ORDER BY TO_DATE(t.match_date, 'DD/MM/YYYY') DESC", nativeQuery = true)
    List<HistoricMatch> getTeamMatchesBySeason(Team team, String season);

    @Query(value = "SELECT * FROM historic_matches t WHERE t.ft_result != 'null' AND t.team_id = ?1 AND t.season = ?2 ORDER BY TO_DATE(t.match_date, 'DD/MM/YYYY') DESC LIMIT 10", nativeQuery = true)
    List<HistoricMatch> getLast10Matches(Team team, String season);

    @Query(value = "SELECT * FROM historic_matches t WHERE t.competition = ?1 AND t.season = ?2 AND TO_DATE(match_date, 'DD/MM/YYYY') < TO_DATE(?3, 'DD/MM/YYYY') ORDER BY TO_DATE(t.match_date, 'DD/MM/YYYY')", nativeQuery = true)
    List<HistoricMatch> getMatchesByCompetitionandDate(String competition, String season, String date);

    @Query(value = "SELECT t FROM HistoricMatch t WHERE t.url = ?1 AND t.teamId = ?2")
    HistoricMatch getMatchByUrl(String url, Team team);

    @Modifying
    @Query(value = "UPDATE HistoricMatch t SET t.htResult = ?6, t.ftResult = ?7 WHERE t.teamId = ?1 AND t.season = ?2 AND t.homeTeam = ?3 AND t.awayTeam = ?4 AND t.matchDate = ?5")
    int updateSpecificMatch(Team team, String season, String homeTeam, String awayTeam, String matchDate, String htResult, String ftResult);

    @Query(value = "SELECT t FROM HistoricMatch t WHERE t.ftResult = 'null'")
    List<HistoricMatch> getUpcomingMatches();

    @Query(value = "SELECT t FROM HistoricMatch t WHERE t.matchDate = ?1")
    List<HistoricMatch> getMatchesByDate(String date);

}