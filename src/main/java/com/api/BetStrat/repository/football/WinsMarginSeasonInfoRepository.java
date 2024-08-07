package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinsMarginSeasonStats;
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
@RepositoryDefinition(domainClass = WinsMarginSeasonStats.class, idClass = Long.class)
public interface WinsMarginSeasonInfoRepository extends JpaRepository<WinsMarginSeasonStats, Long>, JpaSpecificationExecutor<WinsMarginSeasonStats> {

    @Cacheable(value="List<WinsMarginSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM WinsMarginSeasonStats m WHERE m.teamId = ?1")
    List<WinsMarginSeasonStats> getFootballWinsMarginStatsByTeam(Team team);

}