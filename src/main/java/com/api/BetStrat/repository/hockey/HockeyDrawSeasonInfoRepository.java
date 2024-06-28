package com.api.BetStrat.repository.hockey;

import com.api.BetStrat.entity.hockey.HockeyDrawSeasonStats;
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
@RepositoryDefinition(domainClass = HockeyDrawSeasonStats.class, idClass = Long.class)
public interface HockeyDrawSeasonInfoRepository extends JpaRepository<HockeyDrawSeasonStats, Long>, JpaSpecificationExecutor<HockeyDrawSeasonStats> {

    @Cacheable(value="List<HockeyDrawSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM HockeyDrawSeasonStats m WHERE m.teamId = ?1")
    List<HockeyDrawSeasonStats> getHockeyDrawStatsByTeam(Team team);

}