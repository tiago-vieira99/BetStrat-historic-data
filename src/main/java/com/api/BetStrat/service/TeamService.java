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

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public Team updateTeamStats (Team team, String strategy) {
        LOGGER.info("Updating stats for " + team.getName() + " and strategy " + strategy);

        switch (strategy) {
            case "footballDrawHunter":
                drawSeasonInfoService.updateStatsDataInfo(team);
                break;
            case "footballMarginWins":
                winsMarginSeasonInfoService.updateStatsDataInfo(team);
                break;
            case "footballGoalsFest":
                goalsFestSeasonInfoService.updateStatsDataInfo(team);
                break;
            case "footballEuroHandicap":
                euroHandicapSeasonInfoService.updateStatsDataInfo(team);
                break;
            default:
                break;
        }

        return team;
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
                if (teamByName.getSport().equals("Hockey")) {
                    updatedTeam = hockeyDrawSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "hockeyWinsMarginAny2":
                if (teamByName.getSport().equals("Hockey")) {
                    updatedTeam = winsMarginAny2SeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "hockeyWinsMargin3":
                if (teamByName.getSport().equals("Hockey")) {
                    updatedTeam = winsMargin3SeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballDrawHunter":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = drawSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballMarginWins":
                if (teamByName.getSport().equals("Football")) {
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
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = euroHandicapSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "basketComebacks":
                if (teamByName.getSport().equals("Basketball")) {
                    updatedTeam = comebackSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            default:
                break;
        }

        return updatedTeam;
    }

    public HashMap<String, String> getSimulatedTeamScoreByFilteredSeason (Team team, String strategy, int seasonsToDiscard) {

        HashMap<String, String> outMap = new LinkedHashMap<>();
        Team simulatedTeam = null;

        switch (strategy) {
            case "hockeyDraw":
                if (team.getSport().equals("Hockey")) {
                    simulatedTeam = hockeyDrawSeasonInfoService.updateTeamScore(team);
                }
                break;
            case "hockeyWinsMarginAny2":
                if (team.getSport().equals("Hockey")) {
                    simulatedTeam = winsMarginAny2SeasonInfoService.updateTeamScore(team);
                }
                break;
            case "hockeyWinsMargin3":
                if (team.getSport().equals("Hockey")) {
                    simulatedTeam = winsMargin3SeasonInfoService.updateTeamScore(team);
                }
                break;
            case "footballDrawHunter":
                if (team.getSport().equals("Football")) {
                    LinkedHashMap<String, String> simulatedScore = drawSeasonInfoService.getSimulatedScorePartialSeasons(team, seasonsToDiscard);
                    outMap.put("beginSeason", team.getBeginSeason());
                    outMap.put("endSeason", team.getEndSeason());
                    outMap.putAll(simulatedScore);
                }
                break;
            case "footballMarginWins":
                if (team.getSport().equals("Football")) {
                    LinkedHashMap<String, String> simulatedScore = winsMarginSeasonInfoService.getSimulatedScorePartialSeasons(team, seasonsToDiscard);
                    outMap.put("beginSeason", team.getBeginSeason());
                    outMap.put("endSeason", team.getEndSeason());
                    outMap.putAll(simulatedScore);
                }
                break;
            case "footballGoalsFest":
                if (team.getSport().equals("Football")) {
                    LinkedHashMap<String, String> simulatedScore = goalsFestSeasonInfoService.getSimulatedScorePartialSeasons(team, seasonsToDiscard);
                    outMap.put("beginSeason", team.getBeginSeason());
                    outMap.put("endSeason", team.getEndSeason());
                    outMap.putAll(simulatedScore);
                }
                break;
            case "footballEuroHandicap":
                if (team.getSport().equals("Football")) {
                    simulatedTeam = euroHandicapSeasonInfoService.updateTeamScore(team);
                }
                break;
            case "basketComebacks":
                if (team.getSport().equals("Basketball")) {
                    simulatedTeam = comebackSeasonInfoService.updateTeamScore(team);
                }
                break;
            default:
                break;
        }

        return outMap;
    }

}


