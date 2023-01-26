package com.api.BetStrat.repository;

import com.api.BetStrat.entity.DrawSeasonInfo;
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
@RepositoryDefinition(domainClass = WinsMarginSeasonInfo.class, idClass = Long.class)
public interface WinsMarginSeasonInfoRepository extends JpaRepository<WinsMarginSeasonInfo, Long>, JpaSpecificationExecutor<WinsMarginSeasonInfo> {

    @Query(value = "SELECT m FROM WinsMarginSeasonInfo m WHERE m.teamId = ?1")
    List<WinsMarginSeasonInfo> getStatsByTeam(Team team);

}