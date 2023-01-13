package com.api.BetStrat.service;

import com.api.BetStrat.constants.TeamAvailabilityScoreEnum;
import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.ForbiddenException;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.exception.ResourceExceptionHandler;
import com.api.BetStrat.repository.DrawSeasonInfoRepository;
import com.api.BetStrat.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DrawSeasonInfoRepository drawSeasonInfoRepository;

    public Team insertTeam(Team team) {
        return teamRepository.save(team);
    }

    public Team updateTeamScore (String teamName) {
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<DrawSeasonInfo> statsByTeam = drawSeasonInfoRepository.getStatsByTeam(teamByName);

        if (statsByTeam.size() < 3) {
            teamByName.setDrawsHunterScore(TeamAvailabilityScoreEnum.INSUFFICIENT_DATA.getValue());
            teamRepository.save(teamByName);
            return teamByName;
        } else {






        }

        return teamByName;
    }

/* ou avaliar cada parametro independentemente
*
* excellent: avg std dev < 2.1 && avg drawRate > 30 && list.size > 3 && maxSeqValue < 9
* acceptable: ((avg std dev > 2.1 & < 2.5 ; min drawRate > 23) || avg drawRate > 32) && maxSeqValue <= 10
* risky: (max std dev > 3 && min drawRate < 20) || maxSeqValue > 15
*
* */



}
