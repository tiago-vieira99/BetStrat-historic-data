package com.api.BetStrat.service;

import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.service.basketball.ComebackStrategySeasonStatsService;
import com.api.BetStrat.service.basketball.LongBasketWinsStrategySeasonStatsService;
import com.api.BetStrat.service.basketball.ShortBasketWinsStrategySeasonStatsService;
import com.api.BetStrat.service.football.*;
import com.api.BetStrat.service.handball.HandballWinsMargin16StrategySeasonStatsService;
import com.api.BetStrat.service.handball.HandballWinsMargin49StrategySeasonStatsService;
import com.api.BetStrat.service.handball.HandballWinsMargin712StrategySeasonStatsService;
import com.api.BetStrat.service.hockey.HockeyDrawStrategySeasonStatsService;
import com.api.BetStrat.service.hockey.WinsMargin3StrategySeasonStatsService;
import com.api.BetStrat.service.hockey.WinsMarginAny2StrategySeasonStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Service
@Transactional
public class TeamService<T extends StrategySeasonStats> extends StrategyMappingPattern {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private StrategySeasonStatsService statsBySeasonService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;


    public TeamService(ComebackStrategySeasonStatsService comebackStrategySeasonstatsService,
                                  LongBasketWinsStrategySeasonStatsService longBasketWinsStrategySeasonstatsService,
                                  ShortBasketWinsStrategySeasonStatsService shortBasketWinsStrategySeasonstatsService,
                                  DrawStrategySeasonStatsService drawStrategySeasonstatsService,
                                  EuroHandicapStrategySeasonStatsService euroHandicapStrategySeasonstatsService,
                                  FlipFlopOversUndersStatsServiceStrategy flipFlopOversUndersInfoService,
                                  GoalsFestStrategySeasonStatsService goalsFestStrategySeasonstatsService,
                                  NoGoalsFestStrategySeasonStatsService noGoalsFestStrategySeasonstatsService,
                                  WinsMarginStrategySeasonStatsService winsMarginStrategySeasonstatsService,
                                  WinsMarginHomeStrategySeasonStatsService winsMarginHomeStrategySeasonStatsService,
                                  WinsStrategySeasonStatsService winsStrategySeasonStatsService,
                                  NoWinsStrategySeasonStatsService noWinsStrategySeasonStatsService,
                                  CleanSheetStrategySeasonStatsService cleanSheetStrategySeasonStatsService,
                                  BttsStrategySeasonStatsService bttsStrategySeasonStatsService,
                                  NoBttsStrategySeasonStatsService noBttsStrategySeasonStatsService,
                                  ScoreBothHalvesSeasonStatsService scoreBothHalvesSeasonStatsService,
                                  ConcedeBothHalvesSeasonStatsService concedeBothHalvesSeasonStatsService,
                                  WinBothHalvesStrategySeasonStatsService winBothHalvesStrategySeasonStatsService,
                                  WinFirstHalfStrategySeasonStatsService winFirstHalfStrategySeasonStatsService,
                                  NoWinFirstHalfStrategySeasonStatsService noWinFirstHalfStrategySeasonStatsService,
                                  WinAndGoalsStrategySeasonStatsService winAndGoalsStrategySeasonStatsService,
                                  SecondHalfBiggerSeasonStatsService secondHalfBiggerSeasonStatsService,
                                  HandballWinsMargin16StrategySeasonStatsService handballWinsMargin16StrategySeasonstatsService,
                                  HandballWinsMargin49StrategySeasonStatsService handballWinsMargin49StrategySeasonstatsService,
                                  HandballWinsMargin712StrategySeasonStatsService handballWinsMargin712StrategySeasonstatsService,
                                  HockeyDrawStrategySeasonStatsService hockeyDrawStrategySeasonstatsService,
                                  WinsMargin3StrategySeasonStatsService winsMargin3StrategySeasonstatsService,
                                  WinsMarginAny2StrategySeasonStatsService winsMarginAny2StrategySeasonstatsService) {
        super(comebackStrategySeasonstatsService, longBasketWinsStrategySeasonstatsService, shortBasketWinsStrategySeasonstatsService, drawStrategySeasonstatsService, euroHandicapStrategySeasonstatsService,
                flipFlopOversUndersInfoService, goalsFestStrategySeasonstatsService, noGoalsFestStrategySeasonstatsService, winsMarginStrategySeasonstatsService, winsMarginHomeStrategySeasonStatsService,
                winsStrategySeasonStatsService, noWinsStrategySeasonStatsService, cleanSheetStrategySeasonStatsService, bttsStrategySeasonStatsService, noBttsStrategySeasonStatsService, scoreBothHalvesSeasonStatsService,
                concedeBothHalvesSeasonStatsService, winBothHalvesStrategySeasonStatsService, winFirstHalfStrategySeasonStatsService, noWinFirstHalfStrategySeasonStatsService, winAndGoalsStrategySeasonStatsService, secondHalfBiggerSeasonStatsService,
                handballWinsMargin16StrategySeasonstatsService, handballWinsMargin49StrategySeasonstatsService, handballWinsMargin712StrategySeasonstatsService, hockeyDrawStrategySeasonstatsService, winsMargin3StrategySeasonstatsService,
                winsMarginAny2StrategySeasonstatsService);
    }


    public Team insertTeam(Team team) {
        return teamRepository.save(team);
    }

    public Team updateTeamScore (Team team, String strategy) {
        LOGGER.info("Updating score for " + team.getName() + " and strategy " + strategy);

        // Get the service implementation corresponding to the type of strategy
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>)
                serviceMap.get(serviceMap.keySet().stream().filter(s -> s.getSimpleName().equals(strategy)).findFirst().get());
        // Delegate the insertion to the corresponding service implementation
        Team updatedTeam = service.updateTeamScore(team);
        teamRepository.save(updatedTeam);

        return updatedTeam;
    }

    public HashMap<String, String> getSimulatedTeamScoreByFilteredSeason (Team team, String strategy, int seasonsToDiscard) {

        HashMap<String, String> outMap = new LinkedHashMap<>();
        Team simulatedTeam = null;

        switch (strategy) {
//            case "hockeyDraw":
//                if (team.getSport().equals("Hockey")) {
//                    simulatedTeam = statsBySeasonService.updateTeamScore(team, HockeyDrawSeasonStats.class);
//                }
//                break;
//            case "hockeyWinsMarginAny2":
//                if (team.getSport().equals("Hockey")) {
//                    simulatedTeam = statsBySeasonService.updateTeamScore(team, WinsMarginAny2SeasonStats.class);
//                }
//                break;
//            case "hockeyWinsMargin3":
//                if (team.getSport().equals("Hockey")) {
//                    simulatedTeam = statsBySeasonService.updateTeamScore(team, WinsMargin3SeasonStats.class);
//                }
//                break;
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
//            case "footballEuroHandicap":
//                if (team.getSport().equals("Football")) {
//                    simulatedTeam = statsBySeasonService.updateTeamScore(team, EuroHandicapSeasonStats.class);
//                }
//                break;
//            case "basketComebacks":
//                if (team.getSport().equals("Basketball")) {
//                    simulatedTeam = statsBySeasonService.updateTeamScore(team, ComebackSeasonStats.class);
//                }
//                break;
            default:
                break;
        }

        return outMap;
    }

}


