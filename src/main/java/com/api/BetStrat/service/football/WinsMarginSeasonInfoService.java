package com.api.BetStrat.service.football;

import com.api.BetStrat.constants.TeamScoreEnum;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinsMarginSeasonInfo;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.WinsMarginSeasonInfoRepository;
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
public class WinsMarginSeasonInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinsMarginSeasonInfoService.class);

    @Autowired
    private WinsMarginSeasonInfoRepository winsMarginSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    public WinsMarginSeasonInfo insertWinsMarginInfo(WinsMarginSeasonInfo winsMarginSeasonInfo) {
        LOGGER.info("Inserted " + winsMarginSeasonInfo.getClass() + " for " + winsMarginSeasonInfo.getTeamId().getName() + " and season " + winsMarginSeasonInfo.getSeason());
        return winsMarginSeasonInfoRepository.save(winsMarginSeasonInfo);
    }

    public void updateStatsDataInfo(Team team) {
        List<WinsMarginSeasonInfo> statsByTeam = winsMarginSeasonInfoRepository.getFootballWinsMarginStatsByTeam(team);
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

                WinsMarginSeasonInfo winsMarginSeasonInfo = new WinsMarginSeasonInfo();

                ArrayList<Integer> noMarginWinsSequence = new ArrayList<>();
                int count = 0;
                int totalWins= 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    String res = historicMatch.getFtResult().split("\\(")[0];
                    count++;
                    int homeResult = Integer.parseInt(res.split(":")[0]);
                    int awayResult = Integer.parseInt(res.split(":")[1]);
                    if ((historicMatch.getHomeTeam().equals(team.getName()) && homeResult>awayResult) || (historicMatch.getAwayTeam().equals(team.getName()) && homeResult<awayResult)) {
                        totalWins++;
                        if (Math.abs(homeResult - awayResult) == 1 || Math.abs(homeResult - awayResult) == 2) {
                            noMarginWinsSequence.add(count);
                            count = 0;
                        }
                    }
                }

                int totalMarginWins = noMarginWinsSequence.size();

                noMarginWinsSequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                String lastResult = lastMatch.getFtResult().split("\\(")[0];
                if (!((lastMatch.getHomeTeam().equals(team.getName()) && Integer.parseInt(lastResult.split(":")[0])>Integer.parseInt(lastResult.split(":")[1])) ||
                        (lastMatch.getAwayTeam().equals(team.getName()) && Integer.parseInt(lastResult.split(":")[0])<Integer.parseInt(lastResult.split(":")[1]))) ||
                        (Math.abs(Integer.parseInt(lastResult.split(":")[0]) - Integer.parseInt(lastResult.split(":")[1])) > 2)) {
                    noMarginWinsSequence.add(-1);
                }

                if (totalWins == 0) {
                    winsMarginSeasonInfo.setMarginWinsRate(0);
                    winsMarginSeasonInfo.setWinsRate(0);
                } else {
                    winsMarginSeasonInfo.setMarginWinsRate(Utils.beautifyDoubleValue(100*totalMarginWins/totalWins));
                    winsMarginSeasonInfo.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/filteredMatches.size()));
                }
                winsMarginSeasonInfo.setCompetition(mainCompetition);
                winsMarginSeasonInfo.setNoMarginWinsSequence(noMarginWinsSequence.toString());
                winsMarginSeasonInfo.setNumMarginWins(totalMarginWins);
                winsMarginSeasonInfo.setNumMatches(filteredMatches.size());
                winsMarginSeasonInfo.setNumWins(totalWins);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(noMarginWinsSequence));
                winsMarginSeasonInfo.setStdDeviation(stdDev);
                winsMarginSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, noMarginWinsSequence)));
                winsMarginSeasonInfo.setSeason(season);
                winsMarginSeasonInfo.setTeamId(team);
                winsMarginSeasonInfo.setUrl(newSeasonUrl);
                insertWinsMarginInfo(winsMarginSeasonInfo);
            }
        }
    }

    public Team updateTeamScore (Team teamByName) {
        List<WinsMarginSeasonInfo> statsByTeam = winsMarginSeasonInfoRepository.getFootballWinsMarginStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setMarginWinsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsMarginWinsRateScore = calculateLast3SeasonsMarginWinsRateScore(statsByTeam);
            int allSeasonsMarginWinsRateScore = calculateAllSeasonsMarginWinsRateScore(statsByTeam);
            int last3SeasonsTotalWinsRateScore = calculateLast3SeasonsTotalWinsRateScore(statsByTeam);
            int allSeasonsTotalWinsRateScore = calculateAllSeasonsTotalWinsRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOMarginWinsScore = calculateLast3SeasonsmaxSeqWOMarginWinsScore(statsByTeam);
            int allSeasonsmaxSeqWOMarginWinsScore = calculateAllSeasonsmaxSeqWOMarginWinsScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double last3SeasonsWinsAvg = (last3SeasonsTotalWinsRateScore + last3SeasonsMarginWinsRateScore) / 2;
            double allSeasonsWinsAvg = (allSeasonsTotalWinsRateScore + allSeasonsMarginWinsRateScore) / 2;

            double last3SeasonsScore = Utils.beautifyDoubleValue(0.3*last3SeasonsWinsAvg + 0.4*last3SeasonsmaxSeqWOMarginWinsScore + 0.3*last3SeasonsStdDevScore);
            double allSeasonsScore = Utils.beautifyDoubleValue(0.3*allSeasonsWinsAvg + 0.4*allSeasonsmaxSeqWOMarginWinsScore + 0.3*allSeasonsStdDevScore);

            double totalScore = Utils.beautifyDoubleValue(0.75*last3SeasonsScore + 0.20*allSeasonsScore + 0.05*totalMatchesScore);

            teamByName.setMarginWinsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    public LinkedHashMap<String, String> getSimulatedScorePartialSeasons(Team teamByName, int seasonsToDiscard) {
        List<WinsMarginSeasonInfo> statsByTeam = winsMarginSeasonInfoRepository.getFootballWinsMarginStatsByTeam(teamByName);
        LinkedHashMap<String, String> outMap = new LinkedHashMap<>();

        if (statsByTeam.size() <= 2 || statsByTeam.size() < seasonsToDiscard) {
            outMap.put("footballMarginWins", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
            return outMap;
        }
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);
        List<WinsMarginSeasonInfo> filteredStats = statsByTeam.subList(seasonsToDiscard, statsByTeam.size());

        if (filteredStats.size() < 3 || !filteredStats.get(0).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-1-seasonsToDiscard)) ||
                !filteredStats.get(1).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-2-seasonsToDiscard)) ||
                !filteredStats.get(2).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-3-seasonsToDiscard))) {
            outMap.put("footballMarginWins", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            String lastComp = filteredStats.get(0).getCompetition().substring(0, filteredStats.get(0).getCompetition().lastIndexOf(' '));
            for (int i=1; i<3; i++) {
                if (!filteredStats.get(i).getCompetition().substring(0, filteredStats.get(i).getCompetition().lastIndexOf(' ')).contains(lastComp)) {
                    outMap.put("footballMarginWins", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
                    return outMap;
                }
            }

            if (filteredStats.stream().filter(st -> st.getWinsRate() >= 50).collect(Collectors.toList()).size() < (0.6* filteredStats.size())) {
                outMap.put("footballMarginWins", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
                return outMap;
            }

            int last3SeasonsMarginWinsRateScore = calculateLast3SeasonsMarginWinsRateScore(filteredStats);
            int allSeasonsMarginWinsRateScore = calculateAllSeasonsMarginWinsRateScore(filteredStats);
            int last3SeasonsTotalWinsRateScore = calculateLast3SeasonsTotalWinsRateScore(filteredStats);
            int allSeasonsTotalWinsRateScore = calculateAllSeasonsTotalWinsRateScore(filteredStats);
            int last3SeasonsmaxSeqWOMarginWinsScore = calculateLast3SeasonsmaxSeqWOMarginWinsScore(filteredStats);
            int allSeasonsmaxSeqWOMarginWinsScore = calculateAllSeasonsmaxSeqWOMarginWinsScore(filteredStats);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(filteredStats);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(filteredStats);
            int totalMatchesScore = calculateLeagueMatchesScore(filteredStats.get(0).getNumMatches());

            double last3SeasonsWinsAvg = (last3SeasonsTotalWinsRateScore + last3SeasonsMarginWinsRateScore) / 2;
            double allSeasonsWinsAvg = (allSeasonsTotalWinsRateScore + allSeasonsMarginWinsRateScore) / 2;

            double last3SeasonsScore = Utils.beautifyDoubleValue(0.3*last3SeasonsWinsAvg + 0.4*last3SeasonsmaxSeqWOMarginWinsScore + 0.3*last3SeasonsStdDevScore);
            double allSeasonsScore = Utils.beautifyDoubleValue(0.3*allSeasonsWinsAvg + 0.4*allSeasonsmaxSeqWOMarginWinsScore + 0.3*allSeasonsStdDevScore);

            double totalScore = Utils.beautifyDoubleValue(0.75*last3SeasonsScore + 0.20*allSeasonsScore + 0.05*totalMatchesScore);

            String finalScore = calculateFinalRating(totalScore);
            outMap.put("footballMarginWins", finalScore);
            outMap.put("sequence", statsByTeam.get(seasonsToDiscard-1).getNoMarginWinsSequence());
            double balance = 0;
            String[] seqArray = statsByTeam.get(seasonsToDiscard - 1).getNoMarginWinsSequence().replaceAll("\\[","").replaceAll("]","").split(",");
            for (int i=0; i<seqArray.length-2; i++) {
                int excelBadRun = 3;
                int accepBadRun = 4;
                if (Integer.parseInt(seqArray[i].trim())-accepBadRun > 4 || Integer.parseInt(seqArray[i].trim())-excelBadRun > 4) {
                    balance += -20;
//                    break;
                }
                double marginWinsScorePoints = Double.parseDouble(finalScore.substring(finalScore.indexOf('(') + 1, finalScore.indexOf(')')));
                if (finalScore.contains("EXCEL") && Integer.parseInt(seqArray[i].trim()) > excelBadRun) {
                    balance += 1.5;
                } else if (finalScore.contains("ACCEPTABLE") &&  Integer.parseInt(seqArray[i].trim()) > accepBadRun) {
                    balance += 1.5;
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

    private int calculateLast3SeasonsMarginWinsRateScore(List<WinsMarginSeasonInfo> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<3; i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgMarginWinsRate = Utils.beautifyDoubleValue(marginWinsRates / 3);

        if (isBetween(avgMarginWinsRate,80,100)) {
            return 100;
        } else if(isBetween(avgMarginWinsRate,70,80)) {
            return 80;
        } else if(isBetween(avgMarginWinsRate,50,70)) {
            return 60;
        } else if(isBetween(avgMarginWinsRate,0,50)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsMarginWinsRateScore(List<WinsMarginSeasonInfo> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgMarginWinsRate = Utils.beautifyDoubleValue(marginWinsRates / statsByTeam.size());

        if (isBetween(avgMarginWinsRate,80,100)) {
            return 100;
        } else if(isBetween(avgMarginWinsRate,70,80)) {
            return 80;
        } else if(isBetween(avgMarginWinsRate,50,70)) {
            return 60;
        } else if(isBetween(avgMarginWinsRate,0,50)) {
            return 30;
        }
        return 0;
    }

    private int calculateLast3SeasonsTotalWinsRateScore(List<WinsMarginSeasonInfo> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<3; i++) {
            totalWinsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / 3);

        if (isBetween(avgWinsRate,80,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,70,80)) {
            return 90;
        } else if(isBetween(avgWinsRate,60,70)) {
            return 80;
        } else if(isBetween(avgWinsRate,50,60)) {
            return 70;
        } else if(isBetween(avgWinsRate,40,50)) {
            return 60;
        } else if(isBetween(avgWinsRate,0,40)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsTotalWinsRateScore(List<WinsMarginSeasonInfo> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            totalWinsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / statsByTeam.size());

        if (isBetween(avgWinsRate,80,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,70,80)) {
            return 90;
        } else if(isBetween(avgWinsRate,60,70)) {
            return 80;
        } else if(isBetween(avgWinsRate,50,60)) {
            return 70;
        } else if(isBetween(avgWinsRate,40,50)) {
            return 60;
        } else if(isBetween(avgWinsRate,0,40)) {
            return 30;
        }
        return 0;
    }

    private int calculateLast3SeasonsmaxSeqWOMarginWinsScore(List<WinsMarginSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getNoMarginWinsSequence().replaceAll("[\\[\\]\\s]", "");
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
            return 70;
        } else if(isBetween(maxValue,9,10)) {
            return 50;
        } else if(isBetween(maxValue,10,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsmaxSeqWOMarginWinsScore(List<WinsMarginSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getNoMarginWinsSequence().replaceAll("[\\[\\]\\s]", "");
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
            return 70;
        } else if(isBetween(maxValue,9,10)) {
            return 50;
        } else if(isBetween(maxValue,10,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateLast3SeasonsStdDevScore(List<WinsMarginSeasonInfo> statsByTeam) {
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

    private int calculateAllSeasonsStdDevScore(List<WinsMarginSeasonInfo> statsByTeam) {
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

    static class SortStatsDataBySeason implements Comparator<WinsMarginSeasonInfo> {

        @Override
        public int compare(WinsMarginSeasonInfo a, WinsMarginSeasonInfo b) {
            return Integer.valueOf(SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(SEASONS_LIST.indexOf(b.getSeason())));
        }
    }

}