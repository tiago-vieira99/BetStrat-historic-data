package com.api.BetStrat.repository;

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
@RepositoryDefinition(domainClass = Team.class, idClass = Long.class)
public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

    @Query(value = "SELECT t FROM Team t WHERE t.name = ?1 ")
    Team getTeamByName(String name);

}