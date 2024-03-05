package com.api.BetStrat.repository.handball;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.handball.Handball16WinsMarginSeasonStats;
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
@RepositoryDefinition(domainClass = Handball16WinsMarginSeasonStats.class, idClass = Long.class)
public interface Handball16WinsMarginSeasonInfoRepository extends JpaRepository<Handball16WinsMarginSeasonStats, Long>, JpaSpecificationExecutor<Handball16WinsMarginSeasonStats> {

    @Cacheable(value="List<Handball16WinsMarginSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM Handball16WinsMarginSeasonStats m WHERE m.teamId = ?1")
    List<Handball16WinsMarginSeasonStats> getHandball16WinsMarginStatsByTeam(Team team);

}