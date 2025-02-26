package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.FirstHalfBiggerSeasonStats;
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
@RepositoryDefinition(domainClass = FirstHalfBiggerSeasonStats.class, idClass = Long.class)
public interface FirstHalfBiggerSeasonInfoRepository extends JpaRepository<FirstHalfBiggerSeasonStats, Long>, JpaSpecificationExecutor<FirstHalfBiggerSeasonStats> {

    @Cacheable(value="List<FirstHalfBiggerSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM FirstHalfBiggerSeasonStats m WHERE m.teamId = ?1")
    List<FirstHalfBiggerSeasonStats> getFootballFirstHalfBiggerStatsByTeam(Team team);

}