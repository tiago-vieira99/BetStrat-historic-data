package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinAndGoalsSeasonStats;
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
@RepositoryDefinition(domainClass = WinAndGoalsSeasonStats.class, idClass = Long.class)
public interface WinAndGoalsSeasonInfoRepository extends JpaRepository<WinAndGoalsSeasonStats, Long>, JpaSpecificationExecutor<WinAndGoalsSeasonStats> {

    @Cacheable(value="List<WinAndGoalsSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM WinAndGoalsSeasonStats m WHERE m.teamId = ?1")
    List<WinAndGoalsSeasonStats> getFootballWinAndGoalsStatsByTeam(Team team);

}