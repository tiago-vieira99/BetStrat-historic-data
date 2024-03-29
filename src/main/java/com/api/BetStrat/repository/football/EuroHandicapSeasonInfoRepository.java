package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.football.EuroHandicapSeasonInfo;
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
@RepositoryDefinition(domainClass = EuroHandicapSeasonInfo.class, idClass = Long.class)
public interface EuroHandicapSeasonInfoRepository extends JpaRepository<EuroHandicapSeasonInfo, Long>, JpaSpecificationExecutor<EuroHandicapSeasonInfo> {

    @Cacheable(value="List<EuroHandicapSeasonInfo>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM EuroHandicapSeasonInfo m WHERE m.teamId = ?1")
    List<EuroHandicapSeasonInfo> getEuroHandicapStatsByTeam(Team team);

}