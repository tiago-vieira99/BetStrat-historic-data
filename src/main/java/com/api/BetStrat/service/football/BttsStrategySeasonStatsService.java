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
            teamByName.setBttsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setBttsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<BttsSeasonStats> statsByTeam) {
        int last3SeasonsBttsRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsBttsRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWOBttsScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWOBttsScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);         
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.2*last3SeasonsBttsRateScore + 0.1*allSeasonsBttsRateScore +
            0.18*last3SeasonsmaxSeqWOBttsScore + 0.1*allSeasonsmaxSeqWOBttsScore +
            0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.02*totalMatchesScore);
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
            double totalScore = calculateTotalFinalScore(statsByTeam);
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

    @Override
    public int calculateLast3SeasonsRateScore(List<BttsSeasonStats> statsByTeam) {
        double BttsRates = 0;
        for (int i=0; i<3; i++) {
            BttsRates += statsByTeam.get(i).getBttsRate();
        }

        double avgBttsRate = Utils.beautifyDoubleValue(BttsRates / 3);

        if (isBetween(avgBttsRate,50,100)) {
            return 100;
        } else if(isBetween(avgBttsRate,40,50)) {
            return 90;
        } else if(isBetween(avgBttsRate,35,40)) {
            return 80;
        } else if(isBetween(avgBttsRate,30,35)) {
            return 60;
        } else if(isBetween(avgBttsRate,20,30)) {
            return 50;
        } else if(isBetween(avgBttsRate,0,20)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsRateScore(List<BttsSeasonStats> statsByTeam) {
        double BttsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            BttsRates += statsByTeam.get(i).getBttsRate();
        }

        double avgBttsRate = Utils.beautifyDoubleValue(BttsRates / statsByTeam.size());

        if (isBetween(avgBttsRate,50,100)) {
            return 100;
        } else if(isBetween(avgBttsRate,40,50)) {
            return 90;
        } else if(isBetween(avgBttsRate,35,40)) {
            return 80;
        } else if(isBetween(avgBttsRate,30,35)) {
            return 60;
        } else if(isBetween(avgBttsRate,20,30)) {
            return 50;
        } else if(isBetween(avgBttsRate,0,20)) {
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