package com.api.BetStrat.repository.basketball;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.basketball.LongBasketWinsSeasonStats;
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
@RepositoryDefinition(domainClass = LongWinsSeasonInfoRepository.class, idClass = Long.class)
public interface LongWinsSeasonInfoRepository extends JpaRepository<LongBasketWinsSeasonStats, Long>, JpaSpecificationExecutor<LongBasketWinsSeasonStats> {

    @Cacheable(value="List<LongBasketWinsSeasonInfo>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM LongBasketWinsSeasonStats m WHERE m.teamId = ?1")
    List<LongBasketWinsSeasonStats> getLongBasketWinsStatsByTeam(Team team);

}