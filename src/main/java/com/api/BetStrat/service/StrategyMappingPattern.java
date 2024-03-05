package com.api.BetStrat.service;

import com.api.BetStrat.entity.StrategySeasonStats;
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
import com.api.BetStrat.service.basketball.ComebackStrategySeasonStatsService;
import com.api.BetStrat.service.basketball.LongBasketWinsStrategySeasonStatsService;
import com.api.BetStrat.service.basketball.ShortBasketWinsStrategySeasonStatsService;
import com.api.BetStrat.service.football.DrawStrategySeasonStatsService;
import com.api.BetStrat.service.football.EuroHandicapStrategySeasonStatsService;
import com.api.BetStrat.service.football.FlipFlopOversUndersStatsServiceStrategy;
import com.api.BetStrat.service.football.GoalsFestStrategySeasonStatsService;
import com.api.BetStrat.service.football.WinsMarginStrategySeasonStatsService;
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
    public StrategyMappingPattern(ComebackStrategySeasonStatsService comebackSeasonInfoService,
                                  LongBasketWinsStrategySeasonStatsService longBasketWinsSeasonInfoService,
                                  ShortBasketWinsStrategySeasonStatsService shortBasketWinsSeasonInfoService,
                                  DrawStrategySeasonStatsService drawSeasonInfoService,
                                  EuroHandicapStrategySeasonStatsService euroHandicapSeasonInfoService,
                                  FlipFlopOversUndersStatsServiceStrategy flipFlopOversUndersInfoService,
                                  GoalsFestStrategySeasonStatsService goalsFestSeasonInfoService,
                                  WinsMarginStrategySeasonStatsService winsMarginSeasonInfoService,
                                  HandballWinsMargin16StrategySeasonStatsService handballWinsMargin16SeasonInfoService,
                                  HandballWinsMargin49StrategySeasonStatsService handballWinsMargin49SeasonInfoService,
                                  HandballWinsMargin712StrategySeasonStatsService handballWinsMargin712SeasonInfoService,
                                  HockeyDrawStrategySeasonStatsService hockeyDrawSeasonInfoService,
                                  WinsMargin3StrategySeasonStatsService winsMargin3SeasonInfoService,
                                  WinsMarginAny2StrategySeasonStatsService winsMarginAny2SeasonInfoService) {
        // Initialize the map
        serviceMap = new HashMap<>();
        // Associate each service with its corresponding type
        // Basket Strategies
        serviceMap.put(ComebackSeasonStats.class, comebackSeasonInfoService);
        serviceMap.put(LongBasketWinsSeasonStats.class, longBasketWinsSeasonInfoService);
        serviceMap.put(ShortBasketWinsSeasonStats.class, shortBasketWinsSeasonInfoService);
        // Football Strategies
        serviceMap.put(DrawSeasonStats.class, drawSeasonInfoService);
        serviceMap.put(EuroHandicapSeasonStats.class, euroHandicapSeasonInfoService);
        serviceMap.put(FlipFlopOversUndersStats.class, flipFlopOversUndersInfoService);
        serviceMap.put(GoalsFestSeasonStats.class, goalsFestSeasonInfoService);
        serviceMap.put(WinsMarginSeasonStats.class, winsMarginSeasonInfoService);
        // Handball Strategies
        serviceMap.put(Handball16WinsMarginSeasonStats.class, handballWinsMargin16SeasonInfoService);
        serviceMap.put(Handball49WinsMarginSeasonStats.class, handballWinsMargin49SeasonInfoService);
        serviceMap.put(Handball712WinsMarginSeasonStats.class, handballWinsMargin712SeasonInfoService);
        // Hockey Strategies
        serviceMap.put(HockeyDrawSeasonStats.class, hockeyDrawSeasonInfoService);
        serviceMap.put(WinsMargin3SeasonStats.class, winsMargin3SeasonInfoService);
        serviceMap.put(WinsMarginAny2SeasonStats.class, winsMarginAny2SeasonInfoService);
    }

}
