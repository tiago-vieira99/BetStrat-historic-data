package com.api.BetStrat.repository.handball;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.handball.Handball16WinsMarginSeasonInfo;
import com.api.BetStrat.entity.handball.Handball712WinsMarginSeasonInfo;
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
@RepositoryDefinition(domainClass = Handball712WinsMarginSeasonInfo.class, idClass = Long.class)
public interface Handball712WinsMarginSeasonInfoRepository extends JpaRepository<Handball712WinsMarginSeasonInfo, Long>, JpaSpecificationExecutor<Handball712WinsMarginSeasonInfo> {

    @Cacheable(value="List<Handball712WinsMarginSeasonInfo>", key="{#root.methodName, #team}")
    @Query(value = "SELECT m FROM Handball712WinsMarginSeasonInfo m WHERE m.teamId = ?1")
    List<Handball712WinsMarginSeasonInfo> getStatsByTeam(Team team);

}