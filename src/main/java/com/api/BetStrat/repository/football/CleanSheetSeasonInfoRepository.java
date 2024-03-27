package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.CleanSheetSeasonStats;
import com.api.BetStrat.entity.football.DrawSeasonStats;
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
@RepositoryDefinition(domainClass = CleanSheetSeasonStats.class, idClass = Long.class)
public interface CleanSheetSeasonInfoRepository extends JpaRepository<CleanSheetSeasonStats, Long>, JpaSpecificationExecutor<CleanSheetSeasonStats> {

    @Cacheable(value="List<CleanSheetSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM CleanSheetSeasonStats m WHERE m.teamId = ?1")
    List<CleanSheetSeasonStats> getFootballCleanSheetStatsByTeam(Team team);

}