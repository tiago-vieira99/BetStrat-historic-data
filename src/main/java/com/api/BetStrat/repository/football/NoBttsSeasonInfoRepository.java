package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.NoBttsSeasonStats;
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
@RepositoryDefinition(domainClass = NoBttsSeasonStats.class, idClass = Long.class)
public interface NoBttsSeasonInfoRepository extends JpaRepository<NoBttsSeasonStats, Long>, JpaSpecificationExecutor<NoBttsSeasonStats> {

    @Cacheable(value="List<NoBttsSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM NoBttsSeasonStats m WHERE m.teamId = ?1")
    List<NoBttsSeasonStats> getFootballNoBttsStatsByTeam(Team team);

}