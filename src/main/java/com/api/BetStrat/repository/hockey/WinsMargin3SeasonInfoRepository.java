package com.api.BetStrat.repository.hockey;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.hockey.WinsMargin3SeasonInfo;
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
@RepositoryDefinition(domainClass = WinsMargin3SeasonInfo.class, idClass = Long.class)
public interface WinsMargin3SeasonInfoRepository extends JpaRepository<WinsMargin3SeasonInfo, Long>, JpaSpecificationExecutor<WinsMargin3SeasonInfo> {

    @Cacheable(value="List<WinsMargin3SeasonInfo>", key="{#root.methodName, #team}")
    @Query(value = "SELECT m FROM WinsMargin3SeasonInfo m WHERE m.teamId = ?1")
    List<WinsMargin3SeasonInfo> getStatsByTeam(Team team);

}