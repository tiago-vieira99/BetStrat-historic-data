package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.NoWinFirstHalfSeasonStats;
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
@RepositoryDefinition(domainClass = NoWinFirstHalfSeasonStats.class, idClass = Long.class)
public interface NoWinFirstHalfSeasonInfoRepository extends JpaRepository<NoWinFirstHalfSeasonStats, Long>, JpaSpecificationExecutor<NoWinFirstHalfSeasonStats> {

    @Cacheable(value="List<NoWinFirstHalfSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM NoWinFirstHalfSeasonStats m WHERE m.teamId = ?1")
    List<NoWinFirstHalfSeasonStats> getFootballNoWinFirstHalfStatsByTeam(Team team);

}