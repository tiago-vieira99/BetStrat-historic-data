package com.api.BetStrat.service;

import com.api.BetStrat.entity.StrategySeasonStats;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StrategySeasonStatsService<T extends StrategySeasonStats> implements StrategySeasonStatsInterface<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategySeasonStatsService.class);

    // Create a map to store service implementations for each type
    private final Map<Class<? extends StrategySeasonStats>, StrategySeasonStatsInterface<?>> serviceMap;

    // Strategy Pattern
    // Inject the required service implementations into the constructor
    public StrategySeasonStatsService(ComebackStrategySeasonStatsService comebackSeasonInfoService,
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
        serviceMap.put(ComebackSeasonStats.class, comebackSeasonInfoService);
        serviceMap.put(LongBasketWinsSeasonStats.class, longBasketWinsSeasonInfoService);
        serviceMap.put(ShortBasketWinsSeasonStats.class, shortBasketWinsSeasonInfoService);
        serviceMap.put(DrawSeasonStats.class, drawSeasonInfoService);
        serviceMap.put(EuroHandicapSeasonStats.class, euroHandicapSeasonInfoService);
        serviceMap.put(FlipFlopOversUndersStats.class, flipFlopOversUndersInfoService);
        serviceMap.put(GoalsFestSeasonStats.class, goalsFestSeasonInfoService);
        serviceMap.put(WinsMarginSeasonStats.class, winsMarginSeasonInfoService);
        serviceMap.put(Handball16WinsMarginSeasonStats.class, handballWinsMargin16SeasonInfoService);
        serviceMap.put(Handball49WinsMarginSeasonStats.class, handballWinsMargin49SeasonInfoService);
        serviceMap.put(Handball712WinsMarginSeasonStats.class, handballWinsMargin712SeasonInfoService);
        serviceMap.put(HockeyDrawSeasonStats.class, hockeyDrawSeasonInfoService);
        serviceMap.put(WinsMargin3SeasonStats.class, winsMargin3SeasonInfoService);
        serviceMap.put(WinsMarginAny2SeasonStats.class, winsMarginAny2SeasonInfoService);
    }

    @Override
    public T insertStrategySeasonStats(T strategySeasonStats) {
        // Get the service implementation corresponding to the type of statsBySeasonInfo
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
    public void updateStrategySeasonStats(Team team, Class<T> className) {
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(className);
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + className.getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        service.updateStrategySeasonStats(team, null);
    }

    @Override
    public Team updateTeamScore(Team team, Class<T> className) {
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(className);
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + className.getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.updateTeamScore(team, null);
    }

    @Override
    public String calculateFinalRating(double score, Class<T> className) {
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(className);
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + className.getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateFinalRating(score, null);
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<T> statsByTeam) {
        if (statsByTeam.size() == 0) {
            return 0;
        }
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateLast3SeasonsRateScore(statsByTeam);
    }

    @Override
    public int calculateAllSeasonsRateScore(List<T> statsByTeam) {
        if (statsByTeam.size() == 0) {
            return 0;
        }
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateAllSeasonsRateScore(statsByTeam);
    }

    @Override
    public int calculateLast3SeasonsMaxSeqWOGreenScore(List<T> statsByTeam) {
        if (statsByTeam.size() == 0) {
            return 0;
        }
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
    }

    @Override
    public int calculateAllSeasonsMaxSeqWOGreenScore(List<T> statsByTeam) {
        if (statsByTeam.size() == 0) {
            return 0;
        }
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
    }

    @Override
    public int calculateLast3SeasonsTotalWinsRateScore(List<T> statsByTeam) {
        if (statsByTeam.size() == 0) {
            return 0;
        }
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateLast3SeasonsTotalWinsRateScore(statsByTeam);
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<T> statsByTeam) {
        if (statsByTeam.size() == 0) {
            return 0;
        }
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateAllSeasonsTotalWinsRateScore(statsByTeam);
    }

    @Override
    public int calculateLast3SeasonsStdDevScore(List<T> statsByTeam) {
        if (statsByTeam.size() == 0) {
            return 0;
        }
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateLast3SeasonsStdDevScore(statsByTeam);
    }

    @Override
    public int calculateAllSeasonsStdDevScore(List<T> statsByTeam) {
        if (statsByTeam.size() == 0) {
            return 0;
        }
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        StrategySeasonStatsInterface<T> service = (StrategySeasonStatsInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateAllSeasonsStdDevScore(statsByTeam);
    }

}
