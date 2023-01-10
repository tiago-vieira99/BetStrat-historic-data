package com.api.BetStrat.service;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.ForbiddenException;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private TeamRepository teamRepository;

    public Team insertTeam(String teamName) {
        Team team = new Team();
        team.setName(teamName);

        return teamRepository.save(team);
    }


}
