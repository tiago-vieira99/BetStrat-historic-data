package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.ScoreBothHalvesSeasonStats;
import com.api.BetStrat.entity.football.WinsSeasonStats;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
@RepositoryDefinition(domainClass = ScoreBothHalvesSeasonStats.class, idClass = Long.class)
public interface ScoreBothHalvesSeasonInfoRepository extends JpaRepository<ScoreBothHalvesSeasonStats, Long>, JpaSpecificationExecutor<ScoreBothHalvesSeasonStats> {

    @Cacheable(value="List<ScoreBothHalvesSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM ScoreBothHalvesSeasonStats m WHERE m.teamId = ?1")
    List<ScoreBothHalvesSeasonStats> getFootballScoreBothHalvesStatsByTeam(Team team);

}