package com.api.BetStrat.service;

import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.basketball.ComebackSeasonStats;
import com.api.BetStrat.entity.basketball.LongBasketWinsSeasonStats;
import com.api.BetStrat.entity.basketball.ShortBasketWinsSeasonStats;
import com.api.BetStrat.entity.football.DrawSeasonStats;
import com.api.BetStrat.entity.football.EuroHandicapSeasonStats;
import com.api.BetStrat.entity.football.FlipFlopOversUndersStats;
import com.api.BetStrat.entity.football.GoalsFestSeasonStats;
import com.api.BetStrat.entity.football.WinsMarginSeasonStats;
import com.api.BetStrat.entity.handball.Handball16WinsMarginSeasonStats;
import com.api.BetStrat.entity.handball.Handball49WinsMarginSeasonStats;
import com.api.BetStrat.entity.handball.Handball712WinsMarginSeasonStats;
import com.api.BetStrat.entity.hockey.HockeyDrawSeasonStats;
import com.api.BetStrat.entity.hockey.WinsMargin3SeasonStats;
import com.api.BetStrat.entity.hockey.WinsMarginAny2SeasonStats;
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
    private StrategySeasonStatsService statsBySeasonService;

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
                statsBySeasonService.updateStrategySeasonStats(team, DrawSeasonStats.class);
                break;
            case "footballMarginWins":
                statsBySeasonService.updateStrategySeasonStats(team, WinsMarginSeasonStats.class);
                break;
            case "footballGoalsFest":
                statsBySeasonService.updateStrategySeasonStats(team, GoalsFestSeasonStats.class);
                break;
            case "footballEuroHandicap":
                statsBySeasonService.updateStrategySeasonStats(team, EuroHandicapSeasonStats.class);
                break;
            case "footballFlipFlop":
                statsBySeasonService.updateStrategySeasonStats(team, FlipFlopOversUndersStats.class);
                break;
            default:
                break;
        }

        return team;
    }

    public List<HockeyDrawSeasonStats> getHockeyTeamDrawStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Hockey");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<HockeyDrawSeasonStats> statsByTeam = hockeyDrawSeasonInfoRepository.getHockeyDrawStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<DrawSeasonStats> getTeamDrawStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<DrawSeasonStats> statsByTeam = drawSeasonInfoRepository.getFootballDrawStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<Handball49WinsMarginSeasonStats> getTeamMargin49WinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Handball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<Handball49WinsMarginSeasonStats> statsByTeam = handballWinsMargin49SeasonInfoRepository.getHandball49WinsMarginStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<Handball16WinsMarginSeasonStats> getTeamMargin16WinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Handball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<Handball16WinsMarginSeasonStats> statsByTeam = handballWinsMargin16SeasonInfoRepository.getHandball16WinsMarginStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<Handball712WinsMarginSeasonStats> getTeamMargin712WinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Handball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<Handball712WinsMarginSeasonStats> statsByTeam = handballWinsMargin712SeasonInfoRepository.getHandball712WinsMarginStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<ComebackSeasonStats> getTeamComebackWinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Basketball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<ComebackSeasonStats> statsByTeam = comebackSeasonInfoRepository.getComebackStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<ShortBasketWinsSeasonStats> getTeamShortWinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Basketball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<ShortBasketWinsSeasonStats> statsByTeam = shortWinsSeasonInfoRepository.getShortBasketWinsStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<LongBasketWinsSeasonStats> getTeamLongWinsStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Basketball");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<LongBasketWinsSeasonStats> statsByTeam = longWinsSeasonInfoRepository.getLongBasketWinsStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<WinsMarginSeasonStats> getTeamMarginWinStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<WinsMarginSeasonStats> statsByTeam = winsMarginSeasonInfoRepository.getFootballWinsMarginStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<EuroHandicapSeasonStats> getTeamEuroHandicapStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<EuroHandicapSeasonStats> statsByTeam = euroHandicapSeasonInfoRepository.getEuroHandicapStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<GoalsFestSeasonStats> getTeamGoalsFestStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<GoalsFestSeasonStats> statsByTeam = goalsFestSeasonInfoRepository.getGoalsFestStatsByTeam(teamByName);
        return statsByTeam;
    }

    public List<FlipFlopOversUndersStats> getTeamFlipFlopStats(String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }

        List<FlipFlopOversUndersStats> statsByTeam = flipFlopOversUndersInfoRepository.getFlipFlopStatsByTeam(teamByName);
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
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, HockeyDrawSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "hockeyWinsMarginAny2":
                if (teamByName.getSport().equals("Hockey")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, WinsMarginAny2SeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "hockeyWinsMargin3":
                if (teamByName.getSport().equals("Hockey")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, WinsMargin3SeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballDrawHunter":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, DrawSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballMarginWins":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, WinsMarginSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballGoalsFest":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, GoalsFestSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballEuroHandicap":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, EuroHandicapSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "footballFlipFlop":
                if (teamByName.getSport().equals("Football")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, FlipFlopOversUndersStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "basketComebacks":
                if (teamByName.getSport().equals("Basketball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, ComebackSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "basketShortWins":
                if (teamByName.getSport().equals("Basketball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, ShortBasketWinsSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "basketLongWins":
                if (teamByName.getSport().equals("Basketball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, LongBasketWinsSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "handballmargin16wins":
                if (teamByName.getSport().equals("Handball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, Handball16WinsMarginSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "handballmargin49wins":
                if (teamByName.getSport().equals("Handball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, Handball49WinsMarginSeasonStats.class);
                    teamRepository.save(updatedTeam);
                }
                break;
            case "handballmargin712wins":
                if (teamByName.getSport().equals("Handball")) {
                    updatedTeam = statsBySeasonService.updateTeamScore(teamByName, Handball712WinsMarginSeasonStats.class);
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
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, HockeyDrawSeasonStats.class);
                }
                break;
            case "hockeyWinsMarginAny2":
                if (team.getSport().equals("Hockey")) {
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, WinsMarginAny2SeasonStats.class);
                }
                break;
            case "hockeyWinsMargin3":
                if (team.getSport().equals("Hockey")) {
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, WinsMargin3SeasonStats.class);
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
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, EuroHandicapSeasonStats.class);
                }
                break;
            case "basketComebacks":
                if (team.getSport().equals("Basketball")) {
                    simulatedTeam = statsBySeasonService.updateTeamScore(team, ComebackSeasonStats.class);
                }
                break;
            default:
                break;
        }

        return outMap;
    }

}


