package com.api.BetStrat.service;

import com.api.BetStrat.entity.basketball.ComebackSeasonInfo;
import com.api.BetStrat.entity.basketball.ShortBasketWinsSeasonInfo;
import com.api.BetStrat.entity.football.DrawSeasonInfo;
import com.api.BetStrat.entity.football.EuroHandicapSeasonInfo;
import com.api.BetStrat.entity.football.GoalsFestSeasonInfo;
import com.api.BetStrat.entity.handball.Handball16WinsMarginSeasonInfo;
import com.api.BetStrat.entity.handball.Handball49WinsMarginSeasonInfo;
import com.api.BetStrat.entity.handball.Handball712WinsMarginSeasonInfo;
import com.api.BetStrat.entity.hockey.HockeyDrawSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinsMarginSeasonInfo;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.basketball.ComebackSeasonInfoRepository;
import com.api.BetStrat.repository.basketball.ShortWinsSeasonInfoRepository;
import com.api.BetStrat.repository.football.DrawSeasonInfoRepository;
import com.api.BetStrat.repository.football.EuroHandicapSeasonInfoRepository;
import com.api.BetStrat.repository.football.GoalsFestSeasonInfoRepository;
import com.api.BetStrat.repository.handball.Handball16WinsMarginSeasonInfoRepository;
import com.api.BetStrat.repository.handball.Handball49WinsMarginSeasonInfoRepository;
import com.api.BetStrat.repository.handball.Handball712WinsMarginSeasonInfoRepository;
import com.api.BetStrat.repository.hockey.HockeyDrawSeasonInfoRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.repository.football.WinsMarginSeasonInfoRepository;
import com.api.BetStrat.service.basketball.ComebackSeasonInfoService;
import com.api.BetStrat.service.basketball.ShortBasketWinsSeasonInfoService;
import com.api.BetStrat.service.football.DrawSeasonInfoService;
import com.api.BetStrat.service.football.EuroHandicapSeasonInfoService;
import com.api.BetStrat.service.football.GoalsFestSeasonInfoService;
import com.api.BetStrat.service.football.WinsMarginSeasonInfoService;
import com.api.BetStrat.service.handball.HandballWinsMargin16SeasonInfoService;
import com.api.BetStrat.service.handball.HandballWinsMargin49SeasonInfoService;
import com.api.BetStrat.service.handball.HandballWinsMargin712SeasonInfoService;
import com.api.BetStrat.service.hockey.HockeyDrawSeasonInfoService;
import com.api.BetStrat.service.hockey.WinsMargin3SeasonInfoService;
import com.api.BetStrat.service.hockey.WinsMarginAny2SeasonInfoService;
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
    private ShortWinsSeasonInfoRepository shortWinsSeasonInfoRepository;

    @Autowired
    private ComebackSeasonInfoService comebackSeasonInfoService;

    @Autowired
    private ShortBasketWinsSeasonInfoService shortBasketWinsSeasonInfoService;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Autowired
    private HandballWinsMargin49SeasonInfoService handballWinsMargin49SeasonInfoService;

    @Autowired
    private HandballWinsMargin16SeasonInfoService handballWinsMargin16SeasonInfoService;

    @Autowired
    private HandballWinsMargin712SeasonInfoService handballWinsMargin712SeasonInfoService;

    @Autowired
    private Handball49WinsMarginSeasonInfoRepository handballWinsMargin49SeasonInfoRepository;

    @Autowired
    private Handball16WinsMarginSeasonInfoRepository handballWinsMargin16SeasonInfoRepository;

    @Autowired
    private Handball712WinsMarginSeasonInfoRepository handballWinsMargin712SeasonInfoRepository;


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

    public List<Handball49WinsMarginSeasonInfo> getTeamMargin49WinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Handball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<Handball49WinsMarginSeasonInfo> statsByTeam = handballWinsMargin49SeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<Handball16WinsMarginSeasonInfo> getTeamMargin16WinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Handball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<Handball16WinsMarginSeasonInfo> statsByTeam = handballWinsMargin16SeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<Handball712WinsMarginSeasonInfo> getTeamMargin712WinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Handball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<Handball712WinsMarginSeasonInfo> statsByTeam = handballWinsMargin712SeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<ComebackSeasonInfo> getTeamComebackWinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Basketball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<ComebackSeasonInfo> statsByTeam = comebackSeasonInfoRepository.getStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<ShortBasketWinsSeasonInfo> getTeamShortWinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Basketball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<ShortBasketWinsSeasonInfo> statsByTeam = shortWinsSeasonInfoRepository.getStatsByTeam(teamByName);
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

    public Team updateTeamScore (String teamName, String strategy, String sport) {
        LOGGER.info("Updating score for " + teamName + " and strategy " + strategy);
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, sport);
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
            case "basketShortWins":
                if (teamByName.getSport().equals("Basketball")) {
                    updatedTeam = shortBasketWinsSeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "handballmargin16wins":
                if (teamByName.getSport().equals("Handball")) {
                    updatedTeam = handballWinsMargin16SeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "handballmargin49wins":
                if (teamByName.getSport().equals("Handball")) {
                    updatedTeam = handballWinsMargin49SeasonInfoService.updateTeamScore(teamByName);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "handballmargin712wins":
                if (teamByName.getSport().equals("Handball")) {
                    updatedTeam = handballWinsMargin712SeasonInfoService.updateTeamScore(teamByName);
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


