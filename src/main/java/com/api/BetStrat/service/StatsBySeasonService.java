package com.api.BetStrat.service;

import com.api.BetStrat.entity.StatsBySeasonInfo;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StatsBySeasonService<T extends StatsBySeasonInfo> implements SeasonInfoInterface<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsBySeasonService.class);

    // Create a map to store service implementations for each type
    private final Map<Class<? extends StatsBySeasonInfo>, SeasonInfoInterface<?>> serviceMap;

    // Strategy Pattern
    // Inject the required service implementations into the constructor
    public StatsBySeasonService(ComebackSeasonInfoService comebackSeasonInfoService,
                                LongBasketWinsSeasonInfoService longBasketWinsSeasonInfoService,
                                ShortBasketWinsSeasonInfoService shortBasketWinsSeasonInfoService,
                                DrawSeasonInfoService drawSeasonInfoService,
                                EuroHandicapSeasonInfoService euroHandicapSeasonInfoService,
                                FlipFlopOversUndersInfoService flipFlopOversUndersInfoService,
                                GoalsFestSeasonInfoService goalsFestSeasonInfoService,
                                WinsMarginSeasonInfoService winsMarginSeasonInfoService,
                                HandballWinsMargin16SeasonInfoService handballWinsMargin16SeasonInfoService,
                                HandballWinsMargin49SeasonInfoService handballWinsMargin49SeasonInfoService,
                                HandballWinsMargin712SeasonInfoService handballWinsMargin712SeasonInfoService,
                                HockeyDrawSeasonInfoService hockeyDrawSeasonInfoService,
                                WinsMargin3SeasonInfoService winsMargin3SeasonInfoService,
                                WinsMarginAny2SeasonInfoService winsMarginAny2SeasonInfoService) {
        // Initialize the map
        serviceMap = new HashMap<>();
        // Associate each service with its corresponding type
        serviceMap.put(ComebackSeasonInfo.class, comebackSeasonInfoService);
        serviceMap.put(LongBasketWinsSeasonInfo.class, longBasketWinsSeasonInfoService);
        serviceMap.put(ShortBasketWinsSeasonInfo.class, shortBasketWinsSeasonInfoService);
        serviceMap.put(DrawSeasonInfo.class, drawSeasonInfoService);
        serviceMap.put(EuroHandicapSeasonInfo.class, euroHandicapSeasonInfoService);
        serviceMap.put(FlipFlopOversUndersInfo.class, flipFlopOversUndersInfoService);
        serviceMap.put(GoalsFestSeasonInfo.class, goalsFestSeasonInfoService);
        serviceMap.put(WinsMarginSeasonInfo.class, winsMarginSeasonInfoService);
        serviceMap.put(Handball16WinsMarginSeasonInfo.class, handballWinsMargin16SeasonInfoService);
        serviceMap.put(Handball49WinsMarginSeasonInfo.class, handballWinsMargin49SeasonInfoService);
        serviceMap.put(Handball712WinsMarginSeasonInfo.class, handballWinsMargin712SeasonInfoService);
        serviceMap.put(HockeyDrawSeasonInfo.class, hockeyDrawSeasonInfoService);
        serviceMap.put(WinsMargin3SeasonInfo.class, winsMargin3SeasonInfoService);
        serviceMap.put(WinsMarginAny2SeasonInfo.class, winsMarginAny2SeasonInfoService);
    }

    @Override
    public T insertStatsBySeasonInfo(T statsBySeasonInfo) {
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(statsBySeasonInfo.getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsBySeasonInfo.getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.insertStatsBySeasonInfo(statsBySeasonInfo);
    }

    @Override
    public void updateStatsBySeasonInfo(Team team, Class<T> className) {
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(className);
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + className.getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        service.updateStatsBySeasonInfo(team, null);
    }

    @Override
    public Team updateTeamScore(Team team, Class<T> className) {
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(className);
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + className.getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.updateTeamScore(team, null);
    }

    @Override
    public String calculateFinalRating(double score, Class<T> className) {
        // Get the service implementation corresponding to the type of statsBySeasonInfo
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(className);
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
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
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
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
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
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
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
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
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
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
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
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
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
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
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
        SeasonInfoInterface<T> service = (SeasonInfoInterface<T>) serviceMap.get(statsByTeam.get(0).getClass());
        if (service == null) {
            throw new IllegalArgumentException("No service implementation found for type: " + statsByTeam.get(0).getClass().getSimpleName());
        }
        // Delegate the insertion to the corresponding service implementation
        return service.calculateAllSeasonsStdDevScore(statsByTeam);
    }

}
