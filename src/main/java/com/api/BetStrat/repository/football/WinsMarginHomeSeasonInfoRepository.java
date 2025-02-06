package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinsMarginHomeSeasonStats;
import com.api.BetStrat.entity.football.WinsMarginSeasonStats;
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
@RepositoryDefinition(domainClass = WinsMarginHomeSeasonStats.class, idClass = Long.class)
public interface WinsMarginHomeSeasonInfoRepository extends JpaRepository<WinsMarginHomeSeasonStats, Long>, JpaSpecificationExecutor<WinsMarginHomeSeasonStats> {

    @Cacheable(value="List<WinsMarginHomeSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM WinsMarginHomeSeasonStats m WHERE m.teamId = ?1")
    List<WinsMarginHomeSeasonStats> getFootballWinsMarginHomeStatsByTeam(Team team);

}