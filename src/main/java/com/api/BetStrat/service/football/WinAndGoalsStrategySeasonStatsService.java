package com.api.BetStrat.service.football;

import static com.api.BetStrat.constants.BetStratConstants.DEFAULT_BAD_RUN_TO_NEW_SEQ;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_LIST;
import static com.api.BetStrat.util.Utils.calculateCoeffVariation;
import static com.api.BetStrat.util.Utils.calculateSD;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinAndGoalsSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.WinAndGoalsSeasonInfoRepository;
import com.api.BetStrat.service.StrategyScoreCalculator;
import com.api.BetStrat.service.StrategySeasonStatsInterface;
import com.api.BetStrat.util.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WinAndGoalsStrategySeasonStatsService extends StrategyScoreCalculator<WinAndGoalsSeasonStats> implements StrategySeasonStatsInterface<WinAndGoalsSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinAndGoalsStrategySeasonStatsService.class);

    @Autowired
    private WinAndGoalsSeasonInfoRepository winAndGoalsSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public WinAndGoalsSeasonStats insertStrategySeasonStats(WinAndGoalsSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return winAndGoalsSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<WinAndGoalsSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return winAndGoalsSeasonInfoRepository.getFootballWinAndGoalsStatsByTeam(team);
    }

    @Override
    public HashMap<String, Object> getSimulatedMatchesByStrategyAndSeason(String season, Team team, String strategyName) {
//        List<SimulatedMatchDto> matchesBetted = new ArrayList<>();
//        List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
//        String mainCompetition = Utils.findMainCompetition(teamMatchesBySeason);
//        List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(t -> t.getCompetition().equals(mainCompetition)).collect(Collectors.toList());
//        Collections.sort(filteredMatches, HistoricMatch.matchDateComparator);
//
//        if (filteredMatches.size() == 0) {
//            return matchesBetted;
//        }
//
//        boolean isActiveSequence = true;
//        int actualNegativeSequence = 0;
//        for (int i = 0; i < filteredMatches.size(); i++) {
//            HistoricMatch historicMatch = filteredMatches.get(i);
//            if (actualNegativeSequence >= DEFAULT_BAD_RUN_TO_NEW_SEQ) {
//                isActiveSequence = true;
//            }
//
//            if (isActiveSequence) {
//                SimulatedMatchDto simulatedMatchDto = new SimulatedMatchDto();
//                simulatedMatchDto.setMatchDate(historicMatch.getMatchDate());
//                simulatedMatchDto.setHomeTeam(historicMatch.getHomeTeam());
//                simulatedMatchDto.setAwayTeam(historicMatch.getAwayTeam());
//                simulatedMatchDto.setMatchNumber(String.valueOf(i+1));
//                simulatedMatchDto.setHtResult(historicMatch.getHtResult());
//                simulatedMatchDto.setFtResult(historicMatch.getFtResult());
//                simulatedMatchDto.setSeason(season);
//                simulatedMatchDto.setCompetition(historicMatch.getCompetition());
//                if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
//                    simulatedMatchDto.setIsGreen(true);
//                    actualNegativeSequence = 0;
//                    isActiveSequence = false;
//                } else {
//                    simulatedMatchDto.setIsGreen(false);
//                }
//                matchesBetted.add(simulatedMatchDto);
//            } else {
//                if (!matchFollowStrategyRules(historicMatch, team.getName(), null)) {
//                    actualNegativeSequence++;
//                } else {
//                    actualNegativeSequence = 0;
//                }
//            }
//        }
//
//        return matchesBetted;
        return null;
    }

    @Override
    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategyName) {
        String res = historicMatch.getFtResult().split("\\(")[0];
        int homeResult = Integer.parseInt(res.split(":")[0]);
        int awayResult = Integer.parseInt(res.split(":")[1]);
        int totalGoals = homeResult + awayResult;
        if (((historicMatch.getHomeTeam().equals(teamName) && homeResult>awayResult) || (historicMatch.getAwayTeam().equals(teamName) && homeResult<awayResult))
                && totalGoals > 2) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<WinAndGoalsSeasonStats> statsByTeam = winAndGoalsSeasonInfoRepository.getFootballWinAndGoalsStatsByTeam(team);
        List<String> seasonsList = null;

        if (SUMMER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = SUMMER_SEASONS_LIST;
        } else if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = WINTER_SEASONS_LIST;
        }

        for (String season : seasonsList) {
            if (!statsByTeam.stream().filter(s -> s.getSeason().equals(season)).findAny().isPresent()) {
                String newSeasonUrl = "";

                List<HistoricMatch> filteredMatches = historicMatchRepository.getTeamMatchesBySeason(team, season);
                filteredMatches.sort(HistoricMatch.matchDateComparator);

                if (filteredMatches.size() == 0) {
                    continue;
                }

                WinAndGoalsSeasonStats winAndGoalsSeasonStats = new WinAndGoalsSeasonStats();

                ArrayList<Integer> negativeSequence = new ArrayList<>();
                int count = 0;
                int totalWins= 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    String res = historicMatch.getFtResult().split("\\(")[0];
                    count++;
                    int homeResult = Integer.parseInt(res.split(":")[0]);
                    int awayResult = Integer.parseInt(res.split(":")[1]);
                    if ((historicMatch.getHomeTeam().equals(team.getName()) && homeResult>awayResult) || (historicMatch.getAwayTeam().equals(team.getName()) && homeResult<awayResult)) {
                        totalWins++;
                        if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                            negativeSequence.add(count);
                            count = 0;
                        }
                    }
                }

                int totalWinAndGoals = negativeSequence.size();

                negativeSequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    negativeSequence.add(-1);
                }

                if (totalWins == 0) {
                    winAndGoalsSeasonStats.setWinAndGoalsRate(0);
                    winAndGoalsSeasonStats.setWinsRate(0);
                } else {
                    winAndGoalsSeasonStats.setWinAndGoalsRate(Utils.beautifyDoubleValue(100*totalWinAndGoals/totalWins));
                    winAndGoalsSeasonStats.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/filteredMatches.size()));
                }
                winAndGoalsSeasonStats.setCompetition("all");
                winAndGoalsSeasonStats.setNegativeSequence(negativeSequence.toString());
                winAndGoalsSeasonStats.setNumWinsAndGoals(totalWinAndGoals);
                winAndGoalsSeasonStats.setNumMatches(filteredMatches.size());
                winAndGoalsSeasonStats.setNumWins(totalWins);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(negativeSequence));
                winAndGoalsSeasonStats.setStdDeviation(stdDev);
                winAndGoalsSeasonStats.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, negativeSequence)));
                winAndGoalsSeasonStats.setSeason(season);
                winAndGoalsSeasonStats.setTeamId(team);
                winAndGoalsSeasonStats.setUrl(newSeasonUrl);
                insertStrategySeasonStats(winAndGoalsSeasonStats);
            }
        }
    }

    @Override
    public int calculateHistoricMaxNegativeSeq(List<WinAndGoalsSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public double calculateHistoricAvgNegativeSeq(List<WinAndGoalsSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<WinAndGoalsSeasonStats> statsByTeam = winAndGoalsSeasonInfoRepository.getFootballWinAndGoalsStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setMarginWinsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setMarginWinsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<WinAndGoalsSeasonStats> statsByTeam) {
        int last3SeasonsWinAndGoalsRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsWinAndGoalsRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsTotalWinsRateScore = calculateLast3SeasonsTotalWinsRateScore(statsByTeam);
        int allSeasonsTotalWinsRateScore = calculateAllSeasonsTotalWinsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWOWinAndGoalsScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWOWinAndGoalsScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.13*last3SeasonsWinAndGoalsRateScore + 0.07*allSeasonsWinAndGoalsRateScore +
            0.13*last3SeasonsTotalWinsRateScore + 0.07*allSeasonsTotalWinsRateScore +
            0.12*last3SeasonsmaxSeqWOWinAndGoalsScore + 0.06*allSeasonsmaxSeqWOWinAndGoalsScore +
            0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<WinAndGoalsSeasonStats> statsByTeam = winAndGoalsSeasonInfoRepository.getFootballWinAndGoalsStatsByTeam(team);
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

    @Override
    public int calculateLast3SeasonsRateScore(List<WinAndGoalsSeasonStats> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<3; i++) {
            marginWinsRates += statsByTeam.get(i).getWinAndGoalsRate();
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

    @Override
    public int calculateAllSeasonsRateScore(List<WinAndGoalsSeasonStats> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            marginWinsRates += statsByTeam.get(i).getWinAndGoalsRate();
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

    @Override
    public int calculateLast3SeasonsTotalWinsRateScore(List<WinAndGoalsSeasonStats> statsByTeam) {
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

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<WinAndGoalsSeasonStats> statsByTeam) {
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

}