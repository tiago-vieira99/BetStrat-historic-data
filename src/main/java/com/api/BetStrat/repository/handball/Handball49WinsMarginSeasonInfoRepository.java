package com.api.BetStrat.repository.handball;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.handball.Handball49WinsMarginSeasonStats;
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
@RepositoryDefinition(domainClass = Handball49WinsMarginSeasonStats.class, idClass = Long.class)
public interface Handball49WinsMarginSeasonInfoRepository extends JpaRepository<Handball49WinsMarginSeasonStats, Long>, JpaSpecificationExecutor<Handball49WinsMarginSeasonStats> {

    @Cacheable(value="List<Handball49WinsMarginSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM Handball49WinsMarginSeasonStats m WHERE m.teamId = ?1")
    List<Handball49WinsMarginSeasonStats> getHandball49WinsMarginStatsByTeam(Team team);

}