package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinFirstHalfSeasonStats;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@RepositoryDefinition(domainClass = WinFirstHalfSeasonStats.class, idClass = Long.class)
public interface WinFirstHalfSeasonInfoRepository extends JpaRepository<WinFirstHalfSeasonStats, Long>, JpaSpecificationExecutor<WinFirstHalfSeasonStats> {

    @Cacheable(value="List<WinFirstHalfSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM WinFirstHalfSeasonStats m WHERE m.teamId = ?1")
    List<WinFirstHalfSeasonStats> getFootballWinFirstHalfStatsByTeam(Team team);

}