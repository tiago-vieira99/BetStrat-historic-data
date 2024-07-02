package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.NoBttsSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.NoBttsSeasonInfoRepository;
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
public class NoBttsStrategySeasonStatsService extends StrategyScoreCalculator<NoBttsSeasonStats> implements StrategySeasonStatsInterface<NoBttsSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoBttsStrategySeasonStatsService.class);

    @Autowired
    private NoBttsSeasonInfoRepository noBttsSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public NoBttsSeasonStats insertStrategySeasonStats(NoBttsSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return noBttsSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<NoBttsSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return noBttsSeasonInfoRepository.getFootballNoBttsStatsByTeam(team);
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
        List<NoBttsSeasonStats> statsByTeam = noBttsSeasonInfoRepository.getFootballNoBttsStatsByTeam(team);
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

                NoBttsSeasonStats noBttsSeasonStats = new NoBttsSeasonStats();

                ArrayList<Integer> strategySequence = new ArrayList<>();
                int count = 0;
                for (HistoricMatch historicMatch : teamMatchesBySeason) {
                    String res = historicMatch.getFtResult().split("\\(")[0];
                    count++;
                    int homeResult = Integer.parseInt(res.split(":")[0]);
                    int awayResult = Integer.parseInt(res.split(":")[1]);
                    if (!(homeResult > 0 && awayResult > 0)) {
                        strategySequence.add(count);
                        count = 0;
                    }
                }

                int totalNoBtts = strategySequence.size();

                strategySequence.add(count);
                HistoricMatch lastMatch = teamMatchesBySeason.get(teamMatchesBySeason.size() - 1);
                String lastResult = lastMatch.getFtResult().split("\\(")[0];
                if ((Integer.parseInt(lastResult.split(":")[0]) > 0 && Integer.parseInt(lastResult.split(":")[1]) > 0)) {
                    strategySequence.add(-1);
                }

                if (totalNoBtts == 0) {
                    noBttsSeasonStats.setNoBttsRate(0);
                } else {
                    noBttsSeasonStats.setNoBttsRate(Utils.beautifyDoubleValue(100*totalNoBtts/teamMatchesBySeason.size()));
                }
                noBttsSeasonStats.setCompetition("all");
                noBttsSeasonStats.setNegativeSequence(strategySequence.toString());
                noBttsSeasonStats.setNumNoBtts(totalNoBtts);
                noBttsSeasonStats.setNumMatches(teamMatchesBySeason.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(strategySequence));
                noBttsSeasonStats.setStdDeviation(stdDev);
                noBttsSeasonStats.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, strategySequence)));
                noBttsSeasonStats.setSeason(season);
                noBttsSeasonStats.setTeamId(team);
                noBttsSeasonStats.setUrl(newSeasonUrl);
                insertStrategySeasonStats(noBttsSeasonStats);
            }
        }
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<NoBttsSeasonStats> statsByTeam = noBttsSeasonInfoRepository.getFootballNoBttsStatsByTeam(teamByName);
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

            teamByName.setNoBttsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        return null;
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<NoBttsSeasonStats> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<3; i++) {
            GoalsFestRates += statsByTeam.get(i).getNoBttsRate();
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
    public int calculateAllSeasonsRateScore(List<NoBttsSeasonStats> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            GoalsFestRates += statsByTeam.get(i).getNoBttsRate();
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
    public int calculateLast3SeasonsTotalWinsRateScore(List<NoBttsSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<NoBttsSeasonStats> statsByTeam) {
        return 0;
    }

}