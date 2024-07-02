package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.BttsSeasonStats;
import com.api.BetStrat.entity.football.CleanSheetSeasonStats;
import com.api.BetStrat.entity.football.CleanSheetSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.CleanSheetSeasonInfoRepository;
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

import static com.api.BetStrat.constants.BetStratConstants.SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_LIST;
import static com.api.BetStrat.util.Utils.calculateCoeffVariation;
import static com.api.BetStrat.util.Utils.calculateSD;

@Service
@Transactional
public class CleanSheetStrategySeasonStatsService extends StrategyScoreCalculator<CleanSheetSeasonStats> implements StrategySeasonStatsInterface<CleanSheetSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanSheetStrategySeasonStatsService.class);

    @Autowired
    private CleanSheetSeasonInfoRepository cleanSheetSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public CleanSheetSeasonStats insertStrategySeasonStats(CleanSheetSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return cleanSheetSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<CleanSheetSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return cleanSheetSeasonInfoRepository.getFootballCleanSheetStatsByTeam(team);
    }

    @Override
    public List<SimulatedMatchDto> simulateStrategyBySeason(String season, Team team, String strategyName) {
        return null;
    }

    @Override
    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategyName) {
        String res = historicMatch.getFtResult().split("\\(")[0];
        int homeResult = Integer.parseInt(res.split(":")[0]);
        int awayResult = Integer.parseInt(res.split(":")[1]);
        if ((historicMatch.getHomeTeam().equals(teamName) && awayResult == 0) || (historicMatch.getAwayTeam().equals(teamName) && homeResult == 0)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<CleanSheetSeasonStats> statsByTeam = cleanSheetSeasonInfoRepository.getFootballCleanSheetStatsByTeam(team);
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
                Collections.sort(filteredMatches, HistoricMatch.matchDateComparator);

                if (filteredMatches.size() == 0) {
                    continue;
                }

                CleanSheetSeasonStats cleanSheetSeasonInfo = new CleanSheetSeasonStats();

                ArrayList<Integer> strategySequence = new ArrayList<>();
                int count = 0;
                int totalCleanSheets= 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    count++;
                    if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                        totalCleanSheets++;
                        strategySequence.add(count);
                        count = 0;
                    }
                }

                strategySequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    strategySequence.add(-1);
                }

                if (totalCleanSheets == 0) {
                    cleanSheetSeasonInfo.setCleanSheetRate(0);
                } else {
                    cleanSheetSeasonInfo.setCleanSheetRate(Utils.beautifyDoubleValue(100*totalCleanSheets/filteredMatches.size()));
                }
                cleanSheetSeasonInfo.setCompetition(mainCompetition);
                cleanSheetSeasonInfo.setNegativeSequence(strategySequence.toString());
                cleanSheetSeasonInfo.setNumMatches(filteredMatches.size());
                cleanSheetSeasonInfo.setNumCleanSheets(totalCleanSheets);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(strategySequence));
                cleanSheetSeasonInfo.setStdDeviation(stdDev);
                cleanSheetSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, strategySequence)));
                cleanSheetSeasonInfo.setSeason(season);
                cleanSheetSeasonInfo.setTeamId(team);
                cleanSheetSeasonInfo.setUrl(newSeasonUrl);
                insertStrategySeasonStats(cleanSheetSeasonInfo);
            }
        }
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<CleanSheetSeasonStats> statsByTeam = cleanSheetSeasonInfoRepository.getFootballCleanSheetStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter); //TODO test this
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setWinsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setCleanSheetScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<CleanSheetSeasonStats> statsByTeam) {
        int last3SeasonsCleanSheetRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsCleanSheetRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWOCleanSheetScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWOCleanSheetScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.2*last3SeasonsCleanSheetRateScore + 0.1*allSeasonsCleanSheetRateScore +
            0.18*last3SeasonsmaxSeqWOCleanSheetScore + 0.1*allSeasonsmaxSeqWOCleanSheetScore +
            0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<CleanSheetSeasonStats> statsByTeam = cleanSheetSeasonInfoRepository.getFootballCleanSheetStatsByTeam(team);
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
    public int calculateLast3SeasonsRateScore(List<CleanSheetSeasonStats> statsByTeam) {
        double winsRates = 0;
        for (int i=0; i<3; i++) {
            winsRates += statsByTeam.get(i).getCleanSheetRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(winsRates / 3);

        if (super.isBetween(avgWinsRate,80,100)) {
            return 100;
        } else if(super.isBetween(avgWinsRate,70,80)) {
            return 80;
        } else if(super.isBetween(avgWinsRate,50,70)) {
            return 60;
        } else if(super.isBetween(avgWinsRate,0,50)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsRateScore(List<CleanSheetSeasonStats> statsByTeam) {
        double winsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            winsRates += statsByTeam.get(i).getCleanSheetRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(winsRates / statsByTeam.size());

        if (super.isBetween(avgWinsRate,80,100)) {
            return 100;
        } else if(super.isBetween(avgWinsRate,70,80)) {
            return 80;
        } else if(super.isBetween(avgWinsRate,50,70)) {
            return 60;
        } else if(super.isBetween(avgWinsRate,0,50)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateLast3SeasonsTotalWinsRateScore(List<CleanSheetSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<3; i++) {
            totalWinsRates += statsByTeam.get(i).getCleanSheetRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / 3);

        if (super.isBetween(avgWinsRate,80,100)) {
            return 100;
        } else if(super.isBetween(avgWinsRate,70,80)) {
            return 90;
        } else if(super.isBetween(avgWinsRate,60,70)) {
            return 80;
        } else if(super.isBetween(avgWinsRate,50,60)) {
            return 70;
        } else if(super.isBetween(avgWinsRate,40,50)) {
            return 60;
        } else if(super.isBetween(avgWinsRate,0,40)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<CleanSheetSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            totalWinsRates += statsByTeam.get(i).getCleanSheetRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / statsByTeam.size());

        if (super.isBetween(avgWinsRate,80,100)) {
            return 100;
        } else if(super.isBetween(avgWinsRate,70,80)) {
            return 90;
        } else if(super.isBetween(avgWinsRate,60,70)) {
            return 80;
        } else if(super.isBetween(avgWinsRate,50,60)) {
            return 70;
        } else if(super.isBetween(avgWinsRate,40,50)) {
            return 60;
        } else if(super.isBetween(avgWinsRate,0,40)) {
            return 30;
        }
        return 0;
    }

}