package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.DrawSeasonStats;
import com.api.BetStrat.entity.football.NoDrawSeasonStats;
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
@RepositoryDefinition(domainClass = NoDrawSeasonStats.class, idClass = Long.class)
public interface NoDrawSeasonInfoRepository extends JpaRepository<NoDrawSeasonStats, Long>, JpaSpecificationExecutor<NoDrawSeasonStats> {

    @Cacheable(value="List<NoDrawSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM NoDrawSeasonStats m WHERE m.teamId = ?1")
    List<NoDrawSeasonStats> getFootballNoDrawStatsByTeam(Team team);

}