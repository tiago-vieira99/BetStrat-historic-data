package com.api.BetStrat.service;

import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.HockeyDrawSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.WinsMarginSeasonInfo;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.repository.DrawSeasonInfoRepository;
import com.api.BetStrat.repository.HockeyDrawSeasonInfoRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.repository.WinsMarginSeasonInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class TeamService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private DrawSeasonInfoService drawSeasonInfoService;

    @Autowired
    private HockeyDrawSeasonInfoService hockeyDrawSeasonInfoService;

    @Autowired
    private WinsMarginSeasonInfoService winsMarginSeasonInfoService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DrawSeasonInfoRepository drawSeasonInfoRepository;

    @Autowired
    private HockeyDrawSeasonInfoRepository hockeyDrawSeasonInfoRepository;

    @Autowired
    private WinsMarginSeasonInfoRepository winsMarginSeasonInfoRepository;

    public Team insertTeam(Team team) {
        return teamRepository.save(team);
    }

    public List<HockeyDrawSeasonInfo> getHockeyTeamDrawStats(String teamName) {
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<HockeyDrawSeasonInfo> statsByTeam = hockeyDrawSeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<DrawSeasonInfo> getTeamDrawStats(String teamName) {
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<DrawSeasonInfo> statsByTeam = drawSeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<WinsMarginSeasonInfo> getTeamMarginWinStats(String teamName) {
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<WinsMarginSeasonInfo> statsByTeam = winsMarginSeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public Team updateTeamScore (String teamName) {
        LOGGER.info("Updating score for " + teamName);
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (null == teamByName) {
            throw new NotFoundException();
        }

        Team updatedTeam = drawSeasonInfoService.updateTeamScore(teamByName);
        hockeyDrawSeasonInfoService.updateTeamScore(teamByName);
        teamRepository.save(winsMarginSeasonInfoService.updateTeamScore(updatedTeam));

        return updatedTeam;
    }

}


