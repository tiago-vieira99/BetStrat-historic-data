package com.api.BetStrat.service;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.basketball.ComebackSeasonInfo;
import com.api.BetStrat.entity.basketball.LongBasketWinsSeasonInfo;
import com.api.BetStrat.entity.basketball.ShortBasketWinsSeasonInfo;
import com.api.BetStrat.entity.football.DrawSeasonInfo;
import com.api.BetStrat.entity.football.EuroHandicapSeasonInfo;
import com.api.BetStrat.entity.football.FlipFlopOversUndersInfo;
import com.api.BetStrat.entity.football.GoalsFestSeasonInfo;
import com.api.BetStrat.entity.football.WinsMarginSeasonInfo;
import com.api.BetStrat.entity.handball.Handball16WinsMarginSeasonInfo;
import com.api.BetStrat.entity.handball.Handball49WinsMarginSeasonInfo;
import com.api.BetStrat.entity.handball.Handball712WinsMarginSeasonInfo;
import com.api.BetStrat.entity.hockey.HockeyDrawSeasonInfo;
import com.api.BetStrat.entity.hockey.WinsMargin3SeasonInfo;
import com.api.BetStrat.entity.hockey.WinsMarginAny2SeasonInfo;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.repository.basketball.ComebackSeasonInfoRepository;
import com.api.BetStrat.repository.basketball.LongWinsSeasonInfoRepository;
import com.api.BetStrat.repository.basketball.ShortWinsSeasonInfoRepository;
import com.api.BetStrat.repository.football.DrawSeasonInfoRepository;
import com.api.BetStrat.repository.football.EuroHandicapSeasonInfoRepository;
import com.api.BetStrat.repository.football.FlipFlopOversUndersInfoRepository;
import com.api.BetStrat.repository.football.GoalsFestSeasonInfoRepository;
import com.api.BetStrat.repository.football.WinsMarginSeasonInfoRepository;
import com.api.BetStrat.repository.handball.Handball16WinsMarginSeasonInfoRepository;
import com.api.BetStrat.repository.handball.Handball49WinsMarginSeasonInfoRepository;
import com.api.BetStrat.repository.handball.Handball712WinsMarginSeasonInfoRepository;
import com.api.BetStrat.repository.hockey.HockeyDrawSeasonInfoRepository;
import com.api.BetStrat.service.basketball.ComebackSeasonInfoService;
import com.api.BetStrat.service.basketball.LongBasketWinsSeasonInfoService;
import com.api.BetStrat.service.basketball.ShortBasketWinsSeasonInfoService;
import com.api.BetStrat.service.football.DrawSeasonInfoService;
import com.api.BetStrat.service.football.EuroHandicapSeasonInfoService;
import com.api.BetStrat.service.football.FlipFlopOversUndersInfoService;
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
    private StatsBySeasonService statsBySeasonService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DrawSeasonInfoRepository drawSeasonInfoRepository;

    @Autowired
    private HockeyDrawSeasonInfoRepository hockeyDrawSeasonInfoRepository;

    @Autowired
    private WinsMarginSeasonInfoRepository winsMarginSeasonInfoRepository;

    @Autowired
    private EuroHandicapSeasonInfoRepository euroHandicapSeasonInfoRepository;

    @Autowired
    private FlipFlopOversUndersInfoRepository flipFlopOversUndersInfoRepository;

    @Autowired
    private GoalsFestSeasonInfoRepository goalsFestSeasonInfoRepository;

    @Autowired
    private ComebackSeasonInfoRepository comebackSeasonInfoRepository;

    @Autowired
    private ShortWinsSeasonInfoRepository shortWinsSeasonInfoRepository;

    @Autowired
    private LongWinsSeasonInfoRepository longWinsSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

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
                statsBySeasonService.updateStatsBySeasonInfo(team, DrawSeasonInfo.class);
                break;
            case "footballMarginWins":
                statsBySeasonService.updateStatsBySeasonInfo(team, WinsMarginSeasonInfo.class);
                break;
            case "footballGoalsFest":
                statsBySeasonService.updateStatsBySeasonInfo(team, GoalsFestSeasonInfo.class);
                break;
            case "footballEuroHandicap":
                statsBySeasonService.updateStatsBySeasonInfo(team, EuroHandicapSeasonInfo.class);
                break;
            case "footballFlipFlop":
                statsBySeasonService.updateStatsBySeasonInfo(team, FlipFlopOversUndersInfo.class);
                break;
            default:
                break;
        }

        return team;
    }

    public List<HockeyDrawSeasonInfo> getHockeyTeamDrawStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Hockey");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<HockeyDrawSeasonInfo> statsByTeam = hockeyDrawSeasonInfoRepository.getHockeyDrawStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<DrawSeasonInfo> getTeamDrawStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<DrawSeasonInfo> statsByTeam = drawSeasonInfoRepository.getFootballDrawStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<Handball49WinsMarginSeasonInfo> getTeamMargin49WinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Handball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<Handball49WinsMarginSeasonInfo> statsByTeam = handballWinsMargin49SeasonInfoRepository.getHandball49WinsMarginStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<Handball16WinsMarginSeasonInfo> getTeamMargin16WinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Handball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<Handball16WinsMarginSeasonInfo> statsByTeam = handballWinsMargin16SeasonInfoRepository.getHandball16WinsMarginStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<Handball712WinsMarginSeasonInfo> getTeamMargin712WinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Handball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<Handball712WinsMarginSeasonInfo> statsByTeam = handballWinsMargin712SeasonInfoRepository.getHandball712WinsMarginStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<ComebackSeasonInfo> getTeamComebackWinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Basketball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<ComebackSeasonInfo> statsByTeam = comebackSeasonInfoRepository.getComebackStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<ShortBasketWinsSeasonInfo> getTeamShortWinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Basketball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<ShortBasketWinsSeasonInfo> statsByTeam = shortWinsSeasonInfoRepository.getShortBasketWinsStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<LongBasketWinsSeasonInfo> getTeamLongWinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Basketball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<LongBasketWinsSeasonInfo> statsByTeam = longWinsSeasonInfoRepository.getLongBasketWinsStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<WinsMarginSeasonInfo> getTeamMarginWinStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<WinsMarginSeasonInfo> statsByTeam = winsMarginSeasonInfoRepository.getFootballWinsMarginStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<EuroHandicapSeasonInfo> getTeamEuroHandicapStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<EuroHandicapSeasonInfo> statsByTeam = euroHandicapSeasonInfoRepository.getEuroHandicapStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<GoalsFestSeasonInfo> getTeamGoalsFestStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<GoalsFestSeasonInfo> statsByTeam = goalsFestSeasonInfoRepository.getGoalsFestStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<FlipFlopOversUndersInfo> getTeamFlipFlopStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<FlipFlopOversUndersInfo> statsByTeam = flipFlopOversUndersInfoRepository.getFlipFlopStatsByTeam(teamByName);
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
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, HockeyDrawSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "hockeyWinsMarginAny2":
                if (teamByName.getSport().equals("Hockey")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, WinsMarginAny2SeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "hockeyWinsMargin3":
                if (teamByName.getSport().equals("Hockey")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, WinsMargin3SeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballDrawHunter":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, DrawSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballMarginWins":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, WinsMarginSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballGoalsFest":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, GoalsFestSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballEuroHandicap":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, EuroHandicapSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballFlipFlop":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, FlipFlopOversUndersInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "basketComebacks":
                if (teamByName.getSport().equals("Basketball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, ComebackSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "basketShortWins":
                if (teamByName.getSport().equals("Basketball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, ShortBasketWinsSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "basketLongWins":
                if (teamByName.getSport().equals("Basketball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, LongBasketWinsSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "handballmargin16wins":
                if (teamByName.getSport().equals("Handball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, Handball16WinsMarginSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "handballmargin49wins":
                if (teamByName.getSport().equals("Handball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, Handball49WinsMarginSeasonInfo.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "handballmargin712wins":
                if (teamByName.getSport().equals("Handball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, Handball712WinsMarginSeasonInfo.class);
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
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, HockeyDrawSeasonInfo.class);
                }
                break;
            case "hockeyWinsMarginAny2":
                if (team.getSport().equals("Hockey")) {
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, WinsMarginAny2SeasonInfo.class);
                }
                break;
            case "hockeyWinsMargin3":
                if (team.getSport().equals("Hockey")) {
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, WinsMargin3SeasonInfo.class);
                }
                break;
//            case "footballDrawHunter":
//                if (team.getSport().equals("Football")) {
//                    LinkedHashMap<String, String> simulatedScore = statsBySeasonService.getSimulatedScorePartialSeasons(team, seasonsToDiscard);
//                    outMap.put("beginSeason", team.getBeginSeason());
//                    outMap.put("endSeason", team.getEndSeason());
//                    outMap.putAll(simulatedScore);
//                }
//                break;
//            case "footballMarginWins":
//                if (team.getSport().equals("Football")) {
//                    LinkedHashMap<String, String> simulatedScore = statsBySeasonService.getSimulatedScorePartialSeasons(team, seasonsToDiscard);
//                    outMap.put("beginSeason", team.getBeginSeason());
//                    outMap.put("endSeason", team.getEndSeason());
//                    outMap.putAll(simulatedScore);
//                }
//                break;
//            case "footballGoalsFest":
//                if (team.getSport().equals("Football")) {
//                    LinkedHashMap<String, String> simulatedScore = goalsFestSeasonInfoService.getSimulatedScorePartialSeasons(team, seasonsToDiscard);
//                    outMap.put("beginSeason", team.getBeginSeason());
//                    outMap.put("endSeason", team.getEndSeason());
//                    outMap.putAll(simulatedScore);
//                }
//                break;
            case "footballEuroHandicap":
                if (team.getSport().equals("Football")) {
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, EuroHandicapSeasonInfo.class);
                }
                break;
            case "basketComebacks":
                if (team.getSport().equals("Basketball")) {
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, ComebackSeasonInfo.class);
                }
                break;
            default:
                break;
        }

        return outMap;
    }

}


