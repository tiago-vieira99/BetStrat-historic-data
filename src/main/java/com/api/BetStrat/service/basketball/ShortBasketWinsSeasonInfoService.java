package com.api.BetStrat.service.basketball;

import com.api.BetStrat.constants.TeamScoreEnum;
import com.api.BetStrat.entity.basketball.ShortBasketWinsSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.repository.basketball.ShortWinsSeasonInfoRepository;
import com.api.BetStrat.util.Utils;
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

import static com.api.BetStrat.constants.BetStratConstants.FOOTBALL_SEASONS_LIST;

@Service
@Transactional
public class ShortBasketWinsSeasonInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortBasketWinsSeasonInfoService.class);

    @Autowired
    private ShortWinsSeasonInfoRepository shortWinsSeasonInfoRepository;

    public ShortBasketWinsSeasonInfo insertShortWinsInfo(ShortBasketWinsSeasonInfo ShortBasketWinsSeasonInfo) {
        return shortWinsSeasonInfoRepository.save(ShortBasketWinsSeasonInfo);
    }

    public Team updateTeamScore (Team teamByName) {
        List<ShortBasketWinsSeasonInfo> statsByTeam = shortWinsSeasonInfoRepository.getStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setBasketShortWinsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsDrawRateScore = calculateLast3SeasonsShortWinsRateScore(statsByTeam);
            int allSeasonsDrawRateScore = calculateAllSeasonsShortWinsRateScore(statsByTeam);
            int last3SeasonsmaxSeqWODrawScore = calculateLast3SeasonsmaxSeqWODrawScore(statsByTeam);
            int allSeasonsmaxSeqWODrawScore = calculateAllSeasonsmaxSeqWODrawScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsDrawRateScore + 0.15*allSeasonsDrawRateScore +
                    0.15*last3SeasonsmaxSeqWODrawScore + 0.05*allSeasonsmaxSeqWODrawScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.05*totalMatchesScore);

            teamByName.setBasketShortWinsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

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

    private int calculateLast3SeasonsShortWinsRateScore(List<ShortBasketWinsSeasonInfo> statsByTeam) {
        double sumDrawRates = 0;
        for (int i=0; i<3; i++) {
            sumDrawRates += statsByTeam.get(i).getNumShortWins();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(sumDrawRates / 3);

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

    private int calculateAllSeasonsShortWinsRateScore(List<ShortBasketWinsSeasonInfo> statsByTeam) {
        double sumDrawRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumDrawRates += statsByTeam.get(i).getShortWinsRate();
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

    private int calculateRecommendedLevelToStartSequence(List<ShortBasketWinsSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i = 0; i < 3; i++) {
            String sequenceStr = statsByTeam.get(i).getNoShortWinsSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }
        return maxValue-6 < 0 ? 0 : maxValue-6;
    }

    private int calculateLast3SeasonsmaxSeqWODrawScore(List<ShortBasketWinsSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getNoShortWinsSequence().replaceAll("[\\[\\]\\s]", "");
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

    private int calculateAllSeasonsmaxSeqWODrawScore(List<ShortBasketWinsSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getNoShortWinsSequence().replaceAll("[\\[\\]\\s]", "");
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

    private int calculateLast3SeasonsStdDevScore(List<ShortBasketWinsSeasonInfo> statsByTeam) {
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

    private int calculateAllSeasonsStdDevScore(List<ShortBasketWinsSeasonInfo> statsByTeam) {
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

    static class SortStatsDataBySeason implements Comparator<ShortBasketWinsSeasonInfo> {

        @Override
        public int compare(ShortBasketWinsSeasonInfo a, ShortBasketWinsSeasonInfo b) {
            return Integer.valueOf(FOOTBALL_SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(FOOTBALL_SEASONS_LIST.indexOf(b.getSeason())));
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