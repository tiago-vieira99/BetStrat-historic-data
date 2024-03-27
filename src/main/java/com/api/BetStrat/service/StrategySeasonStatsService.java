package com.api.BetStrat.service;

import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinsSeasonStats;
import com.api.BetStrat.service.basketball.ComebackStrategySeasonStatsService;
import com.api.BetStrat.service.basketball.LongBasketWinsStrategySeasonStatsService;
import com.api.BetStrat.service.basketball.ShortBasketWinsStrategySeasonStatsService;
import com.api.BetStrat.service.football.DrawStrategySeasonStatsService;
import com.api.BetStrat.service.football.EuroHandicapStrategySeasonStatsService;
import com.api.BetStrat.service.football.FlipFlopOversUndersStatsServiceStrategy;
import com.api.BetStrat.service.football.GoalsFestStrategySeasonStatsService;
import com.api.BetStrat.service.football.NoDrawStrategySeasonStatsService;
import com.api.BetStrat.service.football.NoWinsStrategySeasonStatsService;
import com.api.BetStrat.service.football.WinsMarginStrategySeasonStatsService;
import com.api.BetStrat.service.football.WinsStrategySeasonStatsService;
import com.api.BetStrat.service.handball.HandballWinsMargin16StrategySeasonStatsService;
import com.api.BetStrat.service.handball.HandballWinsMargin49StrategySeasonStatsService;
import com.api.BetStrat.service.handball.HandballWinsMargin712StrategySeasonStatsService;
import com.api.BetStrat.service.hockey.HockeyDrawStrategySeasonStatsService;
import com.api.BetStrat.service.hockey.WinsMargin3StrategySeasonStatsService;
import com.api.BetStrat.service.hockey.WinsMarginAny2StrategySeasonStatsService;
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
                                      WinsMarginStrategySeasonStatsService winsMarginStrategySeasonstatsService,
                                      WinsStrategySeasonStatsService winsStrategySeasonStatsService,
                                      NoWinsStrategySeasonStatsService noWinsStrategySeasonStatsService,
                                      NoDrawStrategySeasonStatsService noDrawStrategySeasonStatsService,
                                      HandballWinsMargin16StrategySeasonStatsService handballWinsMargin16StrategySeasonstatsService,
                                      HandballWinsMargin49StrategySeasonStatsService handballWinsMargin49StrategySeasonstatsService,
                                      HandballWinsMargin712StrategySeasonStatsService handballWinsMargin712StrategySeasonstatsService,
                                      HockeyDrawStrategySeasonStatsService hockeyDrawStrategySeasonstatsService,
                                      WinsMargin3StrategySeasonStatsService winsMargin3StrategySeasonstatsService,
                                      WinsMarginAny2StrategySeasonStatsService winsMarginAny2StrategySeasonstatsService) {
        super(comebackStrategySeasonstatsService, longBasketWinsStrategySeasonstatsService, shortBasketWinsStrategySeasonstatsService, drawStrategySeasonstatsService, euroHandicapStrategySeasonstatsService,
                flipFlopOversUndersInfoService, goalsFestStrategySeasonstatsService, winsMarginStrategySeasonstatsService, winsStrategySeasonStatsService, noWinsStrategySeasonStatsService,
                noDrawStrategySeasonStatsService, handballWinsMargin16StrategySeasonstatsService,
                handballWinsMargin49StrategySeasonstatsService, handballWinsMargin712StrategySeasonstatsService, hockeyDrawStrategySeasonstatsService, winsMargin3StrategySeasonstatsService,
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
    public Team updateTeamScore(Team team) {
        return null;
    }

//
//    @Override
//    public String calculateFinalRating(double score, String strategy) {
//        // Get the service implementation corresponding to the type of strategy
//        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>)
//                serviceMap.get(serviceMap.keySet().stream().filter(s -> s.getSimpleName().equals(strategy)).findFirst().get());
//        // Delegate the insertion to the corresponding service implementation
//        return service.calculateFinalRating(score, null);
//    }
//
//    @Override
//    public int calculateLast3SeasonsRateScore(List<T> statsByTeam) {
//        if (statsByTeam.size() == 0) {
//            return 0;
//        }
//        // Get the service implementation corresponding to the type of statsByStrategySeasonstats
//        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
//        if (service == null) {
//            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
//        }
//        // Delegate the insertion to the corresponding service implementation
//        return service.calculateLast3SeasonsRateScore(statsByTeam);
//    }
//
//    @Override
//    public int calculateAllSeasonsRateScore(List<T> statsByTeam) {
//        if (statsByTeam.size() == 0) {
//            return 0;
//        }
//        // Get the service implementation corresponding to the type of statsByStrategySeasonstats
//        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
//        if (service == null) {
//            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
//        }
//        // Delegate the insertion to the corresponding service implementation
//        return service.calculateAllSeasonsRateScore(statsByTeam);
//    }
//
//    @Override
//    public int calculateLast3SeasonsMaxSeqWOGreenScore(List<T> statsByTeam) {
//        if (statsByTeam.size() == 0) {
//            return 0;
//        }
//        // Get the service implementation corresponding to the type of statsByStrategySeasonstats
//        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
//        if (service == null) {
//            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
//        }
//        // Delegate the insertion to the corresponding service implementation
//        return service.calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
//    }
//
//    @Override
//    public int calculateAllSeasonsMaxSeqWOGreenScore(List<T> statsByTeam) {
//        if (statsByTeam.size() == 0) {
//            return 0;
//        }
//        // Get the service implementation corresponding to the type of statsByStrategySeasonstats
//        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
//        if (service == null) {
//            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
//        }
//        // Delegate the insertion to the corresponding service implementation
//        return service.calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
//    }
//
//    @Override
//    public int calculateLast3SeasonsTotalWinsRateScore(List<T> statsByTeam) {
//        if (statsByTeam.size() == 0) {
//            return 0;
//        }
//        // Get the service implementation corresponding to the type of statsByStrategySeasonstats
//        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
//        if (service == null) {
//            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
//        }
//        // Delegate the insertion to the corresponding service implementation
//        return service.calculateLast3SeasonsTotalWinsRateScore(statsByTeam);
//    }
//
//    @Override
//    public int calculateAllSeasonsTotalWinsRateScore(List<T> statsByTeam) {
//        if (statsByTeam.size() == 0) {
//            return 0;
//        }
//        // Get the service implementation corresponding to the type of statsByStrategySeasonstats
//        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
//        if (service == null) {
//            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
//        }
//        // Delegate the insertion to the corresponding service implementation
//        return service.calculateAllSeasonsTotalWinsRateScore(statsByTeam);
//    }
//
//    @Override
//    public int calculateLast3SeasonsStdDevScore(List<T> statsByTeam) {
//        if (statsByTeam.size() == 0) {
//            return 0;
//        }
//        // Get the service implementation corresponding to the type of statsByStrategySeasonstats
//        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
//        if (service == null) {
//            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
//        }
//        // Delegate the insertion to the corresponding service implementation
//        return service.calculateLast3SeasonsStdDevScore(statsByTeam);
//    }
//
//    @Override
//    public int calculateAllSeasonsStdDevScore(List<T> statsByTeam) {
//        if (statsByTeam.size() == 0) {
//            return 0;
//        }
//        // Get the service implementation corresponding to the type of statsByStrategySeasonstats
//        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
//        if (service == null) {
//            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
//        }
//        // Delegate the insertion to the corresponding service implementation
//        return service.calculateAllSeasonsStdDevScore(statsByTeam);
//    }

}
