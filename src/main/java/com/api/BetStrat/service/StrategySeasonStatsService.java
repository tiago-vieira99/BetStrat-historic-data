package com.api.BetStrat.service;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
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
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class StrategySeasonStatsService<T extends StrategySeasonStats> extends StrategyMappingPattern implements StrategySeasonStatsInterface<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategySeasonStatsService.class);

    public StrategySeasonStatsService(ComebackStrategySeasonStatsService comebackStrategySeasonstatsService,
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
                concedeBothHalvesSeasonStatsService, winBothHalvesStrategySeasonStatsService, winFirstHalfStrategySeasonStatsService, noWinFirstHalfStrategySeasonStatsService,
                winAndGoalsStrategySeasonStatsService, secondHalfBiggerSeasonStatsService,
                handballWinsMargin16StrategySeasonstatsService, handballWinsMargin49StrategySeasonstatsService, handballWinsMargin712StrategySeasonstatsService, hockeyDrawStrategySeasonstatsService, winsMargin3StrategySeasonstatsService,
                winsMarginAny2StrategySeasonstatsService);
    }

    @Override
    public T insertStrategySeasonStats(T strategySeasonStats) {
        // Get the service implementation corresponding to the type of statsByStrategySeasonstats
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(strategySeasonStats.getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + strategySeasonStats.getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.insertStrategySeasonStats(strategySeasonStats);
    }

    @Override
    public List<T> getStatsByStrategyAndTeam(Team team, String strategy) {
        LOGGER.info("Getting stats for " + team.getName() + " and strategy " + strategy);

        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>)
                serviceMap.get(serviceMap.keySet().stream().filter(s -> s.getSimpleName().equals(strategy)).findFirst().get());

        return service.getStatsByStrategyAndTeam(team, "");
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategy) {
        // Get the service implementation corresponding to the type of strategy
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>)
                serviceMap.get(serviceMap.keySet().stream().filter(s -> s.getSimpleName().equals(strategy)).findFirst().get());
        // Delegate the insertion to the corresponding service implementation
        service.updateStrategySeasonStats(team, null);
    }

    @Override
    public HashMap<String, Object> getSimulatedMatchesByStrategyAndSeason(String season, Team team, String strategy) {
        // Get the service implementation corresponding to the type of strategy
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>)
                serviceMap.get(serviceMap.keySet().stream().filter(s -> s.getSimpleName().equals(strategy)).findFirst().get());
        // Delegate the insertion to the corresponding service implementation
        return service.getSimulatedMatchesByStrategyAndSeason(season, team, strategy);
    }

    @Override
    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategy) {
        // Get the service implementation corresponding to the type of strategy
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>)
                serviceMap.get(serviceMap.keySet().stream().filter(s -> s.getSimpleName().equals(strategy)).findFirst().get());
        // Delegate the insertion to the corresponding service implementation
        return service.matchFollowStrategyRules(historicMatch, teamName, strategy);
    }

    @Override
    public Team updateTeamScore(Team team) {
        return null;
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        // Get the service implementation corresponding to the type of strategy
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>)
                serviceMap.get(serviceMap.keySet().stream().filter(s -> s.getSimpleName().equals(strategy)).findFirst().get());
        // Delegate the insertion to the corresponding service implementation
        return service.calculateScoreBySeason(team, season, strategy);
    }

}
