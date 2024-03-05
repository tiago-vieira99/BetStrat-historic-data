package com.api.BetStrat.service;

import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;

import java.util.List;

public interface StrategySeasonStatsInterface<T extends StrategySeasonStats> {

    T insertStrategySeasonStats(T strategySeasonStats);

    List<T> getStatsByStrategyAndTeam(Team team, String strategyName);

    void updateStrategySeasonStats(Team team, Class<T> className);

    Team updateTeamScore(Team team, Class<T> className);

    String calculateFinalRating(double score, Class<T> className);

    int calculateLast3SeasonsRateScore(List<T> statsByTeam);

    int calculateAllSeasonsRateScore(List<T> statsByTeam);

    int calculateLast3SeasonsTotalWinsRateScore(List<T> statsByTeam);

    int calculateAllSeasonsTotalWinsRateScore(List<T> statsByTeam);

    int calculateLast3SeasonsMaxSeqWOGreenScore(List<T> statsByTeam);

    int calculateAllSeasonsMaxSeqWOGreenScore(List<T> statsByTeam);

    int calculateLast3SeasonsStdDevScore(List<T> statsByTeam);

    int calculateAllSeasonsStdDevScore(List<T> statsByTeam);

}
