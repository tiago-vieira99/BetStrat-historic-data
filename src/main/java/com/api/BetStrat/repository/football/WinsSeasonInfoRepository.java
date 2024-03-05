package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinsMarginSeasonStats;
import com.api.BetStrat.entity.football.WinsSeasonStats;
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
@RepositoryDefinition(domainClass = WinsSeasonStats.class, idClass = Long.class)
public interface WinsSeasonInfoRepository extends JpaRepository<WinsSeasonStats, Long>, JpaSpecificationExecutor<WinsSeasonStats> {

    @Cacheable(value="List<WinsSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM WinsSeasonStats m WHERE m.teamId = ?1")
    List<WinsSeasonStats> getFootballWinsStatsByTeam(Team team);

}