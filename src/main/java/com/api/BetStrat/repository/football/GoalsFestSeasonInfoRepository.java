package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.football.GoalsFestSeasonStats;
import com.api.BetStrat.entity.Team;
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
@RepositoryDefinition(domainClass = GoalsFestSeasonStats.class, idClass = Long.class)
public interface GoalsFestSeasonInfoRepository extends JpaRepository<GoalsFestSeasonStats, Long>, JpaSpecificationExecutor<GoalsFestSeasonStats> {

    @Cacheable(value="List<GoalsFestSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM GoalsFestSeasonStats m WHERE m.teamId = ?1")
    List<GoalsFestSeasonStats> getGoalsFestStatsByTeam(Team team);

}