package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.NoGoalsFestSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.NoGoalsFestSeasonInfoRepository;
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
    public List<SimulatedMatchDto> simulateStrategyBySeason(String season, Team team, String strategyName) {
        return null;
    }

    @Override
    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategyName) {
        return false;
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
                    String res = historicMatch.getFtResult().split("\\(")[0];
                    count++;
                    int homeResult = Integer.parseInt(res.split(":")[0]);
                    int awayResult = Integer.parseInt(res.split(":")[1]);
                    if (homeResult == 0 || awayResult == 0 || homeResult+awayResult <= 2) {
                        strategySequence.add(count);
                        count = 0;
                    }
                }

                int totalNoGoalsFest = strategySequence.size();

                strategySequence.add(count);
                HistoricMatch lastMatch = teamMatchesBySeason.get(teamMatchesBySeason.size() - 1);
                String lastResult = lastMatch.getFtResult().split("\\(")[0];
                if ((Integer.parseInt(lastResult.split(":")[0]) > 0 && Integer.parseInt(lastResult.split(":")[1]) > 0 &&
                        Integer.parseInt(lastResult.split(":")[0]) + Integer.parseInt(lastResult.split(":")[1]) > 2)) {
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
    public Team updateTeamScore(Team teamByName) {
        List<NoGoalsFestSeasonStats> statsByTeam = noGoalsFestSeasonInfoRepository.getNoGoalsFestStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent()) {
            teamByName.setGoalsFestScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsGoalsFestRateScore = calculateLast3SeasonsRateScore(statsByTeam);
            int allSeasonsGoalsFestRateScore = calculateAllSeasonsRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOGoalsFestScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
            int allSeasonsmaxSeqWOGoalsFestScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
//            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsGoalsFestRateScore + 0.1*allSeasonsGoalsFestRateScore +
                    0.2*last3SeasonsmaxSeqWOGoalsFestScore + 0.1*allSeasonsmaxSeqWOGoalsFestScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore);

            teamByName.setNoGoalsFestScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        return null;
    }

    public LinkedHashMap<String, String> getSimulatedScorePartialSeasons(Team teamByName, int seasonsToDiscard) {
        List<NoGoalsFestSeasonStats> statsByTeam = noGoalsFestSeasonInfoRepository.getNoGoalsFestStatsByTeam(teamByName);
        LinkedHashMap<String, String> outMap = new LinkedHashMap<>();

        if (statsByTeam.size() <= 2 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent() || statsByTeam.size() < seasonsToDiscard) {
            outMap.put("footballGoalsFest", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
            return outMap;
        }
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);
        List<NoGoalsFestSeasonStats> filteredStats = statsByTeam.subList(seasonsToDiscard, statsByTeam.size());

        if (filteredStats.size() < 3 || !filteredStats.get(0).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-1-seasonsToDiscard)) ||
                !filteredStats.get(1).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-2-seasonsToDiscard)) ||
                !filteredStats.get(2).getSeason().equals(WINTER_SEASONS_LIST.get(WINTER_SEASONS_LIST.size()-3-seasonsToDiscard))) {
            outMap.put("footballGoalsFest", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsGoalsFestRateScore = calculateLast3SeasonsRateScore(filteredStats);
            int allSeasonsGoalsFestRateScore = calculateAllSeasonsRateScore(filteredStats);
            int last3SeasonsmaxSeqWOGoalsFestScore = calculateLast3SeasonsMaxSeqWOGreenScore(filteredStats);
            int allSeasonsmaxSeqWOGoalsFestScore = calculateAllSeasonsMaxSeqWOGreenScore(filteredStats);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(filteredStats);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(filteredStats);
//            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsGoalsFestRateScore + 0.1*allSeasonsGoalsFestRateScore +
                    0.2*last3SeasonsmaxSeqWOGoalsFestScore + 0.1*allSeasonsmaxSeqWOGoalsFestScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore);

            String finalScore = calculateFinalRating(totalScore);
            outMap.put("footballGoalsFest", finalScore);
            outMap.put("sequence", statsByTeam.get(seasonsToDiscard-1).getNegativeSequence());
            double balance = 0;
            String[] seqArray = statsByTeam.get(seasonsToDiscard - 1).getNegativeSequence().replaceAll("\\[","").replaceAll("]","").split(",");
            for (int i=0; i<seqArray.length-2; i++) {
                int excelBadRun = 0;
                int accepBadRun = 0;
                if (balance < -20) {
                    break;
                }
//                } else if (Integer.parseInt(seqArray[i].trim())-accepBadRun >= 7 && Integer.parseInt(seqArray[i].trim())-accepBadRun < 10) {
//                    balance += -11;
//                } else if (Integer.parseInt(seqArray[i].trim())-accepBadRun >= 10 && Integer.parseInt(seqArray[i].trim())-accepBadRun < 13) {
//                    balance += -17;
//                } else if (Integer.parseInt(seqArray[i].trim())-accepBadRun >= 13 && Integer.parseInt(seqArray[i].trim())-accepBadRun < 16) {
//                    balance += -23;
//                } else if (Integer.parseInt(seqArray[i].trim())-accepBadRun >= 16) {
//                    balance += -29;
                double marginWinsScorePoints = Double.parseDouble(finalScore.substring(finalScore.indexOf('(') + 1, finalScore.indexOf(')')));
                if (finalScore.contains("EXCEL") && Integer.parseInt(seqArray[i].trim()) > excelBadRun) {
                    if (Integer.parseInt(seqArray[i].trim())-excelBadRun > 4) {
                        balance += -10;
                        continue;
                    }
                    balance += 1;
                } else if (finalScore.contains("ACCEPTABLE") &&  Integer.parseInt(seqArray[i].trim()) > accepBadRun) {
                    if (Integer.parseInt(seqArray[i].trim())-accepBadRun > 4) {
                        balance += -10;
                        continue;
                    }
                    balance += 1;
                }
            }
            outMap.put("balance", String.valueOf(balance).replaceAll("\\.",","));
        }
        return outMap;
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