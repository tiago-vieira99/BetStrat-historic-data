package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.ConcedeBothHalvesSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.ConcedeBothHalvesSeasonInfoRepository;
import com.api.BetStrat.service.StrategyScoreCalculator;
import com.api.BetStrat.service.StrategySeasonStatsInterface;
import com.api.BetStrat.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.*;
import static com.api.BetStrat.util.Utils.calculateCoeffVariation;
import static com.api.BetStrat.util.Utils.calculateSD;

@Service
@Transactional
public class ConcedeBothHalvesSeasonStatsService extends StrategyScoreCalculator<ConcedeBothHalvesSeasonStats> implements StrategySeasonStatsInterface<ConcedeBothHalvesSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcedeBothHalvesSeasonStatsService.class);

    @Autowired
    private ConcedeBothHalvesSeasonInfoRepository concedeBothHalvesSeasonInfoRepository
            ;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public ConcedeBothHalvesSeasonStats insertStrategySeasonStats(ConcedeBothHalvesSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return concedeBothHalvesSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<ConcedeBothHalvesSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return concedeBothHalvesSeasonInfoRepository.getFootballConcedeBothHalvesStatsByTeam(team);
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<ConcedeBothHalvesSeasonStats> statsByTeam = concedeBothHalvesSeasonInfoRepository.getFootballConcedeBothHalvesStatsByTeam(team);
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

                ConcedeBothHalvesSeasonStats ConcedeBothHalvesSeasonStats = new ConcedeBothHalvesSeasonStats();

                ArrayList<Integer> strategySequence = new ArrayList<>();
                int count = 0;
                for (HistoricMatch historicMatch : teamMatchesBySeason) {
                    count++;
                    if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                        strategySequence.add(count);
                        count = 0;
                    }
                }

                int totalConcedeBothHalves = strategySequence.size();

                strategySequence.add(count);
                HistoricMatch lastMatch = teamMatchesBySeason.get(teamMatchesBySeason.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    strategySequence.add(-1);
                }

                if (totalConcedeBothHalves == 0) {
                    ConcedeBothHalvesSeasonStats.setConcedeBothHalvesRate(0);
                } else {
                    ConcedeBothHalvesSeasonStats.setConcedeBothHalvesRate(Utils.beautifyDoubleValue(100*totalConcedeBothHalves/teamMatchesBySeason.size()));
                }
                ConcedeBothHalvesSeasonStats.setCompetition("all");
                ConcedeBothHalvesSeasonStats.setNegativeSequence(strategySequence.toString());
                ConcedeBothHalvesSeasonStats.setNumConcedeBothHalves(totalConcedeBothHalves);
                ConcedeBothHalvesSeasonStats.setNumMatches(teamMatchesBySeason.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(strategySequence));
                ConcedeBothHalvesSeasonStats.setStdDeviation(stdDev);
                ConcedeBothHalvesSeasonStats.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, strategySequence)));
                ConcedeBothHalvesSeasonStats.setSeason(season);
                ConcedeBothHalvesSeasonStats.setTeamId(team);
                ConcedeBothHalvesSeasonStats.setUrl(newSeasonUrl);
                insertStrategySeasonStats(ConcedeBothHalvesSeasonStats);
            }
        }
    }

    @Override
    public double calculateHistoricMaxSeqValue(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public double calculateHistoricAvgSeqValue(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<ConcedeBothHalvesSeasonStats> statsByTeam = concedeBothHalvesSeasonInfoRepository.getFootballConcedeBothHalvesStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent()) {
            teamByName.setGoalsFestScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setConcedeBothHalvesScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        int last3SeasonsConcedeBothHalvesRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsConcedeBothHalvesRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWOConcedeBothHalvesScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWOConcedeBothHalvesScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.2*last3SeasonsConcedeBothHalvesRateScore + 0.1*allSeasonsConcedeBothHalvesRateScore +
            0.18*last3SeasonsmaxSeqWOConcedeBothHalvesScore + 0.1*allSeasonsmaxSeqWOConcedeBothHalvesScore +
            0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<ConcedeBothHalvesSeasonStats> statsByTeam = concedeBothHalvesSeasonInfoRepository.getFootballConcedeBothHalvesStatsByTeam(team);
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
    public List<SimulatedMatchDto> getSimulatedMatchesByStrategyAndSeason(String season, Team team, String strategyName) {
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
        String htRes = historicMatch.getHtResult().split("\\(")[0];
        int homeResult = Integer.parseInt(res.split(":")[0]);
        int awayResult = Integer.parseInt(res.split(":")[1]);
        int homeHTResult = Integer.parseInt(htRes.split(":")[0]);
        int awayHTResult = Integer.parseInt(htRes.split(":")[1]);
        int home2HTResult = homeResult - homeHTResult;
        int away2HTResult = awayResult - awayHTResult;
        if ( (historicMatch.getHomeTeam().equals(teamName) && awayHTResult > 0 && away2HTResult > 0) ||
                (historicMatch.getAwayTeam().equals(teamName) && homeHTResult > 0 && home2HTResult > 0) ) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<3; i++) {
            GoalsFestRates += statsByTeam.get(i).getConcedeBothHalvesRate();
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
    public int calculateAllSeasonsRateScore(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        double GoalsFestRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            GoalsFestRates += statsByTeam.get(i).getConcedeBothHalvesRate();
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
    public int calculateLast3SeasonsTotalWinsRateScore(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        return 0;
    }

}