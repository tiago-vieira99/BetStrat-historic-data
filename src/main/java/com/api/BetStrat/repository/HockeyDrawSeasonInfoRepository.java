package com.api.BetStrat.repository;

import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.HockeyDrawSeasonInfo;
import com.api.BetStrat.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
@RepositoryDefinition(domainClass = HockeyDrawSeasonInfo.class, idClass = Long.class)
public interface HockeyDrawSeasonInfoRepository extends JpaRepository<HockeyDrawSeasonInfo, Long>, JpaSpecificationExecutor<HockeyDrawSeasonInfo> {

    @Query(value = "SELECT m FROM HockeyDrawSeasonInfo m WHERE m.teamId = ?1")
    List<HockeyDrawSeasonInfo> getStatsByTeam(Team team);

}