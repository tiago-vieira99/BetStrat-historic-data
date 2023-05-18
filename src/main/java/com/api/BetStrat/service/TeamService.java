package com.api.BetStrat.service;

import com.api.BetStrat.entity.ComebackSeasonInfo;
import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.EuroHandicapSeasonInfo;
import com.api.BetStrat.entity.GoalsFestSeasonInfo;
import com.api.BetStrat.entity.HockeyDrawSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.WinsMarginSeasonInfo;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.repository.ComebackSeasonInfoRepository;
import com.api.BetStrat.repository.DrawSeasonInfoRepository;
import com.api.BetStrat.repository.EuroHandicapSeasonInfoRepository;
import com.api.BetStrat.repository.GoalsFestSeasonInfoRepository;
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
    private WinsMarginAny2SeasonInfoService winsMarginAny2SeasonInfoService;

    @Autowired
    private WinsMargin3SeasonInfoService winsMargin3SeasonInfoService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DrawSeasonInfoRepository drawSeasonInfoRepository;

    @Autowired
    private HockeyDrawSeasonInfoRepository hockeyDrawSeasonInfoRepository;

    @Autowired
    private WinsMarginSeasonInfoRepository winsMarginSeasonInfoRepository;

    @Autowired
    private EuroHandicapSeasonInfoService euroHandicapSeasonInfoService;

    @Autowired
    private EuroHandicapSeasonInfoRepository euroHandicapSeasonInfoRepository;

    @Autowired
    private GoalsFestSeasonInfoRepository goalsFestSeasonInfoRepository;

    @Autowired
    private GoalsFestSeasonInfoService goalsFestSeasonInfoService;

    @Autowired
    private ComebackSeasonInfoRepository comebackSeasonInfoRepository;

    @Autowired
    private ComebackSeasonInfoService comebackSeasonInfoService;



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

    public List<EuroHandicapSeasonInfo> getTeamEuroHandicapStats(String teamName) {
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<EuroHandicapSeasonInfo> statsByTeam = euroHandicapSeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<GoalsFestSeasonInfo> getTeamGoalsFestStats(String teamName) {
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<GoalsFestSeasonInfo> statsByTeam = goalsFestSeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<ComebackSeasonInfo> getTeamComebackStats(String teamName) {
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<ComebackSeasonInfo> statsByTeam = comebackSeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public Team updateTeamScore (String teamName, String strategy) {
        LOGGER.info("Updating score for " + teamName + " and strategy " + strategy);
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (null == teamByName) {
            throw new NotFoundException();
        }

        Team updatedTeam = null;

        switch (strategy) {
            case "hockeyDraw":
                if (teamByName.getSport() == "Hockey") {
                    updatedTeam = hockeyDrawSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "hockeyWinsMarginAny2":
                if (teamByName.getSport() == "Hockey") {
                    updatedTeam = winsMarginAny2SeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "hockeyWinsMargin3":
                if (teamByName.getSport() == "Hockey") {
                    updatedTeam = winsMargin3SeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballDrawHunter":
                if (teamByName.getSport() == "Football") {
                    updatedTeam = drawSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballMarginWins":
                if (teamByName.getSport() == "Football") {
                    updatedTeam = winsMarginSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballGoalsFest":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = goalsFestSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballEuroHandicap":
                if (teamByName.getSport() == "Football") {
                    updatedTeam = euroHandicapSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "basketComebacks":
                if (teamByName.getSport() == "Basketball") {
                    updatedTeam = comebackSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            default:
                break;
        }

        return updatedTeam;
    }

}


