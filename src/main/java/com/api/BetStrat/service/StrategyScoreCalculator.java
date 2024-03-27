package com.api.BetStrat.service;

import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.entity.StrategySeasonStats;

import java.util.List;

public abstract class StrategyScoreCalculator<T extends StrategySeasonStats> {

    //TODO refactor
    public String calculateFinalRating(double score) {
        if (isBetween(score,85,150)) {
            return TeamScoreEnum.EXCELLENT.getValue() + " (" + score + ")";
        } else if(isBetween(score,70,85)) {
            return TeamScoreEnum.ACCEPTABLE.getValue() + " (" + score + ")";
        } else if(isBetween(score,50,70)) {
            return TeamScoreEnum.RISKY.getValue() + " (" + score + ")";
        } else if(isBetween(score,0,50)) {
            return TeamScoreEnum.INAPT.getValue() + " (" + score + ")";
        }
        return "";
    }

    public abstract int calculateLast3SeasonsRateScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsRateScore(List<T> statsByTeam);

    public abstract int calculateLast3SeasonsTotalWinsRateScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsTotalWinsRateScore(List<T> statsByTeam);

    public abstract int calculateLast3SeasonsMaxSeqWOGreenScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsMaxSeqWOGreenScore(List<T> statsByTeam);

    public abstract int calculateLast3SeasonsStdDevScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsStdDevScore(List<T> statsByTeam);

    public static int calculateLeagueMatchesScore(int totalMatches) {
        if (isBetween(totalMatches,0,31)) {
            return 100;
        } else if(isBetween(totalMatches,31,33)) {
            return 90;
        } else if(isBetween(totalMatches,33,35)) {
            return 80;
        } else if(isBetween(totalMatches,35,41)) {
            return 60;
        } else if(isBetween(totalMatches,41,50)) {
            return 30;
        }
        return 0;
    }

    public static boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x < upper;
    }

}
