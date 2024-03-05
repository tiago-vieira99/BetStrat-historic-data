package com.api.BetStrat.service;

import com.api.BetStrat.entity.StrategySeasonStats;

import java.util.List;

public abstract class StrategyScoreCalculator<T extends StrategySeasonStats> {

    public abstract String calculateFinalRating(double score);

    public abstract int calculateLast3SeasonsRateScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsRateScore(List<T> statsByTeam);

    public abstract int calculateLast3SeasonsTotalWinsRateScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsTotalWinsRateScore(List<T> statsByTeam);

    public abstract int calculateLast3SeasonsMaxSeqWOGreenScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsMaxSeqWOGreenScore(List<T> statsByTeam);

    public abstract int calculateLast3SeasonsStdDevScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsStdDevScore(List<T> statsByTeam);

}
