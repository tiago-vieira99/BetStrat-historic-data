package com.api.BetStrat.repository.basketball;

import com.api.BetStrat.entity.basketball.ShortBasketWinsSeasonInfo;
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
@RepositoryDefinition(domainClass = ShortWinsSeasonInfoRepository.class, idClass = Long.class)
public interface ShortWinsSeasonInfoRepository extends JpaRepository<ShortBasketWinsSeasonInfo, Long>, JpaSpecificationExecutor<ShortBasketWinsSeasonInfo> {

    @Cacheable(value="List<ShortBasketWinsSeasonInfo>", key="{#root.methodName, #team}")
    @Query(value = "SELECT m FROM ShortBasketWinsSeasonInfo m WHERE m.teamId = ?1")
    List<ShortBasketWinsSeasonInfo> getStatsByTeam(Team team);

}