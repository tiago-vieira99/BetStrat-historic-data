package com.api.BetStrat.repository.football;

import com.api.BetStrat.entity.football.DrawSeasonInfo;
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
@RepositoryDefinition(domainClass = DrawSeasonInfo.class, idClass = Long.class)
public interface DrawSeasonInfoRepository extends JpaRepository<DrawSeasonInfo, Long>, JpaSpecificationExecutor<DrawSeasonInfo> {

    @Cacheable(value="List<DrawSeasonInfo>", key="{#root.methodName, #team}")
    @Query(value = "SELECT m FROM DrawSeasonInfo m WHERE m.teamId = ?1")
    List<DrawSeasonInfo> getStatsByTeam(Team team);

}