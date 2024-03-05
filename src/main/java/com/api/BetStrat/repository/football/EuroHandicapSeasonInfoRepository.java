package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.football.EuroHandicapSeasonStats;
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
@RepositoryDefinition(domainClass = EuroHandicapSeasonStats.class, idClass = Long.class)
public interface EuroHandicapSeasonInfoRepository extends JpaRepository<EuroHandicapSeasonStats, Long>, JpaSpecificationExecutor<EuroHandicapSeasonStats> {

    @Cacheable(value="List<EuroHandicapSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM EuroHandicapSeasonStats m WHERE m.teamId = ?1")
    List<EuroHandicapSeasonStats> getEuroHandicapStatsByTeam(Team team);

}