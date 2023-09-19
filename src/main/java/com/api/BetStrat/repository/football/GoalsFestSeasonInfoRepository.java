package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.football.GoalsFestSeasonInfo;
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
@RepositoryDefinition(domainClass = GoalsFestSeasonInfo.class, idClass = Long.class)
public interface GoalsFestSeasonInfoRepository extends JpaRepository<GoalsFestSeasonInfo, Long>, JpaSpecificationExecutor<GoalsFestSeasonInfo> {

    @Cacheable(value="List<GoalsFestSeasonInfo>", key="{#root.methodName, #team}")
    @Query(value = "SELECT m FROM GoalsFestSeasonInfo m WHERE m.teamId = ?1")
    List<GoalsFestSeasonInfo> getStatsByTeam(Team team);

}