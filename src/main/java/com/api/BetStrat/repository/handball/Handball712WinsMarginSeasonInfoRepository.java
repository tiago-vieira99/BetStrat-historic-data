package com.api.BetStrat.repository.handball;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.handball.Handball712WinsMarginSeasonStats;
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
@RepositoryDefinition(domainClass = Handball712WinsMarginSeasonStats.class, idClass = Long.class)
public interface Handball712WinsMarginSeasonInfoRepository extends JpaRepository<Handball712WinsMarginSeasonStats, Long>, JpaSpecificationExecutor<Handball712WinsMarginSeasonStats> {

    @Cacheable(value="List<Handball712WinsMarginSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM Handball712WinsMarginSeasonStats m WHERE m.teamId = ?1")
    List<Handball712WinsMarginSeasonStats> getHandball712WinsMarginStatsByTeam(Team team);

}