package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.ConcedeBothHalvesSeasonStats;
import com.api.BetStrat.entity.football.ConcedeBothHalvesSeasonStats;
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
@RepositoryDefinition(domainClass = ConcedeBothHalvesSeasonStats.class, idClass = Long.class)
public interface ConcedeBothHalvesSeasonInfoRepository extends JpaRepository<ConcedeBothHalvesSeasonStats, Long>, JpaSpecificationExecutor<ConcedeBothHalvesSeasonStats> {

    @Cacheable(value="List<ConcedeBothHalvesSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM ConcedeBothHalvesSeasonStats m WHERE m.teamId = ?1")
    List<ConcedeBothHalvesSeasonStats> getFootballConcedeBothHalvesStatsByTeam(Team team);

}