package com.api.BetStrat.service.basketball;

import com.api.BetStrat.constants.TeamScoreEnum;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.basketball.ComebackSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.basketball.ComebackSeasonInfoRepository;
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
public class ComebackSeasonInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComebackSeasonInfoService.class);

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Autowired
    private ComebackSeasonInfoRepository comebackSeasonInfoRepository;

    public ComebackSeasonInfo insertComebackInfo(ComebackSeasonInfo ComebackSeasonInfo) {
        return comebackSeasonInfoRepository.save(ComebackSeasonInfo);
    }

    public void updateStatsDataInfo(Team team) {
        List<ComebackSeasonInfo> statsByTeam = comebackSeasonInfoRepository.getComebackStatsByTeam(team);
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

                ComebackSeasonInfo comebackSeasonInfo = new ComebackSeasonInfo();
                LOGGER.info("Insert " + comebackSeasonInfo.getClass() + " for " + team.getName() + " and season " + season);
                ArrayList<Integer> noComebacksSequence = new ArrayList<>();
                int count = 0;
                int totalWins= 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    String ftResult = historicMatch.getFtResult().split(" ")[0];
                    String htResult = historicMatch.getHtResult();
                    count++;
                    int homeFTResult = Integer.parseInt(ftResult.split(":")[0]);
                    int awayFTResult = Integer.parseInt(ftResult.split(":")[1]);
                    int homeHTResult = Integer.parseInt(htResult.split(":")[0]);
                    int awayHTResult = Integer.parseInt(htResult.split(":")[1]);

                    if (historicMatch.getHomeTeam().equals(team.getName()) && homeFTResult > awayFTResult) {
                        totalWins++;
                        if (awayHTResult > homeHTResult) {
                            noComebacksSequence.add(count);
                            count = 0;
                        }
                    } else if (historicMatch.getAwayTeam().equals(team.getName()) && homeFTResult < awayFTResult) {
                        totalWins++;
                        if (awayHTResult < homeHTResult) {
                            noComebacksSequence.add(count);
                            count = 0;
                        }
                    }
                }

                int totalComebacks = noComebacksSequence.size();

                noComebacksSequence.add(count);
                if (noComebacksSequence.get(noComebacksSequence.size()-1) != 0) {
                    noComebacksSequence.add(-1);
                }

                comebackSeasonInfo.setCompetition(mainCompetition);
                if (totalWins == 0) {
                    comebackSeasonInfo.setComebacksRate(0);
                    comebackSeasonInfo.setWinsRate(0);
                } else {
                    comebackSeasonInfo.setComebacksRate(Utils.beautifyDoubleValue(100 * totalComebacks / totalWins));
                    comebackSeasonInfo.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/ filteredMatches.size()));
                }
                comebackSeasonInfo.setNoComebacksSequence(noComebacksSequence.toString());
                comebackSeasonInfo.setNumComebacks(totalComebacks);
                comebackSeasonInfo.setNumMatches(filteredMatches.size());
                comebackSeasonInfo.setNumWins(totalWins);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(noComebacksSequence));
                comebackSeasonInfo.setStdDeviation(stdDev);
                comebackSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, noComebacksSequence)));

                comebackSeasonInfo.setSeason(season);
                comebackSeasonInfo.setTeamId(team);
                comebackSeasonInfo.setUrl(team.getUrl());
                insertComebackInfo(comebackSeasonInfo);
            }
        }
    }

    public Team updateTeamScore (Team teamByName) {
        List<ComebackSeasonInfo> statsByTeam = comebackSeasonInfoRepository.getComebackStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setBasketComebackScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsComebackRateScore = calculateLast3SeasonsComebackRateScore(statsByTeam);
            int allSeasonsComebackRateScore = calculateAllSeasonsComebackRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOComebackScore = calculateLast3SeasonsmaxSeqWOComebackScore(statsByTeam);
            int allSeasonsmaxSeqWOComebackScore = calculateAllSeasonsmaxSeqWOComebackScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsComebackRateScore + 0.15*allSeasonsComebackRateScore +
                    0.15*last3SeasonsmaxSeqWOComebackScore + 0.05*allSeasonsmaxSeqWOComebackScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.05*totalMatchesScore);

            teamByName.setBasketComebackScore(calculateFinalRating(totalScore));
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

    private int calculateLast3SeasonsComebackRateScore(List<ComebackSeasonInfo> statsByTeam) {
        double sumComebackRates = 0;
        for (int i=0; i<3; i++) {
            sumComebackRates += statsByTeam.get(i).getComebacksRate();
        }

        double avgComebackRate = Utils.beautifyDoubleValue(sumComebackRates / 3);

        if (isBetween(avgComebackRate,35,100)) {
            return 100;
        } else if(isBetween(avgComebackRate,30,35)) {
            return 90;
        } else if(isBetween(avgComebackRate,27,30)) {
            return 80;
        } else if(isBetween(avgComebackRate,25,27)) {
            return 60;
        } else if(isBetween(avgComebackRate,20,25)) {
            return 50;
        } else if(isBetween(avgComebackRate,0,20)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsComebackRateScore(List<ComebackSeasonInfo> statsByTeam) {
        double sumComebackRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumComebackRates += statsByTeam.get(i).getComebacksRate();
        }

        double avgComebackRate = Utils.beautifyDoubleValue(sumComebackRates / statsByTeam.size());

        if (isBetween(avgComebackRate,35,100)) {
            return 100;
        } else if(isBetween(avgComebackRate,30,35)) {
            return 90;
        } else if(isBetween(avgComebackRate,27,30)) {
            return 80;
        } else if(isBetween(avgComebackRate,25,27)) {
            return 60;
        } else if(isBetween(avgComebackRate,20,25)) {
            return 50;
        } else if(isBetween(avgComebackRate,0,20)) {
            return 30;
        }
        return 0;
    }

    private int calculateRecommendedLevelToStartSequence(List<ComebackSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i = 0; i < 3; i++) {
            String sequenceStr = statsByTeam.get(i).getNoComebacksSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }
        return maxValue-6 < 0 ? 0 : maxValue-6;
    }

    private int calculateLast3SeasonsmaxSeqWOComebackScore(List<ComebackSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getNoComebacksSequence().replaceAll("[\\[\\]\\s]", "");
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

    private int calculateAllSeasonsmaxSeqWOComebackScore(List<ComebackSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getNoComebacksSequence().replaceAll("[\\[\\]\\s]", "");
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

    private int calculateLast3SeasonsStdDevScore(List<ComebackSeasonInfo> statsByTeam) {
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

    private int calculateAllSeasonsStdDevScore(List<ComebackSeasonInfo> statsByTeam) {
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

    static class SortStatsDataBySeason implements Comparator<ComebackSeasonInfo> {

        @Override
        public int compare(ComebackSeasonInfo a, ComebackSeasonInfo b) {
            return Integer.valueOf(SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(SEASONS_LIST.indexOf(b.getSeason())));
        }
    }

    /* avaliar cada parametro independentemente:
     *
     * 1) dar peso a cada parametro:
     *   ComebackRate (last3) - 25
     *   ComebackRate (total) - 20
     *   maxSeqWOComeback (last3) - 15
     *   maxSeqWOComeback (total) - 5
     *   stdDev (last3) - 20
     *   stdDev (total) - 10
     *   numTotalMatches - 5
     *
     *
     *   ComebackRate -> (100 se > 35) ; (90 se < 35) ; (80 entre 27 e 30) ; (60 entre 25 e 27) ; (50 entre 20 e 25) ; (30 se < 20)
     *   maxSeqWOComeback -> (100 se < 7) ; (90 se == 7) ; (80 se == 8) ; (70 se == 9) ; (60 se == 10 ou 11) ; (50 se == 12 ou 13) ; (40 se == 14) ; (30 se > 14)
     *   stdDev -> (100 se < 2.3) ; (90 se < 2.4) ; (80 se < 2.5) ; (70 se < 2.6) ; (60 se < 2.7) ; (50 se < 2.8) ; (40 se < 2.9) ; (30 se > 3)
     *   numTotalMatches -> (100 se < 30) ; (90 se < 32) ; (80 se < 34) ; (50 se < 40) ; (30 se > 40)
     *
     *
     * excellent: avg std dev < 2.1 && avg ComebackRate > 30 && list.size > 3 && maxSeqValue < 9
     * acceptable: ((avg std dev > 2.1 & < 2.5 ; min ComebackRate > 23) || avg ComebackRate > 32) && maxSeqValue <= 10
     * risky: (max std dev > 3 && min ComebackRate < 20) || maxSeqValue > 15
     *
     * */

}
