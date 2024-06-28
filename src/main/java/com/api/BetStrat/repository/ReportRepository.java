package com.api.BetStrat.repository;

import com.api.BetStrat.entity.Report;
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
@RepositoryDefinition(domainClass = Report.class, idClass = Long.class)
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    @Cacheable(value="List<Report>", key="{#root.methodName}")
    @Override
    List<Report> findAll();

    @Cacheable(value="List<Report>", key="{#root.methodName, #season, #strategy}")
    @Query(value = "SELECT t FROM Report t WHERE t.season = ?1 AND t.strategy = ?2")
    List<Report> getReportsBySeasonAndStrategy(String season, String strategy);

}