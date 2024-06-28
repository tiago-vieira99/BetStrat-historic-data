package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.BttsSeasonStats;
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
@RepositoryDefinition(domainClass = BttsSeasonStats.class, idClass = Long.class)
public interface BttsSeasonInfoRepository extends JpaRepository<BttsSeasonStats, Long>, JpaSpecificationExecutor<BttsSeasonStats> {

    @Cacheable(value="List<BttsSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM BttsSeasonStats m WHERE m.teamId = ?1")
    List<BttsSeasonStats> getFootballBttsStatsByTeam(Team team);

}