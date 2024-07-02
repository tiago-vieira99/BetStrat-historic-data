package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.FlipFlopOversUndersStats;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.FlipFlopOversUndersInfoRepository;
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
public class FlipFlopOversUndersStatsServiceStrategy extends StrategyScoreCalculator<FlipFlopOversUndersStats> implements StrategySeasonStatsInterface<FlipFlopOversUndersStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlipFlopOversUndersStatsServiceStrategy.class);

    @Autowired
    private FlipFlopOversUndersInfoRepository flipFlopOversUndersInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public FlipFlopOversUndersStats insertStrategySeasonStats(FlipFlopOversUndersStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return flipFlopOversUndersInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<FlipFlopOversUndersStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return flipFlopOversUndersInfoRepository.getFlipFlopStatsByTeam(team);
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
        List<FlipFlopOversUndersStats> statsByTeam = flipFlopOversUndersInfoRepository.getFlipFlopStatsByTeam(team);
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
                teamMatchesBySeason.sort(HistoricMatch.matchDateComparator);

                if (teamMatchesBySeason.size() == 0) {
                    continue;
                }

                FlipFlopOversUndersStats flipFlopOversUndersInfo = new FlipFlopOversUndersStats();

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
                flipFlopOversUndersInfo.setNegativeSequence(flipFlopsSequence.toString());
                flipFlopOversUndersInfo.setNumMatches(teamMatchesBySeason.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(flipFlopsSequence));
                flipFlopOversUndersInfo.setStdDeviation(stdDev);
                flipFlopOversUndersInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, flipFlopsSequence)));
                flipFlopOversUndersInfo.setSeason(season);
                flipFlopOversUndersInfo.setTeamId(team);
                flipFlopOversUndersInfo.setUrl(newSeasonUrl);
                insertStrategySeasonStats(flipFlopOversUndersInfo);
            }
        }
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<FlipFlopOversUndersStats> statsByTeam = flipFlopOversUndersInfoRepository.getFlipFlopStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setDrawsHunterScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsOversRateScore = calculateLast3SeasonsRateScore(statsByTeam);
            int allSeasonsOversRateScore = calculateAllSeasonsRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOFlipFlopScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
            int allSeasonsmaxSeqWOFlipFlopScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
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

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategyName) {
        return "";
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<FlipFlopOversUndersStats> statsByTeam) {
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
    @Override
    public int calculateAllSeasonsRateScore(List<FlipFlopOversUndersStats> statsByTeam) {
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

    @Override
    public int calculateLast3SeasonsTotalWinsRateScore(List<FlipFlopOversUndersStats> statsByTeam) {
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<FlipFlopOversUndersStats> statsByTeam) {
        return 0;
    }

    private int calculateRecommendedLevelToStartSequence(List<FlipFlopOversUndersStats> statsByTeam) {
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
