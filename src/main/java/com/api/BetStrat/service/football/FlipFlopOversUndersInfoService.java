package com.api.BetStrat.service.football;

import com.api.BetStrat.constants.TeamScoreEnum;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.FlipFlopOversUndersInfo;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.FlipFlopOversUndersInfoRepository;
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
public class FlipFlopOversUndersInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlipFlopOversUndersInfoService.class);

    @Autowired
    private FlipFlopOversUndersInfoRepository flipFlopOversUndersInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    public FlipFlopOversUndersInfo insertFlipFlopsInfo(FlipFlopOversUndersInfo drawSeasonInfo) {
        LOGGER.info("Inserted " + drawSeasonInfo.getClass() + " for " + drawSeasonInfo.getTeamId().getName() + " and season " + drawSeasonInfo.getSeason());
        return flipFlopOversUndersInfoRepository.save(drawSeasonInfo);
    }

    public void updateStatsDataInfo(Team team) {
        List<FlipFlopOversUndersInfo> statsByTeam = flipFlopOversUndersInfoRepository.getFlipFlopStatsByTeam(team);
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
                teamMatchesBySeason.sort(new Utils.MatchesByDateSorter());

                if (teamMatchesBySeason.size() == 0) {
                    continue;
                }

                FlipFlopOversUndersInfo flipFlopOversUndersInfo = new FlipFlopOversUndersInfo();

                ArrayList<Integer> flipFlopsSequence = new ArrayList<>();
                int count = 0;
                int numOvers = 0;
                int numUnders = 0;
                int flag = 0; //0 unders; 1 - overs
                String firstRes = teamMatchesBySeason.get(0).getFtResult().split("\\(")[0];
                if (Integer.parseInt(firstRes.split(":")[0]) + Integer.parseInt(firstRes.split(":")[1]) > 2) {
                    flag = 1;
                }
                for (int i = 1; i < teamMatchesBySeason.size(); i++) {
                    String res = teamMatchesBySeason.get(i).getFtResult().split("\\(")[0];
                    count++;
                    int homeResult = Integer.parseInt(res.split(":")[0]);
                    int awayResult = Integer.parseInt(res.split(":")[1]);
                    if ((homeResult + awayResult) > 2 && flag == 1) {
                        flipFlopsSequence.add(count);
                        count = 0;
                    } else if ((homeResult + awayResult) < 3 && flag == 0) {
                        flipFlopsSequence.add(count);
                        count = 0;
                    }
                    if ((homeResult + awayResult) > 2) {
                        flag = 1;
                        numOvers++;
                    } else {
                        flag = 0;
                        numUnders++;
                    }
                }

                flipFlopsSequence.add(count);
                HistoricMatch lastMatch = teamMatchesBySeason.get(teamMatchesBySeason.size() - 1);
                String lastResult = lastMatch.getFtResult().split("\\(")[0];
                if (count != 0 && ((Integer.parseInt(lastResult.split(":")[0]) + Integer.parseInt(lastResult.split(":")[1]) > 2 && flag == 1) ||
                        (Integer.parseInt(lastResult.split(":")[0]) + Integer.parseInt(lastResult.split(":")[1]) < 3 && flag == 0))) {
                    flipFlopsSequence.add(-1);
                }

                if (numOvers == 0) {
                    flipFlopOversUndersInfo.setOversRate(0);
                } else {
                    flipFlopOversUndersInfo.setOversRate(Utils.beautifyDoubleValue(100*numOvers/teamMatchesBySeason.size()));
                }
                flipFlopOversUndersInfo.setUndersRate(100-flipFlopOversUndersInfo.getOversRate());
                flipFlopOversUndersInfo.setNumOvers(numOvers);
                flipFlopOversUndersInfo.setNumUnders(numUnders);
                flipFlopOversUndersInfo.setCompetition("all");
                flipFlopOversUndersInfo.setFlipFlopsSequence(flipFlopsSequence.toString());
                flipFlopOversUndersInfo.setNumMatches(teamMatchesBySeason.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(flipFlopsSequence));
                flipFlopOversUndersInfo.setStdDeviation(stdDev);
                flipFlopOversUndersInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, flipFlopsSequence)));
                flipFlopOversUndersInfo.setSeason(season);
                flipFlopOversUndersInfo.setTeamId(team);
                flipFlopOversUndersInfo.setUrl(newSeasonUrl);
                insertFlipFlopsInfo(flipFlopOversUndersInfo);
            }
        }
    }

    public Team updateTeamScore (Team teamByName) {
        List<FlipFlopOversUndersInfo> statsByTeam = flipFlopOversUndersInfoRepository.getFlipFlopStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setDrawsHunterScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsOversRateScore = calculateLast3SeasonsOversRateScore(statsByTeam);
            int allSeasonsOversRateScore = calculateAllSeasonsOversRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOFlipFlopScore = calculateLast3SeasonsmaxSeqWOFlipFlopScore(statsByTeam);
            int allSeasonsmaxSeqWOFlipFlopScore = calculateAllSeasonsmaxSeqWOFlipFlopScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsOversRateScore + 0.15*allSeasonsOversRateScore +
                    0.15*last3SeasonsmaxSeqWOFlipFlopScore + 0.05*allSeasonsmaxSeqWOFlipFlopScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.05*totalMatchesScore);

            teamByName.setFlipFlopScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

//    public LinkedHashMap<String, String> getSimulatedScorePartialSeasons(Team teamByName, int seasonsToDiscard) {
//        List<DrawSeasonInfo> statsByTeam = flipFlopOversUndersInfoRepository.getStatsByTeam(teamByName);
//        LinkedHashMap<String, String> outMap = new LinkedHashMap<>();
//        List<String> profits = ImmutableList.of("1","0,6","1,2","1,3","2","2,8","4,3","6,6","-20,4","-23,4","-19,8","-19,2","-15");
//
//        if (statsByTeam.size() <= 2 || statsByTeam.size() < seasonsToDiscard) {
//            outMap.put("footballDrawHunter", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
//            return outMap;
//        }
//        Collections.sort(statsByTeam, new SortStatsDataBySeason());
//        Collections.reverse(statsByTeam);
//        List<DrawSeasonInfo> filteredStats = statsByTeam.subList(seasonsToDiscard, statsByTeam.size());
//
//        if (filteredStats.size() < 3 || !filteredStats.get(0).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-1-seasonsToDiscard)) ||
//                !filteredStats.get(1).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-2-seasonsToDiscard)) ||
//                !filteredStats.get(2).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-3-seasonsToDiscard))) {
//            outMap.put("footballDrawHunter", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
//        } else {
//            int last3SeasonsDrawRateScore = calculateLast3SeasonsDrawRateScore(filteredStats);
//            int allSeasonsDrawRateScore = calculateAllSeasonsOversRateScore(filteredStats);
//            int last3SeasonsmaxSeqWODrawScore = calculateLast3SeasonsmaxSeqWODrawScore(filteredStats);
//            int allSeasonsmaxSeqWODrawScore = calculateAllSeasonsmaxSeqWODrawScore(filteredStats);
//            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(filteredStats);
//            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(filteredStats);
//            int totalMatchesScore = calculateLeagueMatchesScore(filteredStats.get(0).getNumMatches());
//
//            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsDrawRateScore + 0.15*allSeasonsDrawRateScore +
//                    0.15*last3SeasonsmaxSeqWODrawScore + 0.05*allSeasonsmaxSeqWODrawScore +
//                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.05*totalMatchesScore);
//
//            String finalScore = calculateFinalRating(totalScore);
//            outMap.put("footballDrawHunter", finalScore);
//            outMap.put("sequence", statsByTeam.get(seasonsToDiscard-1).getNoDrawsSequence());
//            double balance = 0;
//            String[] seqArray = statsByTeam.get(seasonsToDiscard - 1).getNoDrawsSequence().replaceAll("\\[","").replaceAll("]","").split(",");
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

    private String calculateFinalRating(double score) {
        if (isBetween(score,90,150)) {
            return TeamScoreEnum.EXCELLENT.getValue() + " (" + score + ")";
        } else if(isBetween(score,65,90)) {
            return TeamScoreEnum.ACCEPTABLE.getValue() + " (" + score + ")";
        } else if(isBetween(score,50,65)) {
            return TeamScoreEnum.RISKY.getValue() + " (" + score + ")";
        } else if(isBetween(score,0,50)) {
            return TeamScoreEnum.INAPT.getValue() + " (" + score + ")";
        }
        return "";
    }

    private int calculateLast3SeasonsOversRateScore(List<FlipFlopOversUndersInfo> statsByTeam) {
        double sumOversRates = 0;
        for (int i=0; i<3; i++) {
            sumOversRates += statsByTeam.get(i).getOversRate();
        }

        double avgOversRate = Utils.beautifyDoubleValue(sumOversRates / 3);

        if (isBetween(Math.abs(50 - avgOversRate),0,2)) {
            return 100;
        } else if(isBetween(Math.abs(50 - avgOversRate),2,3)) {
            return 90;
        } else if(isBetween(Math.abs(50 - avgOversRate),3,4)) {
            return 70;
        } else if(isBetween(Math.abs(50 - avgOversRate),4,5)) {
            return 50;
        } else if(isBetween(Math.abs(50 - avgOversRate),5,20)) {
            return 30;
        }
        return 0;
    }

    //the more closest to 50%, the best
    private int calculateAllSeasonsOversRateScore(List<FlipFlopOversUndersInfo> statsByTeam) {
        double sumOversRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumOversRates += statsByTeam.get(i).getOversRate();
        }

        double avgOversRate = Utils.beautifyDoubleValue(sumOversRates / statsByTeam.size());

        if (isBetween(Math.abs(50 - avgOversRate),0,2)) {
            return 100;
        } else if(isBetween(Math.abs(50 - avgOversRate),2,3)) {
            return 90;
        } else if(isBetween(Math.abs(50 - avgOversRate),3,4)) {
            return 70;
        } else if(isBetween(Math.abs(50 - avgOversRate),4,5)) {
            return 50;
        } else if(isBetween(Math.abs(50 - avgOversRate),5,20)) {
            return 30;
        }
        return 0;
    }

    private int calculateRecommendedLevelToStartSequence(List<FlipFlopOversUndersInfo> statsByTeam) {
        int maxValue = 0;
        for (int i = 0; i < 3; i++) {
            String sequenceStr = statsByTeam.get(i).getFlipFlopsSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }
        return maxValue-6 < 0 ? 0 : maxValue-6;
    }

    private int calculateLast3SeasonsmaxSeqWOFlipFlopScore(List<FlipFlopOversUndersInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getFlipFlopsSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,3)) {
            return 100;
        } else if(isBetween(maxValue,3,4)) {
            return 80;
        } else if(isBetween(maxValue,4,5)) {
            return 60;
        }  else if(isBetween(maxValue,5,6)) {
            return 50;
        }  else if(isBetween(maxValue,6,15)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsmaxSeqWOFlipFlopScore(List<FlipFlopOversUndersInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getFlipFlopsSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,3)) {
            return 100;
        } else if(isBetween(maxValue,3,4)) {
            return 80;
        } else if(isBetween(maxValue,4,5)) {
            return 60;
        }  else if(isBetween(maxValue,5,6)) {
            return 50;
        }  else if(isBetween(maxValue,6,15)) {
            return 30;
        }
        return 0;
    }

    private int calculateLast3SeasonsStdDevScore(List<FlipFlopOversUndersInfo> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<3; i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/3);

        if (isBetween(avgStdDev,0,1.6)) {
            return 100;
        } else if(isBetween(avgStdDev,1.6,1.7)) {
            return 90;
        } else if(isBetween(avgStdDev,1.7,1.8)) {
            return 80;
        } else if(isBetween(avgStdDev,1.8,1.9)) {
            return 70;
        } else if(isBetween(avgStdDev,1.9,2)) {
            return 60;
        }  else if(isBetween(avgStdDev,2,2.2)) {
            return 50;
        }  else if(isBetween(avgStdDev,2.2,3)) {
            return 40;
        } else if(isBetween(avgStdDev,3,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsStdDevScore(List<FlipFlopOversUndersInfo> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/statsByTeam.size());

        if (isBetween(avgStdDev,0,1.6)) {
            return 100;
        } else if(isBetween(avgStdDev,1.6,1.7)) {
            return 90;
        } else if(isBetween(avgStdDev,1.7,1.8)) {
            return 80;
        } else if(isBetween(avgStdDev,1.8,1.9)) {
            return 70;
        } else if(isBetween(avgStdDev,1.9,2)) {
            return 60;
        }  else if(isBetween(avgStdDev,2,2.2)) {
            return 50;
        }  else if(isBetween(avgStdDev,2.2,3)) {
            return 40;
        } else if(isBetween(avgStdDev,3,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateLeagueMatchesScore(int totalMatches) {
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

    private static boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x < upper;
    }

    static class SortStatsDataBySeason implements Comparator<FlipFlopOversUndersInfo> {

        @Override
        public int compare(FlipFlopOversUndersInfo a, FlipFlopOversUndersInfo b) {
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
