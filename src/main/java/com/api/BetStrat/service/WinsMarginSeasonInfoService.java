package com.api.BetStrat.service;

import com.api.BetStrat.constants.TeamScoreEnum;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.WinsMarginSeasonInfo;
import com.api.BetStrat.repository.WinsMarginSeasonInfoRepository;
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

import static com.api.BetStrat.constants.BetStratConstants.SEASONS_LIST;

@Service
@Transactional
public class WinsMarginSeasonInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinsMarginSeasonInfoService.class);

    @Autowired
    private WinsMarginSeasonInfoRepository winsMarginSeasonInfoRepository;

    public WinsMarginSeasonInfo insertWinsMarginInfo(WinsMarginSeasonInfo winsMarginSeasonInfo) {
        return winsMarginSeasonInfoRepository.save(winsMarginSeasonInfo);
    }

    public Team updateTeamScore (Team teamByName) {
        List<WinsMarginSeasonInfo> statsByTeam = winsMarginSeasonInfoRepository.getStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setMarginWinsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsMarginWinsRateScore = calculateLast3SeasonsMarginWinsRateScore(statsByTeam);
            int allSeasonsMarginWinsRateScore = calculateAllSeasonsMarginWinsRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOMarginWinsScore = calculateLast3SeasonsmaxSeqWOMarginWinsScore(statsByTeam);
            int allSeasonsmaxSeqWOMarginWinsScore = calculateAllSeasonsmaxSeqWOMarginWinsScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsMarginWinsRateScore + 0.15*allSeasonsMarginWinsRateScore +
                    0.15*last3SeasonsmaxSeqWOMarginWinsScore + 0.05*allSeasonsmaxSeqWOMarginWinsScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.05*totalMatchesScore);

            teamByName.setMarginWinsScore(calculateFinalRating(totalScore));
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

    private int calculateLast3SeasonsMarginWinsRateScore(List<WinsMarginSeasonInfo> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(marginWinsRates / statsByTeam.size());

        if (isBetween(avgDrawRate,90,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,85,90)) {
            return 90;
        } else if(isBetween(avgDrawRate,80,85)) {
            return 80;
        } else if(isBetween(avgDrawRate,70,80)) {
            return 60;
        } else if(isBetween(avgDrawRate,60,70)) {
            return 50;
        } else if(isBetween(avgDrawRate,0,60)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsMarginWinsRateScore(List<WinsMarginSeasonInfo> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(marginWinsRates / statsByTeam.size());

        if (isBetween(avgDrawRate,90,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,85,90)) {
            return 90;
        } else if(isBetween(avgDrawRate,80,85)) {
            return 80;
        } else if(isBetween(avgDrawRate,70,80)) {
            return 60;
        } else if(isBetween(avgDrawRate,60,70)) {
            return 50;
        } else if(isBetween(avgDrawRate,0,60)) {
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

        if (isBetween(maxValue,0,5)) {
            return 100;
        } else if(isBetween(maxValue,5,6)) {
            return 80;
        } else if(isBetween(maxValue,6,7)) {
            return 60;
        }  else if(isBetween(maxValue,7,25)) {
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

        if (isBetween(maxValue,0,5)) {
            return 100;
        } else if(isBetween(maxValue,5,6)) {
            return 80;
        } else if(isBetween(maxValue,6,7)) {
            return 60;
        }  else if(isBetween(maxValue,7,25)) {
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

        if (isBetween(avgStdDev,0,2.0)) {
            return 100;
        } else if(isBetween(avgStdDev,2.0,2.1)) {
            return 90;
        } else if(isBetween(avgStdDev,2.1,2.2)) {
            return 70;
        } else if(isBetween(avgStdDev,2.2,2.3)) {
            return 60;
        } else if(isBetween(avgStdDev,2.3,2.4)) {
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

        if (isBetween(avgStdDev,0,2.0)) {
            return 100;
        } else if(isBetween(avgStdDev,2.0,2.1)) {
            return 90;
        } else if(isBetween(avgStdDev,2.1,2.2)) {
            return 70;
        } else if(isBetween(avgStdDev,2.2,2.3)) {
            return 60;
        } else if(isBetween(avgStdDev,2.3,2.4)) {
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