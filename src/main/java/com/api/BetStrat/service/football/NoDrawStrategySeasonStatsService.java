package com.api.BetStrat.service.football;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.DrawSeasonStats;
import com.api.BetStrat.entity.football.NoDrawSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.NoDrawSeasonInfoRepository;
import com.api.BetStrat.service.StrategyScoreCalculator;
import com.api.BetStrat.service.StrategySeasonStatsInterface;
import com.api.BetStrat.util.Utils;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class NoDrawStrategySeasonStatsService extends StrategyScoreCalculator<NoDrawSeasonStats> implements StrategySeasonStatsInterface<NoDrawSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoDrawStrategySeasonStatsService.class);

    @Autowired
    private NoDrawSeasonInfoRepository noDrawSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public NoDrawSeasonStats insertStrategySeasonStats(NoDrawSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return noDrawSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<NoDrawSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return noDrawSeasonInfoRepository.getFootballNoDrawStatsByTeam(team);
    }

    @Override
    public List<Map> simulateStrategyBySeason(String season, Team team, String strategyName) {
        return null;
    }

    @Override
    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategyName) {
        return false;
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<NoDrawSeasonStats> statsByTeam = noDrawSeasonInfoRepository.getFootballNoDrawStatsByTeam(team);
        List<String> seasonsList = null;

        if (SUMMER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = SUMMER_SEASONS_LIST;
        } else if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = WINTER_SEASONS_LIST;
        }

        for (String season : seasonsList) {
            if (!statsByTeam.stream().filter(s -> s.getSeason().equals(season)).findAny().isPresent()) {
                String newSeasonUrl = "";

                List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
                String mainCompetition = Utils.findMainCompetition(teamMatchesBySeason);
                List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(t -> t.getCompetition().equals(mainCompetition)).collect(Collectors.toList());
                filteredMatches.sort(new Utils.MatchesByDateSorter());

                if (filteredMatches.size() == 0) {
                    continue;
                }

                NoDrawSeasonStats noDrawSeasonInfo = new NoDrawSeasonStats();

                ArrayList<Integer> strategySequence = new ArrayList<>();
                int count = 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    String res = historicMatch.getFtResult().split("\\(")[0];
                    count++;
                    int homeResult = Integer.parseInt(res.split(":")[0]);
                    int awayResult = Integer.parseInt(res.split(":")[1]);
                    if (homeResult != awayResult) {
                        strategySequence.add(count);
                        count = 0;
                    }
                }

                int totalNoDraws = strategySequence.size();

                strategySequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                String lastResult = lastMatch.getFtResult().split("\\(")[0];
                if (Integer.parseInt(lastResult.split(":")[0]) == Integer.parseInt(lastResult.split(":")[1])) {
                    strategySequence.add(-1);
                }

                if (totalNoDraws == 0) {
                    noDrawSeasonInfo.setNoDrawRate(0);
                } else {
                    noDrawSeasonInfo.setNoDrawRate(Utils.beautifyDoubleValue(100*totalNoDraws/filteredMatches.size()));
                }
                noDrawSeasonInfo.setCompetition(mainCompetition);
                noDrawSeasonInfo.setNegativeSequence(strategySequence.toString());
                noDrawSeasonInfo.setNumNoDraws(totalNoDraws);
                noDrawSeasonInfo.setNumMatches(filteredMatches.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(strategySequence));
                noDrawSeasonInfo.setStdDeviation(stdDev);
                noDrawSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, strategySequence)));
                noDrawSeasonInfo.setSeason(season);
                noDrawSeasonInfo.setTeamId(team);
                noDrawSeasonInfo.setUrl(newSeasonUrl);
                insertStrategySeasonStats(noDrawSeasonInfo);
            }
        }
    }

    @Override
    public Team updateTeamScore (Team team) {
        List<NoDrawSeasonStats> statsByTeam = noDrawSeasonInfoRepository.getFootballNoDrawStatsByTeam(team);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            team.setDrawsHunterScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsDrawRateScore = calculateLast3SeasonsRateScore(statsByTeam);
            int allSeasonsDrawRateScore = calculateAllSeasonsRateScore(statsByTeam);
            int last3SeasonsmaxSeqWODrawScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
            int allSeasonsmaxSeqWODrawScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsDrawRateScore + 0.15*allSeasonsDrawRateScore +
                    0.15*last3SeasonsmaxSeqWODrawScore + 0.05*allSeasonsmaxSeqWODrawScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.05*totalMatchesScore);

            team.setNoDrawsScore(calculateFinalRating(totalScore));
        }

        return team;
    }

//    public LinkedHashMap<String, String> getSimulatedScorePartialSeasons(Team teamByName, int seasonsToDiscard) {
//        List<NoDrawSeasonStats> statsByTeam = noDrawSeasonInfoRepository.getFootballNoDrawStatsByTeam(teamByName);
//        LinkedHashMap<String, String> outMap = new LinkedHashMap<>();
//        List<String> profits = ImmutableList.of("1","0,6","1,2","1,3","2","2,8","4,3","6,6","-20,4","-23,4","-19,8","-19,2","-15");
//
//        if (statsByTeam.size() <= 2 || statsByTeam.size() < seasonsToDiscard) {
//            outMap.put("footballDrawHunter", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
//            return outMap;
//        }
//        Collections.sort(statsByTeam, new SortStatsDataBySeason());
//        Collections.reverse(statsByTeam);
//        List<NoDrawSeasonStats> filteredStats = statsByTeam.subList(seasonsToDiscard, statsByTeam.size());
//
//        if (filteredStats.size() < 3 || !filteredStats.get(0).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-1-seasonsToDiscard)) ||
//                !filteredStats.get(1).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-2-seasonsToDiscard)) ||
//                !filteredStats.get(2).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-3-seasonsToDiscard))) {
//            outMap.put("footballDrawHunter", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
//        } else {
//            int last3SeasonsDrawRateScore = calculateLast3SeasonsRateScore(filteredStats);
//            int allSeasonsDrawRateScore = calculateAllSeasonsRateScore(filteredStats);
//            int last3SeasonsmaxSeqWODrawScore = calculateLast3SeasonsMaxSeqWOGreenScore(filteredStats);
//            int allSeasonsmaxSeqWODrawScore = calculateAllSeasonsMaxSeqWOGreenScore(filteredStats);
//            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(filteredStats);
//            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(filteredStats);
//            int totalMatchesScore = super.calculateLeagueMatchesScore(filteredStats.get(0).getNumMatches());
//
//            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsDrawRateScore + 0.15*allSeasonsDrawRateScore +
//                    0.15*last3SeasonsmaxSeqWODrawScore + 0.05*allSeasonsmaxSeqWODrawScore +
//                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.05*totalMatchesScore);
//
//            String finalScore = calculateFinalRating(totalScore);
//            outMap.put("footballDrawHunter", finalScore);
//            outMap.put("sequence", statsByTeam.get(seasonsToDiscard-1).getNegativeSequence());
//            double balance = 0;
//            String[] seqArray = statsByTeam.get(seasonsToDiscard - 1).getNegativeSequence().replaceAll("\\[","").replaceAll("]","").split(",");
//            for (int i=0; i<seqArray.length-2; i++) {
//                int excelBadRun = 5;
//                int accepBadRun = 6;
//                double drawsScorePoints = Double.parseDouble(finalScore.substring(finalScore.indexOf('(') + 1, finalScore.indexOf(')')));
//                if (finalScore.contains("EXCEL") && Integer.parseInt(seqArray[i].trim()) > excelBadRun) {
//                    if (Integer.parseInt(seqArray[i].trim())-excelBadRun > 6) {
//                        balance += -10;
//                        continue;
//                    }
//                   balance += Double.parseDouble(profits.get(Integer.parseInt(seqArray[i].trim())-excelBadRun-1).replaceAll(",","."));
//                } else if (drawsScorePoints >= 70 && finalScore.contains("ACCEPTABLE") &&  Integer.parseInt(seqArray[i].trim()) > accepBadRun) {
//                    if (Integer.parseInt(seqArray[i].trim())-accepBadRun > 6) {
//                        balance += -10;
//                        continue;
//                    }
//                    balance += Double.parseDouble(profits.get(Integer.parseInt(seqArray[i].trim())-accepBadRun-1).replaceAll(",","."));
//                }
//            }
//            outMap.put("balance", String.valueOf(balance).replaceAll("\\.",","));
//        }
//        return outMap;
//    }

    public String calculateFinalRating(double score) {
        return super.calculateFinalRating(score);
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<NoDrawSeasonStats> statsByTeam) {
        int result = 0;
        double sumDrawRates = 0;
        for (int i = 0; i < 3; i++) {
            sumDrawRates += statsByTeam.get(i).getNoDrawRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(sumDrawRates / 3);

        if (isBetween(avgDrawRate, 35, 100)) {
            result = 100;
        } else if (isBetween(avgDrawRate, 30, 35)) {
            result = 90;
        } else if (isBetween(avgDrawRate, 27, 30)) {
            result = 80;
        } else if (isBetween(avgDrawRate, 25, 27)) {
            result = 60;
        } else if (isBetween(avgDrawRate, 20, 25)) {
            result = 50;
        } else if (isBetween(avgDrawRate, 0, 20)) {
            result = 30;
        }
        return result;
    }

    @Override
    public int calculateAllSeasonsRateScore(List<NoDrawSeasonStats> statsByTeam) {
        double sumDrawRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumDrawRates += statsByTeam.get(i).getNoDrawRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(sumDrawRates / statsByTeam.size());

        if (isBetween(avgDrawRate,35,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,30,35)) {
            return 90;
        } else if(isBetween(avgDrawRate,27,30)) {
            return 80;
        } else if(isBetween(avgDrawRate,25,27)) {
            return 60;
        } else if(isBetween(avgDrawRate,20,25)) {
            return 50;
        } else if(isBetween(avgDrawRate,0,20)) {
            return 30;
        }
        return 0;
    }

    private int calculateRecommendedLevelToStartSequence(List<DrawSeasonStats> statsByTeam) {
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
    public int calculateLast3SeasonsTotalWinsRateScore(List<NoDrawSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<NoDrawSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public int calculateLast3SeasonsMaxSeqWOGreenScore(List<NoDrawSeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,7)) {
            return 100;
        } else if(isBetween(maxValue,7,8)) {
            return 90;
        } else if(isBetween(maxValue,8,9)) {
            return 80;
        } else if(isBetween(maxValue,9,10)) {
            return 70;
        } else if(isBetween(maxValue,10,13)) {
            return 60;
        }  else if(isBetween(maxValue,12,15)) {
            return 50;
        }  else if(isBetween(maxValue,14,15)) {
            return 40;
        } else if(isBetween(maxValue,14,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsMaxSeqWOGreenScore(List<NoDrawSeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,7)) {
            return 100;
        } else if(isBetween(maxValue,7,8)) {
            return 90;
        } else if(isBetween(maxValue,8,9)) {
            return 80;
        } else if(isBetween(maxValue,9,10)) {
            return 70;
        } else if(isBetween(maxValue,10,13)) {
            return 60;
        }  else if(isBetween(maxValue,12,15)) {
            return 50;
        }  else if(isBetween(maxValue,14,15)) {
            return 40;
        } else if(isBetween(maxValue,14,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateLast3SeasonsStdDevScore(List<NoDrawSeasonStats> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<3; i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/3);

        if (isBetween(avgStdDev,0,2.3)) {
            return 100;
        } else if(isBetween(avgStdDev,2.3,2.4)) {
            return 90;
        } else if(isBetween(avgStdDev,2.4,2.5)) {
            return 80;
        } else if(isBetween(avgStdDev,2.5,2.6)) {
            return 70;
        } else if(isBetween(avgStdDev,2.6,2.7)) {
            return 60;
        }  else if(isBetween(avgStdDev,2.7,2.8)) {
            return 50;
        }  else if(isBetween(avgStdDev,2.8,3)) {
            return 40;
        } else if(isBetween(avgStdDev,3,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsStdDevScore(List<NoDrawSeasonStats> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/statsByTeam.size());

        if (isBetween(avgStdDev,0,2.3)) {
            return 100;
        } else if(isBetween(avgStdDev,2.3,2.4)) {
            return 90;
        } else if(isBetween(avgStdDev,2.4,2.5)) {
            return 80;
        } else if(isBetween(avgStdDev,2.5,2.6)) {
            return 70;
        } else if(isBetween(avgStdDev,2.6,2.7)) {
            return 60;
        }  else if(isBetween(avgStdDev,2.7,2.8)) {
            return 50;
        }  else if(isBetween(avgStdDev,2.8,3)) {
            return 40;
        } else if(isBetween(avgStdDev,3,25)) {
            return 30;
        }
        return 0;
    }

    static class SortStatsDataBySeason implements Comparator<NoDrawSeasonStats> {

        @Override
        public int compare(NoDrawSeasonStats a, NoDrawSeasonStats b) {
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
