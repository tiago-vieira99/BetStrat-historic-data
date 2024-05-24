package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.SecondHalfBiggerSeasonStats;
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
@RepositoryDefinition(domainClass = SecondHalfBiggerSeasonStats.class, idClass = Long.class)
public interface SecondHalfBiggerSeasonInfoRepository extends JpaRepository<SecondHalfBiggerSeasonStats, Long>, JpaSpecificationExecutor<SecondHalfBiggerSeasonStats> {

    @Cacheable(value="List<SecondHalfBiggerSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM SecondHalfBiggerSeasonStats m WHERE m.teamId = ?1")
    List<SecondHalfBiggerSeasonStats> getFootballSecondHalfBiggerStatsByTeam(Team team);

}