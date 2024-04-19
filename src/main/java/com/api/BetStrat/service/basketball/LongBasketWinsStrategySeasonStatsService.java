package com.api.BetStrat.service.basketball;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.basketball.LongBasketWinsSeasonStats;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.basketball.LongWinsSeasonInfoRepository;
import com.api.BetStrat.service.StrategyScoreCalculator;
import com.api.BetStrat.service.StrategySeasonStatsInterface;
import com.api.BetStrat.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_LIST;
import static com.api.BetStrat.util.Utils.calculateCoeffVariation;
import static com.api.BetStrat.util.Utils.calculateSD;

@Service
@Transactional
public class LongBasketWinsStrategySeasonStatsService extends StrategyScoreCalculator<LongBasketWinsSeasonStats> implements StrategySeasonStatsInterface<LongBasketWinsSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LongBasketWinsStrategySeasonStatsService.class);

    @Autowired
    private LongWinsSeasonInfoRepository longWinsSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public LongBasketWinsSeasonStats insertStrategySeasonStats(LongBasketWinsSeasonStats strategySeasonStats) {
        return longWinsSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<LongBasketWinsSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return longWinsSeasonInfoRepository.getLongBasketWinsStatsByTeam(team);
    }

    @Override
    public List<SimulatedMatchDto> simulateStrategyBySeason(String season, Team team, String strategyName) {
        return null;
    }

    @Override
    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategyName) {
        return false;
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<LongBasketWinsSeasonStats> statsByTeam = longWinsSeasonInfoRepository.getLongBasketWinsStatsByTeam(team);
        List<String> seasonsList = null;

        if (SUMMER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = SUMMER_SEASONS_LIST;
        } else if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = WINTER_SEASONS_LIST;
        }

        for (String season : seasonsList) {
            if (!statsByTeam.stream().filter(s -> s.getSeason().equals(season)).findAny().isPresent()) {

                List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
                String mainCompetition = Utils.findMainCompetition(teamMatchesBySeason);
                List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(t -> t.getCompetition().equals(mainCompetition)).collect(Collectors.toList());
//                filteredMatches.sort(new Utils.MatchesByDateSorter());

                LongBasketWinsSeasonStats longBasketWinsSeasonInfo = new LongBasketWinsSeasonStats();
                LOGGER.info("Insert " + longBasketWinsSeasonInfo.getClass() + " for " + team.getName() + " and season " + season);
                ArrayList<Integer> noLongWinsSequence = new ArrayList<>();
                int count = 0;
                int totalWins= 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    String ftResult = historicMatch.getFtResult().split(" ")[0];
                    count++;
                    int homeFTResult = Integer.parseInt(ftResult.split(":")[0]);
                    int awayFTResult = Integer.parseInt(ftResult.split(":")[1]);

                    if ((historicMatch.getHomeTeam().equals(team.getName()) && homeFTResult > awayFTResult) || (historicMatch.getAwayTeam().equals(team.getName()) && homeFTResult < awayFTResult)) {
                        totalWins++;
                        if (Math.abs(homeFTResult - awayFTResult) >= 11) {
                            noLongWinsSequence.add(count);
                            count = 0;
                        }
                    }
                }

                int totalLongWins = noLongWinsSequence.size();

                noLongWinsSequence.add(count);
                if (noLongWinsSequence.get(noLongWinsSequence.size()-1) != 0) {
                    noLongWinsSequence.add(-1);
                }

                longBasketWinsSeasonInfo.setCompetition(mainCompetition);
                if (totalWins == 0) {
                    longBasketWinsSeasonInfo.setLongWinsRate(0);
                    longBasketWinsSeasonInfo.setWinsRate(0);
                } else {
                    longBasketWinsSeasonInfo.setLongWinsRate(Utils.beautifyDoubleValue(100 * totalLongWins / totalWins));
                    longBasketWinsSeasonInfo.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/ filteredMatches.size()));
                }
                longBasketWinsSeasonInfo.setNegativeSequence(noLongWinsSequence.toString());
                longBasketWinsSeasonInfo.setNumLongWins(totalLongWins);
                longBasketWinsSeasonInfo.setNumMatches(filteredMatches.size());
                longBasketWinsSeasonInfo.setNumWins(totalWins);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(noLongWinsSequence));
                longBasketWinsSeasonInfo.setStdDeviation(stdDev);
                longBasketWinsSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, noLongWinsSequence)));

                longBasketWinsSeasonInfo.setSeason(season);
                longBasketWinsSeasonInfo.setTeamId(team);
                longBasketWinsSeasonInfo.setUrl(team.getUrl());
                insertStrategySeasonStats(longBasketWinsSeasonInfo);
            }
        }
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<LongBasketWinsSeasonStats> statsByTeam = longWinsSeasonInfoRepository.getLongBasketWinsStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setBasketLongWinsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsMarginWinsRateScore = calculateLast3SeasonsRateScore(statsByTeam);
            int allSeasonsMarginWinsRateScore = calculateAllSeasonsRateScore(statsByTeam);
            int last3SeasonsTotalWinsRateScore = calculateLast3SeasonsTotalWinsRateScore(statsByTeam);
            int allSeasonsTotalWinsRateScore = calculateAllSeasonsTotalWinsRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOMarginWinsScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
            int allSeasonsmaxSeqWOMarginWinsScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);

            double last3SeasonsWinsAvg = (last3SeasonsTotalWinsRateScore + last3SeasonsMarginWinsRateScore) / 2;
            double allSeasonsWinsAvg = (allSeasonsTotalWinsRateScore + allSeasonsMarginWinsRateScore) / 2;

            double last3SeasonsScore = Utils.beautifyDoubleValue(0.3*last3SeasonsWinsAvg + 0.4*last3SeasonsmaxSeqWOMarginWinsScore + 0.3*last3SeasonsStdDevScore);
            double allSeasonsScore = Utils.beautifyDoubleValue(0.3*allSeasonsWinsAvg + 0.4*allSeasonsmaxSeqWOMarginWinsScore + 0.3*allSeasonsStdDevScore);

            double totalScore = Utils.beautifyDoubleValue(0.75*last3SeasonsScore + 0.25*allSeasonsScore);

            teamByName.setBasketLongWinsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    public String calculateFinalRating(double score) {
        return super.calculateFinalRating(score);
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<LongBasketWinsSeasonStats> statsByTeam) {
        double sumLongWinsRates = 0;
        for (int i=0; i<3; i++) {
            sumLongWinsRates += statsByTeam.get(i).getNumLongWins();
        }

        double avgLongWinsRate = Utils.beautifyDoubleValue(sumLongWinsRates / 3);

        if (isBetween(avgLongWinsRate,65,100)) {
            return 100;
        } else if(isBetween(avgLongWinsRate,55,65)) {
            return 80;
        } else if(isBetween(avgLongWinsRate,45,55)) {
            return 60;
        } else if(isBetween(avgLongWinsRate,0,55)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsRateScore(List<LongBasketWinsSeasonStats> statsByTeam) {
        double sumLongWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumLongWinsRates += statsByTeam.get(i).getLongWinsRate();
        }

        double avgLongWinsRate = Utils.beautifyDoubleValue(sumLongWinsRates / statsByTeam.size());

        if (isBetween(avgLongWinsRate,65,100)) {
            return 100;
        } else if(isBetween(avgLongWinsRate,55,65)) {
            return 80;
        } else if(isBetween(avgLongWinsRate,45,55)) {
            return 60;
        } else if(isBetween(avgLongWinsRate,0,55)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateLast3SeasonsTotalWinsRateScore(List<LongBasketWinsSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<3; i++) {
            totalWinsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / 3);

        if (isBetween(avgWinsRate,70,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,65,70)) {
            return 90;
        } else if(isBetween(avgWinsRate,60,65)) {
            return 80;
        } else if(isBetween(avgWinsRate,55,60)) {
            return 70;
        } else if(isBetween(avgWinsRate,45,55)) {
            return 60;
        } else if(isBetween(avgWinsRate,0,45)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<LongBasketWinsSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            totalWinsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / statsByTeam.size());

        if (isBetween(avgWinsRate,70,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,65,70)) {
            return 90;
        } else if(isBetween(avgWinsRate,60,65)) {
            return 80;
        } else if(isBetween(avgWinsRate,55,60)) {
            return 70;
        } else if(isBetween(avgWinsRate,45,55)) {
            return 60;
        } else if(isBetween(avgWinsRate,0,45)) {
            return 30;
        }
        return 0;
    }

    private int calculateRecommendedLevelToStartSequence(List<LongBasketWinsSeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i = 0; i < 3; i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }
        return maxValue-6 < 0 ? 0 : maxValue-6;
    }

    @Override
    public int calculateLast3SeasonsMaxSeqWOGreenScore(List<LongBasketWinsSeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,9)) {
            return 100;
        } else if(isBetween(maxValue,9,10)) {
            return 90;
        } else if(isBetween(maxValue,10,11)) {
            return 70;
        } else if(isBetween(maxValue,11,13)) {
            return 50;
        } else if(isBetween(maxValue,13,35)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsMaxSeqWOGreenScore(List<LongBasketWinsSeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,9)) {
            return 100;
        } else if(isBetween(maxValue,9,10)) {
            return 90;
        } else if(isBetween(maxValue,10,11)) {
            return 70;
        } else if(isBetween(maxValue,11,13)) {
            return 50;
        } else if(isBetween(maxValue,13,35)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateLast3SeasonsStdDevScore(List<LongBasketWinsSeasonStats> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<3; i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/3);

        if (isBetween(avgStdDev,0,2.0)) {
            return 100;
        } else if(isBetween(avgStdDev,2.0,2.3)) {
            return 80;
        } else if(isBetween(avgStdDev,2.3,2.6)) {
            return 70;
        } else if(isBetween(avgStdDev,2.6,3)) {
            return 50;
        } else if(isBetween(avgStdDev,3,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsStdDevScore(List<LongBasketWinsSeasonStats> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/statsByTeam.size());

        if (isBetween(avgStdDev,0,2.0)) {
            return 100;
        } else if(isBetween(avgStdDev,2.0,2.3)) {
            return 80;
        } else if(isBetween(avgStdDev,2.3,2.6)) {
            return 70;
        } else if(isBetween(avgStdDev,2.6,3)) {
            return 50;
        } else if(isBetween(avgStdDev,3,25)) {
            return 30;
        }
        return 0;
    }

    static class SortStatsDataBySeason implements Comparator<LongBasketWinsSeasonStats> {

        @Override
        public int compare(LongBasketWinsSeasonStats a, LongBasketWinsSeasonStats b) {
            return Integer.valueOf(SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(SEASONS_LIST.indexOf(b.getSeason())));
        }
    }

    /* avaliar cada parametro independentemente:
     *
     * 1) dar peso a cada parametro:
     *   drawRate (last3) - 25
     *   drawRate (total) - 20
     *   maxSeqWODraw (last3) - 15
     *   maxSeqWODraw (total) - 5
     *   stdDev (last3) - 20
     *   stdDev (total) - 10
     *   numTotalMatches - 5
     *
     *
     *   drawRate -> (100 se > 35) ; (90 se < 35) ; (80 entre 27 e 30) ; (60 entre 25 e 27) ; (50 entre 20 e 25) ; (30 se < 20)
     *   maxSeqWODraw -> (100 se < 7) ; (90 se == 7) ; (80 se == 8) ; (70 se == 9) ; (60 se == 10 ou 11) ; (50 se == 12 ou 13) ; (40 se == 14) ; (30 se > 14)
     *   stdDev -> (100 se < 2.3) ; (90 se < 2.4) ; (80 se < 2.5) ; (70 se < 2.6) ; (60 se < 2.7) ; (50 se < 2.8) ; (40 se < 2.9) ; (30 se > 3)
     *   numTotalMatches -> (100 se < 30) ; (90 se < 32) ; (80 se < 34) ; (50 se < 40) ; (30 se > 40)
     *
     *
     * excellent: avg std dev < 2.1 && avg drawRate > 30 && list.size > 3 && maxSeqValue < 9
     * acceptable: ((avg std dev > 2.1 & < 2.5 ; min drawRate > 23) || avg drawRate > 32) && maxSeqValue <= 10
     * risky: (max std dev > 3 && min drawRate < 20) || maxSeqValue > 15
     *
     * */

}
