package com.api.BetStrat.repository;

import com.api.BetStrat.entity.ComebackSeasonInfo;
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
@RepositoryDefinition(domainClass = ComebackSeasonInfo.class, idClass = Long.class)
public interface ComebackSeasonInfoRepository extends JpaRepository<ComebackSeasonInfo, Long>, JpaSpecificationExecutor<ComebackSeasonInfo> {

    @Query(value = "SELECT m FROM ComebackSeasonInfo m WHERE m.teamId = ?1")
    List<ComebackSeasonInfo> getStatsByTeam(Team team);

}