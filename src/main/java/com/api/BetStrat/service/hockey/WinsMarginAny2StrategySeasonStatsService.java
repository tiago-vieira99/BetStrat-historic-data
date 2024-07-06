package com.api.BetStrat.service.hockey;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.hockey.WinsMarginAny2SeasonStats;
import com.api.BetStrat.repository.hockey.WinsMarginAny2SeasonInfoRepository;
import com.api.BetStrat.service.StrategyScoreCalculator;
import com.api.BetStrat.service.StrategySeasonStatsInterface;
import com.api.BetStrat.util.Utils;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.SEASONS_LIST;

@Service
@Transactional
public class WinsMarginAny2StrategySeasonStatsService extends StrategyScoreCalculator<WinsMarginAny2SeasonStats> implements StrategySeasonStatsInterface<WinsMarginAny2SeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinsMarginAny2StrategySeasonStatsService.class);

    @Autowired
    private WinsMarginAny2SeasonInfoRepository winsMarginAny2SeasonInfoRepository;

    @Override
    public WinsMarginAny2SeasonStats insertStrategySeasonStats(WinsMarginAny2SeasonStats strategySeasonStats) {
        return winsMarginAny2SeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<WinsMarginAny2SeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return winsMarginAny2SeasonInfoRepository.getHockeyWinsMarginAny2StatsByTeam(team);
    }

    @Override
    public HashMap<String, Object> getSimulatedMatchesByStrategyAndSeason(String season, Team team, String strategyName) {
        return null;
    }

    @Override
    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategyName) {
        return false;
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<WinsMarginAny2SeasonStats> statsByTeam = winsMarginAny2SeasonInfoRepository.getHockeyWinsMarginAny2StatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setHockeyMarginWinsAny2Score(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsMarginWinsRateScore = calculateLast3SeasonsRateScore(statsByTeam);
            int allSeasonsMarginWinsRateScore = calculateAllSeasonsRateScore(statsByTeam);
            int last3SeasonsTotalWinsRateScore = calculateLast3SeasonsTotalWinsRateScore(statsByTeam);
            int allSeasonsTotalWinsRateScore = calculateAllSeasonsTotalWinsRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOMarginWinsScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
            int allSeasonsmaxSeqWOMarginWinsScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double last3SeasonsWinsAvg = (last3SeasonsTotalWinsRateScore + last3SeasonsMarginWinsRateScore) / 2;
            double allSeasonsWinsAvg = (allSeasonsTotalWinsRateScore + allSeasonsMarginWinsRateScore) / 2;

            double last3SeasonsScore = Utils.beautifyDoubleValue(0.3*last3SeasonsWinsAvg + 0.4*last3SeasonsmaxSeqWOMarginWinsScore + 0.3*last3SeasonsStdDevScore);
            double allSeasonsScore = Utils.beautifyDoubleValue(0.3*allSeasonsWinsAvg + 0.4*allSeasonsmaxSeqWOMarginWinsScore + 0.3*allSeasonsStdDevScore);

            double totalScore = Utils.beautifyDoubleValue(0.70*last3SeasonsScore + 0.25*allSeasonsScore + 0.05*totalMatchesScore);

            teamByName.setHockeyMarginWinsAny2Score(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    @Override
    public int calculateHistoricMaxNegativeSeq(List<WinsMarginAny2SeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public double calculateHistoricAvgNegativeSeq(List<WinsMarginAny2SeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        return null;
    }

    public String calculateFinalRating(double score) {
        return super.calculateFinalRating(score);
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<WinsMarginAny2SeasonStats> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<3; i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(marginWinsRates / 3);

        if (isBetween(avgDrawRate,80,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,70,80)) {
            return 80;
        } else if(isBetween(avgDrawRate,50,70)) {
            return 60;
        } else if(isBetween(avgDrawRate,0,50)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsRateScore(List<WinsMarginAny2SeasonStats> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(marginWinsRates / statsByTeam.size());

        if (isBetween(avgDrawRate,80,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,70,80)) {
            return 80;
        } else if(isBetween(avgDrawRate,50,70)) {
            return 60;
        } else if(isBetween(avgDrawRate,0,50)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateLast3SeasonsTotalWinsRateScore(List<WinsMarginAny2SeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<3; i++) {
            totalWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(totalWinsRates / 3);

        if (isBetween(avgDrawRate,70,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,60,70)) {
            return 80;
        } else if(isBetween(avgDrawRate,40,60)) {
            return 60;
        } else if(isBetween(avgDrawRate,0,40)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<WinsMarginAny2SeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            totalWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(totalWinsRates / statsByTeam.size());

        if (isBetween(avgDrawRate,70,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,60,70)) {
            return 80;
        } else if(isBetween(avgDrawRate,40,60)) {
            return 60;
        } else if(isBetween(avgDrawRate,0,40)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateLast3SeasonsMaxSeqWOGreenScore(List<WinsMarginAny2SeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,5)) {
            return 100;
        } else if(isBetween(maxValue,5,6)) {
            return 90;
        } else if(isBetween(maxValue,6,7)) {
            return 80;
        } else if(isBetween(maxValue,7,8)) {
            return 60;
        } else if(isBetween(maxValue,8,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsMaxSeqWOGreenScore(List<WinsMarginAny2SeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,5)) {
            return 100;
        } else if(isBetween(maxValue,5,6)) {
            return 90;
        } else if(isBetween(maxValue,6,7)) {
            return 80;
        } else if(isBetween(maxValue,7,8)) {
            return 60;
        } else if(isBetween(maxValue,8,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateLast3SeasonsStdDevScore(List<WinsMarginAny2SeasonStats> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<3; i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/3);

        if (isBetween(avgStdDev,0,1.8)) {
            return 100;
        } else if(isBetween(avgStdDev,1.8,2.0)) {
            return 80;
        } else if(isBetween(avgStdDev,2.0,2.2)) {
            return 70;
        } else if(isBetween(avgStdDev,2.2,2.4)) {
            return 50;
        } else if(isBetween(avgStdDev,2.4,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsStdDevScore(List<WinsMarginAny2SeasonStats> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/statsByTeam.size());

        if (isBetween(avgStdDev,0,1.8)) {
            return 100;
        } else if(isBetween(avgStdDev,1.8,2.0)) {
            return 80;
        } else if(isBetween(avgStdDev,2.0,2.2)) {
            return 70;
        } else if(isBetween(avgStdDev,2.2,2.4)) {
            return 50;
        } else if(isBetween(avgStdDev,2.4,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {

    }

    static class SortStatsDataBySeason implements Comparator<WinsMarginAny2SeasonStats> {

        @Override
        public int compare(WinsMarginAny2SeasonStats a, WinsMarginAny2SeasonStats b) {
            return Integer.valueOf(SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(SEASONS_LIST.indexOf(b.getSeason())));
        }
    }

}