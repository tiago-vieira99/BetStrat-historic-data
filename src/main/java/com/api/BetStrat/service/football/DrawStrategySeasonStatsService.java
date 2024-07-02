package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.football.DrawSeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.DrawSeasonInfoRepository;
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
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.DEFAULT_BAD_RUN_TO_NEW_SEQ;
import static com.api.BetStrat.constants.BetStratConstants.SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_LIST;
import static com.api.BetStrat.util.Utils.calculateCoeffVariation;
import static com.api.BetStrat.util.Utils.calculateSD;

@Service
@Transactional
public class DrawStrategySeasonStatsService extends StrategyScoreCalculator<DrawSeasonStats> implements StrategySeasonStatsInterface<DrawSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrawStrategySeasonStatsService.class);

    @Autowired
    private DrawSeasonInfoRepository drawSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public DrawSeasonStats insertStrategySeasonStats(DrawSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return drawSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<DrawSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return drawSeasonInfoRepository.getFootballDrawStatsByTeam(team);
    }

    @Override
    public List<SimulatedMatchDto> simulateStrategyBySeason(String season, Team team, String strategyName) {
        List<SimulatedMatchDto> matchesBetted = new ArrayList<>();
        List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
        String mainCompetition = Utils.findMainCompetition(teamMatchesBySeason);
        List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(t -> t.getCompetition().equals(mainCompetition)).collect(Collectors.toList());
        Collections.sort(filteredMatches, HistoricMatch.matchDateComparator);

        if (filteredMatches.size() == 0) {
            return matchesBetted;
        }

        boolean isActiveSequence = true;
        int actualNegativeSequence = 0;
        for (int i = 0; i < filteredMatches.size(); i++) {
            HistoricMatch historicMatch = filteredMatches.get(i);
            if (actualNegativeSequence >= DEFAULT_BAD_RUN_TO_NEW_SEQ) {
                isActiveSequence = true;
            }

            if (isActiveSequence) {
                SimulatedMatchDto simulatedMatchDto = new SimulatedMatchDto();
                simulatedMatchDto.setMatchDate(historicMatch.getMatchDate());
                simulatedMatchDto.setHomeTeam(historicMatch.getHomeTeam());
                simulatedMatchDto.setAwayTeam(historicMatch.getAwayTeam());
                simulatedMatchDto.setMatchNumber(String.valueOf(i+1));
                simulatedMatchDto.setHtResult(historicMatch.getHtResult());
                simulatedMatchDto.setFtResult(historicMatch.getFtResult());
                simulatedMatchDto.setSeason(season);
                simulatedMatchDto.setCompetition(historicMatch.getCompetition());
                if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                    simulatedMatchDto.setIsGreen(true);
                    actualNegativeSequence = 0;
                    isActiveSequence = false;
                } else {
                    simulatedMatchDto.setIsGreen(false);
                }
                matchesBetted.add(simulatedMatchDto);
            } else {
                if (!matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                    actualNegativeSequence++;
                } else {
                    actualNegativeSequence = 0;
                }
            }
        }

        return matchesBetted;
    }

    @Override
    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategyName) {
        String res = historicMatch.getFtResult().split("\\(")[0];
        int homeResult = Integer.parseInt(res.split(":")[0]);
        int awayResult = Integer.parseInt(res.split(":")[1]);
        if (homeResult == awayResult) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<DrawSeasonStats> statsByTeam = drawSeasonInfoRepository.getFootballDrawStatsByTeam(team);
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
                filteredMatches.sort(HistoricMatch.matchDateComparator);

                if (filteredMatches.size() == 0) {
                    continue;
                }

                DrawSeasonStats drawSeasonInfo = new DrawSeasonStats();

                ArrayList<Integer> noDrawsSequence = new ArrayList<>();
                int count = 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    count++;
                    if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                        noDrawsSequence.add(count);
                        count = 0;
                    }
                }

                int totalDraws = noDrawsSequence.size();

                noDrawsSequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    noDrawsSequence.add(-1);
                }

                if (totalDraws == 0) {
                    drawSeasonInfo.setDrawRate(0);
                } else {
                    drawSeasonInfo.setDrawRate(Utils.beautifyDoubleValue(100*totalDraws/filteredMatches.size()));
                }
                drawSeasonInfo.setCompetition(mainCompetition);
                drawSeasonInfo.setNegativeSequence(noDrawsSequence.toString());
                drawSeasonInfo.setNumDraws(totalDraws);
                drawSeasonInfo.setNumMatches(filteredMatches.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(noDrawsSequence));
                drawSeasonInfo.setStdDeviation(stdDev);
                drawSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, noDrawsSequence)));
                drawSeasonInfo.setSeason(season);
                drawSeasonInfo.setTeamId(team);
                drawSeasonInfo.setUrl(newSeasonUrl);
                insertStrategySeasonStats(drawSeasonInfo);
            }
        }
    }

    @Override
    public Team updateTeamScore (Team team) {
        List<DrawSeasonStats> statsByTeam = drawSeasonInfoRepository.getFootballDrawStatsByTeam(team);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            team.setDrawsHunterScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            team.setDrawsHunterScore(calculateFinalRating(totalScore));
        }

        return team;
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<DrawSeasonStats> statsByTeam = drawSeasonInfoRepository.getFootballDrawStatsByTeam(team);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        int indexOfSeason = WINTER_SEASONS_LIST.indexOf(season);
        statsByTeam = statsByTeam.stream().filter(s -> WINTER_SEASONS_LIST.indexOf(s.getSeason()) < indexOfSeason).collect(Collectors.toList());

        if (statsByTeam.size() < 3) {
            return TeamScoreEnum.INSUFFICIENT_DATA.getValue();
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            return calculateFinalRating(totalScore);
        }
    }

    private double calculateTotalFinalScore(List<DrawSeasonStats> statsByTeam) {
        int last3SeasonsDrawRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsDrawRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWODrawScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWODrawScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.2*last3SeasonsDrawRateScore + 0.1*allSeasonsDrawRateScore +
            0.18*last3SeasonsmaxSeqWODrawScore + 0.1*allSeasonsmaxSeqWODrawScore +
            0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<DrawSeasonStats> statsByTeam) {
        int result = 0;
        double sumDrawRates = 0;
        for (int i = 0; i < 3; i++) {
            sumDrawRates += statsByTeam.get(i).getDrawRate();
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
    public int calculateAllSeasonsRateScore(List<DrawSeasonStats> statsByTeam) {
        double sumDrawRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumDrawRates += statsByTeam.get(i).getDrawRate();
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
    public int calculateLast3SeasonsTotalWinsRateScore(List<DrawSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<DrawSeasonStats> statsByTeam) {
        return 0;
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
