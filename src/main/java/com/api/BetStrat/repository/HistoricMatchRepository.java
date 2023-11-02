package com.api.BetStrat.repository;

import com.api.BetStrat.entity.HistoricMatch;
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
@RepositoryDefinition(domainClass = HistoricMatch.class, idClass = Long.class)
public interface HistoricMatchRepository extends JpaRepository<HistoricMatch, Long>, JpaSpecificationExecutor<HistoricMatch> {

    @Cacheable(value="List<HistoricMatch>", key="{#root.methodName}")
    @Override
    List<HistoricMatch> findAll();

    @Cacheable(value="List<HistoricMatch>", key="{#root.methodName, #team.name, #season}")
    @Query(value = "SELECT t FROM HistoricMatch t WHERE t.teamId = ?1 AND t.season = ?2")
    List<HistoricMatch> getTeamMatchesBySeason(Team team, String season);

}