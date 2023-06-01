package com.api.BetStrat.repository;

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
@RepositoryDefinition(domainClass = Team.class, idClass = Long.class)
public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

    @Cacheable(value="List<Team>", key="{#root.methodName}")
    @Override
    List<Team> findAll();

    @Cacheable(value="Team", key="{#root.methodName, #name}")
    @Query(value = "SELECT t FROM Team t WHERE t.name = ?1 ")
    Team getTeamByName(String name);

}