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
import com.api.BetStrat.entity.football.CleanSheetSeasonStats;
import com.api.BetStrat.entity.football.NoGoalsFestSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.NoGoalsFestSeasonInfoRepository;
import com.api.BetStrat.service.StrategyScoreCalculator;
import com.api.BetStrat.service.StrategySeasonStatsInterface;
import com.api.BetStrat.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
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
public class NoGoalsFestStrategySeasonStatsService extends StrategyScoreCalculator<NoGoalsFestSeasonStats> implements StrategySeasonStatsInterface<NoGoalsFestSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoGoalsFestStrategySeasonStatsService.class);

    @Autowired
    private NoGoalsFestSeasonInfoRepository noGoalsFestSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public NoGoalsFestSeasonStats insertStrategySeasonStats(NoGoalsFestSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return noGoalsFestSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<NoGoalsFestSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return noGoalsFestSeasonInfoRepository.getNoGoalsFestStatsByTeam(team);
    }

    @Override
    public HashMap<String, Object> getSimulatedMatchesByStrategyAndSeason(String season, Team team, String strategyName) {
//        List<SimulatedMatchDto> matchesBetted = new ArrayList<>();
//        List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
//        Collections.sort(teamMatchesBySeason, HistoricMatch.matchDateComparator);
//
//        if (teamMatchesBySeason.size() == 0) {
//            return matchesBetted;
//        }
//
//        boolean isActiveSequence = true;
//        int actualNegativeSequence = 0;
//        for (int i = 0; i < teamMatchesBySeason.size(); i++) {
//            HistoricMatch historicMatch = teamMatchesBySeason.get(i);
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
        if (homeResult == 0 || awayResult == 0 || homeResult+awayResult <= 2) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<NoGoalsFestSeasonStats> statsByTeam = noGoalsFestSeasonInfoRepository.getNoGoalsFestStatsByTeam(team);
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

                NoGoalsFestSeasonStats noGoalsFestSeasonStats = new NoGoalsFestSeasonStats();

                ArrayList<Integer> strategySequence = new ArrayList<>();
                int count = 0;
                for (HistoricMatch historicMatch : teamMatchesBySeason) {
                    count++;
                    if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                        strategySequence.add(count);
                        count = 0;
                    }
                }

                int totalNoGoalsFest = strategySequence.size();

                strategySequence.add(count);
                HistoricMatch lastMatch = teamMatchesBySeason.get(teamMatchesBySeason.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    strategySequence.add(-1);
                }

                if (totalNoGoalsFest == 0) {
                    noGoalsFestSeasonStats.setNoGoalsFestRate(0);
                } else {
                    noGoalsFestSeasonStats.setNoGoalsFestRate(Utils.beautifyDoubleValue(100*totalNoGoalsFest/teamMatchesBySeason.size()));
                }
                noGoalsFestSeasonStats.setCompetition("all");
                noGoalsFestSeasonStats.setNegativeSequence(strategySequence.toString());
                noGoalsFestSeasonStats.setNumNoGoalsFest(totalNoGoalsFest);
                noGoalsFestSeasonStats.setNumMatches(teamMatchesBySeason.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(strategySequence));
                noGoalsFestSeasonStats.setStdDeviation(stdDev);
                noGoalsFestSeasonStats.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, strategySequence)));
                noGoalsFestSeasonStats.setSeason(season);
                noGoalsFestSeasonStats.setTeamId(team);
                noGoalsFestSeasonStats.setUrl(newSeasonUrl);
                insertStrategySeasonStats(noGoalsFestSeasonStats);
            }
        }
    }

    @Override
    public int calculateHistoricMaxNegativeSeq(List<NoGoalsFestSeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            int[] currSeqMaxValue = Arrays.stream(statsByTeam.get(i).getNegativeSequence().replaceAll("\\[","").replaceAll("\\]","")
                .replaceAll(" ","").split(",")).mapToInt(Integer::parseInt).toArray();
            if (currSeqMaxValue.length > 2) {
                for (int j = 0; j < currSeqMaxValue.length - 1; j++) {
                    if (currSeqMaxValue[j] > maxValue)
                        maxValue = currSeqMaxValue[j];
                }
            } else {
                if (currSeqMaxValue[0] > maxValue)
                    maxValue = currSeqMaxValue[0];
            }
        }

        return maxValue;
    }

    @Override
    public double calculateHistoricAvgNegativeSeq(List<NoGoalsFestSeasonStats> statsByTeam) {
        int seqValues = 0;
        int count = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String[] arraySeq = statsByTeam.get(i).getNegativeSequence()
                .replaceAll("\\[","").replaceAll("\\]","").replaceAll(" ","").split(",");
            count += arraySeq.length - 1;
            for (int j = 0; j < arraySeq.length - 1; j++)
                seqValues += Integer.parseInt(arraySeq[j]);
        }

        return Utils.beautifyDoubleValue((double) seqValues / (double) count);
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<NoGoalsFestSeasonStats> statsByTeam = noGoalsFestSeasonInfoRepository.getNoGoalsFestStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent()) {
            teamByName.setGoalsFestScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setNoGoalsFestScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<NoGoalsFestSeasonStats> statsByTeam) {
        int last3SeasonsNoGoalsFestRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsNoGoalsFestRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWONoGoalsFestScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWONoGoalsFestScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.2*last3SeasonsNoGoalsFestRateScore + 0.1*allSeasonsNoGoalsFestRateScore +
            0.18*last3SeasonsmaxSeqWONoGoalsFestScore + 0.1*allSeasonsmaxSeqWONoGoalsFestScore +
            0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<NoGoalsFestSeasonStats> statsByTeam = noGoalsFestSeasonInfoRepository.getNoGoalsFestStatsByTeam(team);
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
    public int calculateLast3SeasonsRateScore(List<NoGoalsFestSeasonStats> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<3; i++) {
            GoalsFestRates += statsByTeam.get(i).getNoGoalsFestRate();
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

    @Override
    public int calculateAllSeasonsRateScore(List<NoGoalsFestSeasonStats> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            GoalsFestRates += statsByTeam.get(i).getNoGoalsFestRate();
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
    @Override
    public int calculateLast3SeasonsTotalWinsRateScore(List<NoGoalsFestSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<NoGoalsFestSeasonStats> statsByTeam) {
        return 0;
    }

}