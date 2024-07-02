package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.football.EuroHandicapSeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.EuroHandicapSeasonInfoRepository;
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
public class EuroHandicapStrategySeasonStatsService extends StrategyScoreCalculator<EuroHandicapSeasonStats> implements StrategySeasonStatsInterface<EuroHandicapSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EuroHandicapStrategySeasonStatsService.class);

    @Autowired
    private EuroHandicapSeasonInfoRepository euroHandicapSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public EuroHandicapSeasonStats insertStrategySeasonStats(EuroHandicapSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return euroHandicapSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<EuroHandicapSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return euroHandicapSeasonInfoRepository.getEuroHandicapStatsByTeam(team);
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
        if ((historicMatch.getHomeTeam().equals(teamName) && Math.abs(homeResult-awayResult) == 1) ||
            (historicMatch.getAwayTeam().equals(teamName) && Math.abs(homeResult-awayResult) == 1)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<EuroHandicapSeasonStats> statsByTeam = euroHandicapSeasonInfoRepository.getEuroHandicapStatsByTeam(team);
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

                EuroHandicapSeasonStats euroHandicapSeasonInfo = new EuroHandicapSeasonStats();

                ArrayList<Integer> noEuroHandicapsSequence = new ArrayList<>();
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
                            noEuroHandicapsSequence.add(count);
                            count = 0;
                        }
                    }
                }

                int totalMarginWins = noEuroHandicapsSequence.size();

                noEuroHandicapsSequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    noEuroHandicapsSequence.add(-1);
                }

                if (totalWins == 0) {
                    euroHandicapSeasonInfo.setMarginWinsRate(0);
                    euroHandicapSeasonInfo.setWinsRate(0);
                } else {
                    euroHandicapSeasonInfo.setMarginWinsRate(Utils.beautifyDoubleValue(100*totalMarginWins/totalWins));
                    euroHandicapSeasonInfo.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/filteredMatches.size()));
                }
                euroHandicapSeasonInfo.setCompetition(mainCompetition);
                euroHandicapSeasonInfo.setNegativeSequence(noEuroHandicapsSequence.toString());
                euroHandicapSeasonInfo.setNumMarginWins(totalMarginWins);
                euroHandicapSeasonInfo.setNumMatches(filteredMatches.size());
                euroHandicapSeasonInfo.setNumWins(totalWins);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(noEuroHandicapsSequence));
                euroHandicapSeasonInfo.setStdDeviation(stdDev);
                euroHandicapSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, noEuroHandicapsSequence)));
                euroHandicapSeasonInfo.setSeason(season);
                euroHandicapSeasonInfo.setTeamId(team);
                euroHandicapSeasonInfo.setUrl(newSeasonUrl);
                insertStrategySeasonStats(euroHandicapSeasonInfo);
            }
        }
    }

    @Override
    public Team updateTeamScore (Team teamByName) {
        List<EuroHandicapSeasonStats> statsByTeam = euroHandicapSeasonInfoRepository.getEuroHandicapStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setEuroHandicapScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setEuroHandicapScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<EuroHandicapSeasonStats> statsByTeam) {
        int last3SeasonsEuroHandicapRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsEuroHandicapRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsTotalWinsRateScore = calculateLast3SeasonsTotalWinsRateScore(statsByTeam);
        int allSeasonsTotalWinsRateScore = calculateAllSeasonsTotalWinsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWOEuroHandicapScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWOEuroHandicapScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.13*last3SeasonsEuroHandicapRateScore + 0.07*allSeasonsEuroHandicapRateScore +
            0.13*last3SeasonsTotalWinsRateScore + 0.07*allSeasonsTotalWinsRateScore +
            0.12*last3SeasonsmaxSeqWOEuroHandicapScore + 0.06*allSeasonsmaxSeqWOEuroHandicapScore +
            0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategyName) {
        return "";
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<EuroHandicapSeasonStats> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<3; i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
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
    public int calculateAllSeasonsRateScore(List<EuroHandicapSeasonStats> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
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
    public int calculateLast3SeasonsTotalWinsRateScore(List<EuroHandicapSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<3; i++) {
            totalWinsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / 3);

        if (isBetween(avgWinsRate,70,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,60,70)) {
            return 80;
        } else if(isBetween(avgWinsRate,40,60)) {
            return 60;
        } else if(isBetween(avgWinsRate,0,40)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<EuroHandicapSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            totalWinsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / statsByTeam.size());

        if (isBetween(avgWinsRate,70,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,60,70)) {
            return 80;
        } else if(isBetween(avgWinsRate,40,60)) {
            return 60;
        } else if(isBetween(avgWinsRate,0,40)) {
            return 30;
        }
        return 0;
    }

}