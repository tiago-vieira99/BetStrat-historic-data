package com.api.BetStrat.repository.hockey;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.hockey.WinsMarginAny2SeasonInfo;
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
@RepositoryDefinition(domainClass = WinsMarginAny2SeasonInfo.class, idClass = Long.class)
public interface WinsMarginAny2SeasonInfoRepository extends JpaRepository<WinsMarginAny2SeasonInfo, Long>, JpaSpecificationExecutor<WinsMarginAny2SeasonInfo> {

    @Cacheable(value="List<WinsMarginAny2SeasonInfo>", key="{#root.methodName, #team.name}")
    @Query(value = "SELECT m FROM WinsMarginAny2SeasonInfo m WHERE m.teamId = ?1")
    List<WinsMarginAny2SeasonInfo> getHockeyWinsMarginAny2StatsByTeam(Team team);

}