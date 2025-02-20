package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.BttsOneHalfSeasonStats;
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
@RepositoryDefinition(domainClass = BttsOneHalfSeasonStats.class, idClass = Long.class)
public interface BttsOneHalfSeasonInfoRepository extends JpaRepository<BttsOneHalfSeasonStats, Long>, JpaSpecificationExecutor<BttsOneHalfSeasonStats> {

    @Cacheable(value="List<BttsOneHalfSeasonStats>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM BttsOneHalfSeasonStats m WHERE m.teamId = ?1")
    List<BttsOneHalfSeasonStats> getFootballBttsOneHalfStatsByTeam(Team team);

}