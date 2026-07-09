package com.api.BetStrat.repository;

import com.api.BetStrat.entity.AIInsight;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Report;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@RepositoryDefinition(domainClass = AIInsight.class, idClass = Long.class)
public interface AIInsightRepository extends JpaRepository<AIInsight, Long>, JpaSpecificationExecutor<AIInsight> {

    @Query(value = "SELECT a FROM AIInsight a WHERE a.match = ?1")
    AIInsight getInsightByMatchId(HistoricMatch match);

}