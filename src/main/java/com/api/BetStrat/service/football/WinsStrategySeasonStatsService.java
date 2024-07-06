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
import com.api.BetStrat.entity.football.WinsSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.WinsSeasonInfoRepository;
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
public class WinsStrategySeasonStatsService extends StrategyScoreCalculator<WinsSeasonStats> implements StrategySeasonStatsInterface<WinsSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinsStrategySeasonStatsService.class);

    @Autowired
    private WinsSeasonInfoRepository winsSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public WinsSeasonStats insertStrategySeasonStats(WinsSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return winsSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<WinsSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return winsSeasonInfoRepository.getFootballWinsStatsByTeam(team);
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
        if ((historicMatch.getHomeTeam().equals(teamName) && homeResult>awayResult) ||
            (historicMatch.getAwayTeam().equals(teamName) && homeResult<awayResult)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<WinsSeasonStats> statsByTeam = winsSeasonInfoRepository.getFootballWinsStatsByTeam(team);
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

                WinsSeasonStats winsSeasonInfo = new WinsSeasonStats();

                ArrayList<Integer> negativeSequence = new ArrayList<>();
                int count = 0;
                int totalWins= 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    count++;
                    if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                        totalWins++;
                        negativeSequence.add(count);
                        count = 0;
                    }
                }

                negativeSequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    negativeSequence.add(-1);
                }

                if (totalWins == 0) {
                    winsSeasonInfo.setWinsRate(0);
                } else {
                    winsSeasonInfo.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/filteredMatches.size()));
                }
                winsSeasonInfo.setCompetition(mainCompetition);
                winsSeasonInfo.setNegativeSequence(negativeSequence.toString());
                winsSeasonInfo.setNumMatches(filteredMatches.size());
                winsSeasonInfo.setNumWins(totalWins);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(negativeSequence));
                winsSeasonInfo.setStdDeviation(stdDev);
                winsSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, negativeSequence)));
                winsSeasonInfo.setSeason(season);
                winsSeasonInfo.setTeamId(team);
                winsSeasonInfo.setUrl(newSeasonUrl);
                insertStrategySeasonStats(winsSeasonInfo);
            }
        }
    }

    @Override
    public int calculateHistoricMaxNegativeSeq(List<WinsSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public double calculateHistoricAvgNegativeSeq(List<WinsSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<WinsSeasonStats> statsByTeam = winsSeasonInfoRepository.getFootballWinsStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setWinsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setWinsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<WinsSeasonStats> statsByTeam) {
        int last3SeasonsWinsRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsWinsRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWOWinsScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWOWinsScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.2*last3SeasonsWinsRateScore + 0.1*allSeasonsWinsRateScore +
            0.18*last3SeasonsmaxSeqWOWinsScore + 0.1*allSeasonsmaxSeqWOWinsScore +
            0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<WinsSeasonStats> statsByTeam = winsSeasonInfoRepository.getFootballWinsStatsByTeam(team);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        int indexOfSeason = WINTER_SEASONS_LIST.indexOf(season);
        statsByTeam = statsByTeam.stream().filter(s -> WINTER_SEASONS_LIST.indexOf(s.getSeason()) < indexOfSeason).collect(Collectors.toList());

        if (statsByTeam.size() < 3 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent()) {
            return TeamScoreEnum.INSUFFICIENT_DATA.getValue();
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            return calculateFinalRating(totalScore);
        }
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<WinsSeasonStats> statsByTeam) {
        double winsRates = 0;
        for (int i=0; i<3; i++) {
            winsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(winsRates / 3);

        if (isBetween(avgWinsRate,80,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,70,80)) {
            return 80;
        } else if(isBetween(avgWinsRate,50,70)) {
            return 60;
        } else if(isBetween(avgWinsRate,0,50)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsRateScore(List<WinsSeasonStats> statsByTeam) {
        double winsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            winsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(winsRates / statsByTeam.size());

        if (isBetween(avgWinsRate,80,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,70,80)) {
            return 80;
        } else if(isBetween(avgWinsRate,50,70)) {
            return 60;
        } else if(isBetween(avgWinsRate,0,50)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateLast3SeasonsTotalWinsRateScore(List<WinsSeasonStats> statsByTeam) {
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
    public int calculateAllSeasonsTotalWinsRateScore(List<WinsSeasonStats> statsByTeam) {
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