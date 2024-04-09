package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.NoScoreBothHalvesSeasonStats;
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
@RepositoryDefinition(domainClass = NoScoreBothHalvesSeasonStats.class, idClass = Long.class)
public interface NoScoreBothHalvesSeasonInfoRepository extends JpaRepository<NoScoreBothHalvesSeasonStats, Long>, JpaSpecificationExecutor<NoScoreBothHalvesSeasonStats> {

    @Cacheable(value="List<NoScoreBothHalvesSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM NoScoreBothHalvesSeasonStats m WHERE m.teamId = ?1")
    List<NoScoreBothHalvesSeasonStats> getFootballNoScoreBothHalvesStatsByTeam(Team team);

}