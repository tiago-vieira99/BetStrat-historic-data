package com.api.BetStrat.service.football;

import com.api.BetStrat.constants.TeamScoreEnum;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.GoalsFestSeasonInfo;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.GoalsFestSeasonInfoRepository;
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
import java.util.LinkedHashMap;
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
public class GoalsFestSeasonInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoalsFestSeasonInfoService.class);

    @Autowired
    private GoalsFestSeasonInfoRepository goalsFestSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    public GoalsFestSeasonInfo insertGoalsFestInfo(GoalsFestSeasonInfo goalsFestSeasonInfo) {
        LOGGER.info("Inserted " + goalsFestSeasonInfo.getClass() + " for " + goalsFestSeasonInfo.getTeamId().getName() + " and season " + goalsFestSeasonInfo.getSeason());
        return goalsFestSeasonInfoRepository.save(goalsFestSeasonInfo);
    }

    public void updateStatsDataInfo(Team team) {
        List<GoalsFestSeasonInfo> statsByTeam = goalsFestSeasonInfoRepository.getGoalsFestStatsByTeam(team);
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

                GoalsFestSeasonInfo goalsFestSeasonInfo = new GoalsFestSeasonInfo();

                ArrayList<Integer> noGoalsFestSequence = new ArrayList<>();
                int count = 0;
                for (HistoricMatch historicMatch : teamMatchesBySeason) {
                    String res = historicMatch.getFtResult().split("\\(")[0];
                    count++;
                    int homeResult = Integer.parseInt(res.split(":")[0]);
                    int awayResult = Integer.parseInt(res.split(":")[1]);
                    if (homeResult > 0 && awayResult > 0 && homeResult+awayResult > 2) {
                        noGoalsFestSequence.add(count);
                        count = 0;
                    }
                }

                int totalGoalsFest = noGoalsFestSequence.size();

                noGoalsFestSequence.add(count);
                HistoricMatch lastMatch = teamMatchesBySeason.get(teamMatchesBySeason.size() - 1);
                String lastResult = lastMatch.getFtResult().split("\\(")[0];
                if (!(Integer.parseInt(lastResult.split(":")[0]) > 0 && Integer.parseInt(lastResult.split(":")[1]) > 0 &&
                        Integer.parseInt(lastResult.split(":")[0]) + Integer.parseInt(lastResult.split(":")[1]) > 2)) {
                    noGoalsFestSequence.add(-1);
                }

                if (totalGoalsFest == 0) {
                    goalsFestSeasonInfo.setGoalsFestRate(0);
                } else {
                    goalsFestSeasonInfo.setGoalsFestRate(Utils.beautifyDoubleValue(100*totalGoalsFest/teamMatchesBySeason.size()));
                }
                goalsFestSeasonInfo.setCompetition("all");
                goalsFestSeasonInfo.setNegativeSequence(noGoalsFestSequence.toString());
                goalsFestSeasonInfo.setNumGoalsFest(totalGoalsFest);
                goalsFestSeasonInfo.setNumMatches(teamMatchesBySeason.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(noGoalsFestSequence));
                goalsFestSeasonInfo.setStdDeviation(stdDev);
                goalsFestSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, noGoalsFestSequence)));
                goalsFestSeasonInfo.setSeason(season);
                goalsFestSeasonInfo.setTeamId(team);
                goalsFestSeasonInfo.setUrl(newSeasonUrl);
                insertGoalsFestInfo(goalsFestSeasonInfo);
            }
        }
    }

    public Team updateTeamScore (Team teamByName) {
        List<GoalsFestSeasonInfo> statsByTeam = goalsFestSeasonInfoRepository.getGoalsFestStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent()) {
            teamByName.setGoalsFestScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsGoalsFestRateScore = calculateLast3SeasonsGoalsFestRateScore(statsByTeam);
            int allSeasonsGoalsFestRateScore = calculateAllSeasonsGoalsFestRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOGoalsFestScore = calculateLast3SeasonsmaxSeqWOGoalsFestScore(statsByTeam);
            int allSeasonsmaxSeqWOGoalsFestScore = calculateAllSeasonsmaxSeqWOGoalsFestScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
//            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsGoalsFestRateScore + 0.1*allSeasonsGoalsFestRateScore +
                    0.2*last3SeasonsmaxSeqWOGoalsFestScore + 0.1*allSeasonsmaxSeqWOGoalsFestScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore);

            teamByName.setGoalsFestScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    public LinkedHashMap<String, String> getSimulatedScorePartialSeasons(Team teamByName, int seasonsToDiscard) {
        List<GoalsFestSeasonInfo> statsByTeam = goalsFestSeasonInfoRepository.getGoalsFestStatsByTeam(teamByName);
        LinkedHashMap<String, String> outMap = new LinkedHashMap<>();

        if (statsByTeam.size() <= 2 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent() || statsByTeam.size() < seasonsToDiscard) {
            outMap.put("footballGoalsFest", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
            return outMap;
        }
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);
        List<GoalsFestSeasonInfo> filteredStats = statsByTeam.subList(seasonsToDiscard, statsByTeam.size());

        if (filteredStats.size() < 3 || !filteredStats.get(0).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-1-seasonsToDiscard)) ||
                !filteredStats.get(1).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-2-seasonsToDiscard)) ||
                !filteredStats.get(2).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-3-seasonsToDiscard))) {
            outMap.put("footballGoalsFest", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsGoalsFestRateScore = calculateLast3SeasonsGoalsFestRateScore(filteredStats);
            int allSeasonsGoalsFestRateScore = calculateAllSeasonsGoalsFestRateScore(filteredStats);
            int last3SeasonsmaxSeqWOGoalsFestScore = calculateLast3SeasonsmaxSeqWOGoalsFestScore(filteredStats);
            int allSeasonsmaxSeqWOGoalsFestScore = calculateAllSeasonsmaxSeqWOGoalsFestScore(filteredStats);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(filteredStats);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(filteredStats);
//            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsGoalsFestRateScore + 0.1*allSeasonsGoalsFestRateScore +
                    0.2*last3SeasonsmaxSeqWOGoalsFestScore + 0.1*allSeasonsmaxSeqWOGoalsFestScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore);

            String finalScore = calculateFinalRating(totalScore);
            outMap.put("footballGoalsFest", finalScore);
            outMap.put("sequence", statsByTeam.get(seasonsToDiscard-1).getNegativeSequence());
            double balance = 0;
            String[] seqArray = statsByTeam.get(seasonsToDiscard - 1).getNegativeSequence().replaceAll("\\[","").replaceAll("]","").split(",");
            for (int i=0; i<seqArray.length-2; i++) {
                int excelBadRun = 0;
                int accepBadRun = 0;
                if (balance < -20) {
                    break;
                }
//                } else if (Integer.parseInt(seqArray[i].trim())-accepBadRun >= 7 && Integer.parseInt(seqArray[i].trim())-accepBadRun < 10) {
//                    balance += -11;
//                } else if (Integer.parseInt(seqArray[i].trim())-accepBadRun >= 10 && Integer.parseInt(seqArray[i].trim())-accepBadRun < 13) {
//                    balance += -17;
//                } else if (Integer.parseInt(seqArray[i].trim())-accepBadRun >= 13 && Integer.parseInt(seqArray[i].trim())-accepBadRun < 16) {
//                    balance += -23;
//                } else if (Integer.parseInt(seqArray[i].trim())-accepBadRun >= 16) {
//                    balance += -29;
                double marginWinsScorePoints = Double.parseDouble(finalScore.substring(finalScore.indexOf('(') + 1, finalScore.indexOf(')')));
                if (finalScore.contains("EXCEL") && Integer.parseInt(seqArray[i].trim()) > excelBadRun) {
                    if (Integer.parseInt(seqArray[i].trim())-excelBadRun > 4) {
                        balance += -10;
                        continue;
                    }
                    balance += 1;
                } else if (finalScore.contains("ACCEPTABLE") &&  Integer.parseInt(seqArray[i].trim()) > accepBadRun) {
                    if (Integer.parseInt(seqArray[i].trim())-accepBadRun > 4) {
                        balance += -10;
                        continue;
                    }
                    balance += 1;
                }
            }
            outMap.put("balance", String.valueOf(balance).replaceAll("\\.",","));
        }
        return outMap;
    }

    private String calculateFinalRating(double score) {
        if (isBetween(score,85,150)) {
            return TeamScoreEnum.EXCELLENT.getValue() + " (" + score + ")";
        } else if(isBetween(score,65,85)) {
            return TeamScoreEnum.ACCEPTABLE.getValue() + " (" + score + ")";
        } else if(isBetween(score,50,65)) {
            return TeamScoreEnum.RISKY.getValue() + " (" + score + ")";
        } else if(isBetween(score,0,50)) {
            return TeamScoreEnum.INAPT.getValue() + " (" + score + ")";
        }
        return "";
    }

    private int calculateLast3SeasonsGoalsFestRateScore(List<GoalsFestSeasonInfo> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<3; i++) {
            GoalsFestRates += statsByTeam.get(i).getGoalsFestRate();
        }

        double avgGoalsFestRate = Utils.beautifyDoubleValue(GoalsFestRates / 3);

        if (isBetween(avgGoalsFestRate,50,100)) {
            return 100;
        } else if(isBetween(avgGoalsFestRate,40,50)) {
            return 90;
        } else if(isBetween(avgGoalsFestRate,35,40)) {
            return 80;
        } else if(isBetween(avgGoalsFestRate,30,35)) {
            return 60;
        } else if(isBetween(avgGoalsFestRate,20,30)) {
            return 50;
        } else if(isBetween(avgGoalsFestRate,0,20)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsGoalsFestRateScore(List<GoalsFestSeasonInfo> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            GoalsFestRates += statsByTeam.get(i).getGoalsFestRate();
        }

        double avgGoalsFestRate = Utils.beautifyDoubleValue(GoalsFestRates / statsByTeam.size());

        if (isBetween(avgGoalsFestRate,50,100)) {
            return 100;
        } else if(isBetween(avgGoalsFestRate,40,50)) {
            return 90;
        } else if(isBetween(avgGoalsFestRate,35,40)) {
            return 80;
        } else if(isBetween(avgGoalsFestRate,30,35)) {
            return 60;
        } else if(isBetween(avgGoalsFestRate,20,30)) {
            return 50;
        } else if(isBetween(avgGoalsFestRate,0,20)) {
            return 30;
        }
        return 0;
    }

    private int calculateLast3SeasonsmaxSeqWOGoalsFestScore(List<GoalsFestSeasonInfo> statsByTeam) {
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
            return 50;
        } else if(isBetween(maxValue,8,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsmaxSeqWOGoalsFestScore(List<GoalsFestSeasonInfo> statsByTeam) {
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
            return 50;
        } else if(isBetween(maxValue,8,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateLast3SeasonsStdDevScore(List<GoalsFestSeasonInfo> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<3; i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/3);

        if (isBetween(avgStdDev,0,1.7)) {
            return 100;
        } else if(isBetween(avgStdDev,1.7,2.0)) {
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

    private int calculateAllSeasonsStdDevScore(List<GoalsFestSeasonInfo> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/statsByTeam.size());

        if (isBetween(avgStdDev,0,1.7)) {
            return 100;
        } else if(isBetween(avgStdDev,1.7,2.0)) {
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

//    private int calculateLeagueMatchesScore(int totalMatches) {
//        if (isBetween(totalMatches,0,31)) {
//            return 100;
//        } else if(isBetween(totalMatches,31,33)) {
//            return 90;
//        } else if(isBetween(totalMatches,33,35)) {
//            return 80;
//        } else if(isBetween(totalMatches,35,41)) {
//            return 60;
//        } else if(isBetween(totalMatches,41,50)) {
//            return 30;
//        }
//        return 0;
//    }

    private static boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x < upper;
    }

    static class SortStatsDataBySeason implements Comparator<GoalsFestSeasonInfo> {

        @Override
        public int compare(GoalsFestSeasonInfo a, GoalsFestSeasonInfo b) {
            return Integer.valueOf(SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(SEASONS_LIST.indexOf(b.getSeason())));
        }
    }

}