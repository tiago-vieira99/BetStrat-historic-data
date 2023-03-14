package com.api.BetStrat.repository;

import com.api.BetStrat.entity.EuroHandicapSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.WinsMarginSeasonInfo;
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

    @Query(value = "SELECT m FROM EuroHandicapSeasonInfo m WHERE m.teamId = ?1")
    List<EuroHandicapSeasonInfo> getStatsByTeam(Team team);

}