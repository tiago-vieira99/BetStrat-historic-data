package com.api.BetStrat.repository.basketball;

import com.api.BetStrat.entity.basketball.ComebackSeasonStats;
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
@RepositoryDefinition(domainClass = ComebackSeasonStats.class, idClass = Long.class)
public interface ComebackSeasonInfoRepository extends JpaRepository<ComebackSeasonStats, Long>, JpaSpecificationExecutor<ComebackSeasonStats> {

    @Cacheable(value="List<ComebackSeasonInfo>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM ComebackSeasonStats m WHERE m.teamId = ?1")
    List<ComebackSeasonStats> getComebackStatsByTeam(Team team);

}