package com.api.BetStrat.service.handball;

import com.api.BetStrat.constants.TeamScoreEnum;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.handball.Handball712WinsMarginSeasonInfo;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.handball.Handball712WinsMarginSeasonInfoRepository;
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
public class HandballWinsMargin712SeasonInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandballWinsMargin712SeasonInfoService.class);

    @Autowired
    private Handball712WinsMarginSeasonInfoRepository handball712WinsMarginSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    public Handball712WinsMarginSeasonInfo insert712WinsMarginInfo(Handball712WinsMarginSeasonInfo winsMarginSeasonInfo) {
        LOGGER.info("Inserted " + winsMarginSeasonInfo.getClass() + " for " + winsMarginSeasonInfo.getTeamId().getName() + " and season " + winsMarginSeasonInfo.getSeason());
        return handball712WinsMarginSeasonInfoRepository.save(winsMarginSeasonInfo);
    }

    public void updateStatsDataInfo(Team team) {
        List<Handball712WinsMarginSeasonInfo> statsByTeam = handball712WinsMarginSeasonInfoRepository.getStatsByTeam(team);
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

                Handball712WinsMarginSeasonInfo handball712WinsMarginSeasonInfo = new Handball712WinsMarginSeasonInfo();

                ArrayList<Integer> noMarginWinsSequence = new ArrayList<>();
                int count = 0;
                int totalWins= 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    String res = historicMatch.getFtResult();
                    count++;
                    int homeResult = Integer.parseInt(res.split(":")[0]);
                    int awayResult = 0;
                    if (res.contains("(")){
                        awayResult = homeResult;
                    } else {
                        awayResult = Integer.parseInt(res.split(":")[1]);
                    }

                    if ((historicMatch.getHomeTeam().equals(team.getName()) && homeResult > awayResult) || (historicMatch.getAwayTeam().equals(team.getName()) && homeResult < awayResult)) {
                        totalWins++;
                        if (Math.abs(homeResult - awayResult) <= 12 && Math.abs(homeResult - awayResult) >= 7) {
                            noMarginWinsSequence.add(count);
                            count = 0;
                        }
                    }
                }

                int totalMarginWins = noMarginWinsSequence.size();

                noMarginWinsSequence.add(count);
                if (noMarginWinsSequence.get(noMarginWinsSequence.size()-1) != 0) {
                    noMarginWinsSequence.add(-1);
                }

                handball712WinsMarginSeasonInfo.setCompetition(mainCompetition);
                handball712WinsMarginSeasonInfo.setMarginWinsRate(Utils.beautifyDoubleValue(100*totalMarginWins/totalWins));
                handball712WinsMarginSeasonInfo.setNoMarginWinsSequence(noMarginWinsSequence.toString());
                handball712WinsMarginSeasonInfo.setNumMarginWins(totalMarginWins);
                handball712WinsMarginSeasonInfo.setNumMatches(filteredMatches.size());
                handball712WinsMarginSeasonInfo.setNumWins(totalWins);
                handball712WinsMarginSeasonInfo.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/ filteredMatches.size()));

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(noMarginWinsSequence));
                handball712WinsMarginSeasonInfo.setStdDeviation(stdDev);
                handball712WinsMarginSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, noMarginWinsSequence)));

                handball712WinsMarginSeasonInfo.setSeason(season);
                handball712WinsMarginSeasonInfo.setTeamId(team);
                handball712WinsMarginSeasonInfo.setUrl(team.getUrl());
//                insert712WinsMarginInfo(handball712WinsMarginSeasonInfo);
                System.out.println();

            }
        }
    }

    public Team updateTeamScore (Team teamByName) {
        List<Handball712WinsMarginSeasonInfo> statsByTeam = handball712WinsMarginSeasonInfoRepository.getStatsByTeam(teamByName);
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

    private int calculateLast3SeasonsMarginWinsRateScore(List<Handball712WinsMarginSeasonInfo> statsByTeam) {
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

    private int calculateAllSeasonsMarginWinsRateScore(List<Handball712WinsMarginSeasonInfo> statsByTeam) {
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

    private int calculateLast3SeasonsTotalWinsRateScore(List<Handball712WinsMarginSeasonInfo> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<3; i++) {
            totalWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(totalWinsRates / 3);

        if (isBetween(avgDrawRate,80,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,70,80)) {
            return 90;
        } else if(isBetween(avgDrawRate,60,70)) {
            return 80;
        } else if(isBetween(avgDrawRate,50,60)) {
            return 70;
        } else if(isBetween(avgDrawRate,40,50)) {
            return 60;
        } else if(isBetween(avgDrawRate,0,40)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsTotalWinsRateScore(List<Handball712WinsMarginSeasonInfo> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            totalWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(totalWinsRates / statsByTeam.size());

        if (isBetween(avgDrawRate,80,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,70,80)) {
            return 90;
        } else if(isBetween(avgDrawRate,60,70)) {
            return 80;
        } else if(isBetween(avgDrawRate,50,60)) {
            return 70;
        } else if(isBetween(avgDrawRate,40,50)) {
            return 60;
        } else if(isBetween(avgDrawRate,0,40)) {
            return 30;
        }
        return 0;
    }

    private int calculateLast3SeasonsmaxSeqWOMarginWinsScore(List<Handball712WinsMarginSeasonInfo> statsByTeam) {
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

    private int calculateAllSeasonsmaxSeqWOMarginWinsScore(List<Handball712WinsMarginSeasonInfo> statsByTeam) {
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

    private int calculateLast3SeasonsStdDevScore(List<Handball712WinsMarginSeasonInfo> statsByTeam) {
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

    private int calculateAllSeasonsStdDevScore(List<Handball712WinsMarginSeasonInfo> statsByTeam) {
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

    static class SortStatsDataBySeason implements Comparator<Handball712WinsMarginSeasonInfo> {

        @Override
        public int compare(Handball712WinsMarginSeasonInfo a, Handball712WinsMarginSeasonInfo b) {
            return Integer.valueOf(SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(SEASONS_LIST.indexOf(b.getSeason())));
        }
    }

}