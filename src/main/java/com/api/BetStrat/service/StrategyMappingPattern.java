package com.api.BetStrat.service;

import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.basketball.ComebackSeasonStats;
import com.api.BetStrat.entity.basketball.LongBasketWinsSeasonStats;
import com.api.BetStrat.entity.basketball.ShortBasketWinsSeasonStats;
import com.api.BetStrat.entity.football.CleanSheetSeasonStats;
import com.api.BetStrat.entity.football.DrawSeasonStats;
import com.api.BetStrat.entity.football.EuroHandicapSeasonStats;
import com.api.BetStrat.entity.football.FlipFlopOversUndersStats;
import com.api.BetStrat.entity.football.GoalsFestSeasonStats;
import com.api.BetStrat.entity.football.NoCleanSheetSeasonStats;
import com.api.BetStrat.entity.football.NoDrawSeasonStats;
import com.api.BetStrat.entity.football.NoMarginWinsSeasonStats;
import com.api.BetStrat.entity.football.NoWinsSeasonStats;
import com.api.BetStrat.entity.football.WinsMarginSeasonStats;
import com.api.BetStrat.entity.football.WinsSeasonStats;
import com.api.BetStrat.entity.handball.Handball16WinsMarginSeasonStats;
import com.api.BetStrat.entity.handball.Handball49WinsMarginSeasonStats;
import com.api.BetStrat.entity.handball.Handball712WinsMarginSeasonStats;
import com.api.BetStrat.entity.hockey.HockeyDrawSeasonStats;
import com.api.BetStrat.entity.hockey.WinsMargin3SeasonStats;
import com.api.BetStrat.entity.hockey.WinsMarginAny2SeasonStats;
import com.api.BetStrat.service.basketball.ComebackStrategySeasonStatsService;
import com.api.BetStrat.service.basketball.LongBasketWinsStrategySeasonStatsService;
import com.api.BetStrat.service.basketball.ShortBasketWinsStrategySeasonStatsService;
import com.api.BetStrat.service.football.CleanSheetStrategySeasonStatsService;
import com.api.BetStrat.service.football.DrawStrategySeasonStatsService;
import com.api.BetStrat.service.football.EuroHandicapStrategySeasonStatsService;
import com.api.BetStrat.service.football.FlipFlopOversUndersStatsServiceStrategy;
import com.api.BetStrat.service.football.GoalsFestStrategySeasonStatsService;
import com.api.BetStrat.service.football.NoCleanSheetStrategySeasonStatsService;
import com.api.BetStrat.service.football.NoDrawStrategySeasonStatsService;
import com.api.BetStrat.service.football.NoMarginWinsStrategySeasonStatsService;
import com.api.BetStrat.service.football.NoWinsStrategySeasonStatsService;
import com.api.BetStrat.service.football.WinsMarginStrategySeasonStatsService;
import com.api.BetStrat.service.football.WinsStrategySeasonStatsService;
import com.api.BetStrat.service.handball.HandballWinsMargin16StrategySeasonStatsService;
import com.api.BetStrat.service.handball.HandballWinsMargin49StrategySeasonStatsService;
import com.api.BetStrat.service.handball.HandballWinsMargin712StrategySeasonStatsService;
import com.api.BetStrat.service.hockey.HockeyDrawStrategySeasonStatsService;
import com.api.BetStrat.service.hockey.WinsMargin3StrategySeasonStatsService;
import com.api.BetStrat.service.hockey.WinsMarginAny2StrategySeasonStatsService;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class StrategyMappingPattern {

    // Create a map to store service implementations for each type
    public final Map<Class<? extends StrategySeasonStats>, StrategySeasonStatsInterface<?>> serviceMap;

    // Strategy Pattern
    // Inject the required service implementations into the constructor
    public StrategyMappingPattern(ComebackStrategySeasonStatsService comebackStrategySeasonstatsService,
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
                                  NoMarginWinsStrategySeasonStatsService noMarginWinsStrategySeasonStatsService,
                                  CleanSheetStrategySeasonStatsService cleanSheetStrategySeasonStatsService,
                                  NoCleanSheetStrategySeasonStatsService noCleanSheetStrategySeasonStatsService,
                                  HandballWinsMargin16StrategySeasonStatsService handballWinsMargin16StrategySeasonstatsService,
                                  HandballWinsMargin49StrategySeasonStatsService handballWinsMargin49StrategySeasonstatsService,
                                  HandballWinsMargin712StrategySeasonStatsService handballWinsMargin712StrategySeasonstatsService,
                                  HockeyDrawStrategySeasonStatsService hockeyDrawStrategySeasonstatsService,
                                  WinsMargin3StrategySeasonStatsService winsMargin3StrategySeasonstatsService,
                                  WinsMarginAny2StrategySeasonStatsService winsMarginAny2StrategySeasonstatsService) {
        // Initialize the map
        serviceMap = new HashMap<>();
        // Associate each service with its corresponding type
        // Basket Strategies
        serviceMap.put(ComebackSeasonStats.class, comebackStrategySeasonstatsService);
        serviceMap.put(LongBasketWinsSeasonStats.class, longBasketWinsStrategySeasonstatsService);
        serviceMap.put(ShortBasketWinsSeasonStats.class, shortBasketWinsStrategySeasonstatsService);
        // Football Strategies
        serviceMap.put(DrawSeasonStats.class, drawStrategySeasonstatsService);
        serviceMap.put(EuroHandicapSeasonStats.class, euroHandicapStrategySeasonstatsService);
        serviceMap.put(FlipFlopOversUndersStats.class, flipFlopOversUndersInfoService);
        serviceMap.put(GoalsFestSeasonStats.class, goalsFestStrategySeasonstatsService);
        serviceMap.put(WinsMarginSeasonStats.class, winsMarginStrategySeasonstatsService);
        serviceMap.put(WinsSeasonStats.class, winsStrategySeasonStatsService);
        serviceMap.put(NoWinsSeasonStats.class, noWinsStrategySeasonStatsService);
        serviceMap.put(NoDrawSeasonStats.class, noDrawStrategySeasonStatsService);
        serviceMap.put(NoMarginWinsSeasonStats.class, noMarginWinsStrategySeasonStatsService);
        serviceMap.put(CleanSheetSeasonStats.class, cleanSheetStrategySeasonStatsService);
        serviceMap.put(NoCleanSheetSeasonStats.class, noCleanSheetStrategySeasonStatsService);
        // Handball Strategies
        serviceMap.put(Handball16WinsMarginSeasonStats.class, handballWinsMargin16StrategySeasonstatsService);
        serviceMap.put(Handball49WinsMarginSeasonStats.class, handballWinsMargin49StrategySeasonstatsService);
        serviceMap.put(Handball712WinsMarginSeasonStats.class, handballWinsMargin712StrategySeasonstatsService);
        // Hockey Strategies
        serviceMap.put(HockeyDrawSeasonStats.class, hockeyDrawStrategySeasonstatsService);
        serviceMap.put(WinsMargin3SeasonStats.class, winsMargin3StrategySeasonstatsService);
        serviceMap.put(WinsMarginAny2SeasonStats.class, winsMarginAny2StrategySeasonstatsService);
    }

}
