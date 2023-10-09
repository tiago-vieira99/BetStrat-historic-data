package com.api.BetStrat.repository.handball;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinsMarginSeasonInfo;
import com.api.BetStrat.entity.handball.Handball16WinsMarginSeasonInfo;
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
@RepositoryDefinition(domainClass = Handball16WinsMarginSeasonInfo.class, idClass = Long.class)
public interface Handball16WinsMarginSeasonInfoRepository extends JpaRepository<Handball16WinsMarginSeasonInfo, Long>, JpaSpecificationExecutor<Handball16WinsMarginSeasonInfo> {

    @Cacheable(value="List<Handball16WinsMarginSeasonInfo>", key="{#root.methodName, #team}")
    @Query(value = "SELECT m FROM Handball16WinsMarginSeasonInfo m WHERE m.teamId = ?1")
    List<Handball16WinsMarginSeasonInfo> getStatsByTeam(Team team);

}