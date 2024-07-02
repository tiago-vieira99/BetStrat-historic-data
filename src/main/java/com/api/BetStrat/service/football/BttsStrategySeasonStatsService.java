package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.BttsSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.BttsSeasonInfoRepository;
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
public class BttsStrategySeasonStatsService extends StrategyScoreCalculator<BttsSeasonStats> implements StrategySeasonStatsInterface<BttsSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BttsStrategySeasonStatsService.class);

    @Autowired
    private BttsSeasonInfoRepository bttsSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public BttsSeasonStats insertStrategySeasonStats(BttsSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return bttsSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<BttsSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return bttsSeasonInfoRepository.getFootballBttsStatsByTeam(team);
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<BttsSeasonStats> statsByTeam = bttsSeasonInfoRepository.getFootballBttsStatsByTeam(team);
        List<String> seasonsList = null;

        if (SUMMER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = SUMMER_SEASONS_LIST;
        } else if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = WINTER_SEASONS_LIST;
        }

        for (String season : seasonsList) {
            if (!statsByTeam.stream().anyMatch(s -> s.getSeason().equals(season))) {
                String newSeasonUrl = "";

                List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
                teamMatchesBySeason.sort(HistoricMatch.matchDateComparator);

                if (teamMatchesBySeason.size() == 0) {
                    continue;
                }

                BttsSeasonStats BttsSeasonStats = new BttsSeasonStats();

                ArrayList<Integer> strategySequence = new ArrayList<>();
                int count = 0;
                for (HistoricMatch historicMatch : teamMatchesBySeason) {
                    count++;
                    if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                        strategySequence.add(count);
                        count = 0;
                    }
                }

                int totalBtts = strategySequence.size();

                strategySequence.add(count);
                HistoricMatch lastMatch = teamMatchesBySeason.get(teamMatchesBySeason.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    strategySequence.add(-1);
                }

                if (totalBtts == 0) {
                    BttsSeasonStats.setBttsRate(0);
                } else {
                    BttsSeasonStats.setBttsRate(Utils.beautifyDoubleValue(100*totalBtts/teamMatchesBySeason.size()));
                }
                BttsSeasonStats.setCompetition("all");
                BttsSeasonStats.setNegativeSequence(strategySequence.toString());
                BttsSeasonStats.setNumBtts(totalBtts);
                BttsSeasonStats.setNumMatches(teamMatchesBySeason.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(strategySequence));
                BttsSeasonStats.setStdDeviation(stdDev);
                BttsSeasonStats.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, strategySequence)));
                BttsSeasonStats.setSeason(season);
                BttsSeasonStats.setTeamId(team);
                BttsSeasonStats.setUrl(newSeasonUrl);
                insertStrategySeasonStats(BttsSeasonStats);
            }
        }
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<BttsSeasonStats> statsByTeam = bttsSeasonInfoRepository.getFootballBttsStatsByTeam(teamByName);
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

            teamByName.setBttsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<BttsSeasonStats> statsByTeam = bttsSeasonInfoRepository.getFootballBttsStatsByTeam(team);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        int indexOfSeason = WINTER_SEASONS_LIST.indexOf(season);
        statsByTeam = statsByTeam.stream().filter(s -> WINTER_SEASONS_LIST.indexOf(s.getSeason()) < indexOfSeason).collect(Collectors.toList());

        if (statsByTeam.size() < 3 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent()) {
            return TeamScoreEnum.INSUFFICIENT_DATA.getValue();
        } else {
            //TODO move this block into calculateFinalRating method
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

            return calculateFinalRating(totalScore);
        }
    }

    @Override
    public List<SimulatedMatchDto> simulateStrategyBySeason(String season, Team team, String strategyName) {
        List<SimulatedMatchDto> matchesBetted = new ArrayList<>();
        List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
        Collections.sort(teamMatchesBySeason, HistoricMatch.matchDateComparator);

        if (teamMatchesBySeason.size() == 0) {
            return matchesBetted;
        }

        boolean isActiveSequence = true;
        int actualNegativeSequence = 0;
        for (int i = 0; i < teamMatchesBySeason.size(); i++) {
            HistoricMatch historicMatch = teamMatchesBySeason.get(i);
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
        if (homeResult > 0 && awayResult > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Deprecated
    public LinkedHashMap<String, String> getSimulatedScorePartialSeasons(Team teamByName, int seasonsToDiscard) {
        List<BttsSeasonStats> statsByTeam = bttsSeasonInfoRepository.getFootballBttsStatsByTeam(teamByName);
        LinkedHashMap<String, String> outMap = new LinkedHashMap<>();

        if (statsByTeam.size() <= 2 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent() || statsByTeam.size() < seasonsToDiscard) {
            outMap.put("footballGoalsFest", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
            return outMap;
        }
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);
        List<BttsSeasonStats> filteredStats = statsByTeam.subList(seasonsToDiscard, statsByTeam.size());

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
    public int calculateLast3SeasonsRateScore(List<BttsSeasonStats> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<3; i++) {
            GoalsFestRates += statsByTeam.get(i).getBttsRate();
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
    public int calculateAllSeasonsRateScore(List<BttsSeasonStats> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            GoalsFestRates += statsByTeam.get(i).getBttsRate();
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
    public int calculateLast3SeasonsTotalWinsRateScore(List<BttsSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<BttsSeasonStats> statsByTeam) {
        return 0;
    }

}