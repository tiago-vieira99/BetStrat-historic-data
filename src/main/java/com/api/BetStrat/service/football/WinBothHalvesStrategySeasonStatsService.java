package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.WinBothHalvesSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.WinBothHalvesSeasonInfoRepository;
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
public class WinBothHalvesStrategySeasonStatsService extends StrategyScoreCalculator<WinBothHalvesSeasonStats> implements StrategySeasonStatsInterface<WinBothHalvesSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinBothHalvesStrategySeasonStatsService.class);

    @Autowired
    private WinBothHalvesSeasonInfoRepository winBothHalvesSeasonInfoRepository
            ;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public WinBothHalvesSeasonStats insertStrategySeasonStats(WinBothHalvesSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return winBothHalvesSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<WinBothHalvesSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return winBothHalvesSeasonInfoRepository.getFootballWinBothHalvesStatsByTeam(team);
    }

    @Override
    public List<SimulatedMatchDto> simulateStrategyBySeason(String season, Team team, String strategyName) {
        return null;
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
        if ( (historicMatch.getHomeTeam().equals(teamName) && homeHTResult > awayHTResult && home2HTResult > away2HTResult) ||
                (historicMatch.getAwayTeam().equals(teamName) && awayHTResult > homeHTResult && away2HTResult > home2HTResult) ) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<WinBothHalvesSeasonStats> statsByTeam = winBothHalvesSeasonInfoRepository.getFootballWinBothHalvesStatsByTeam(team);
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
                String mainCompetition = Utils.findMainCompetition(teamMatchesBySeason);
                List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(t -> t.getCompetition().equals(mainCompetition)).collect(Collectors.toList());
                filteredMatches.sort(new Utils.MatchesByDateSorter());

                if (filteredMatches.isEmpty()) {
                    continue;
                }

                WinBothHalvesSeasonStats winsSeasonInfo = new WinBothHalvesSeasonStats();

                ArrayList<Integer> noWinsSequence = new ArrayList<>();
                int count = 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    count++;
                    if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                        noWinsSequence.add(count);
                        count = 0;
                    }
                }

                int totalWins = noWinsSequence.size();

                noWinsSequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    noWinsSequence.add(-1);
                }

                if (totalWins == 0) {
                    winsSeasonInfo.setWinBothHalvesRate(0);
                } else {
                    winsSeasonInfo.setWinBothHalvesRate(Utils.beautifyDoubleValue(100*totalWins/filteredMatches.size()));
                }
                winsSeasonInfo.setCompetition(mainCompetition);
                winsSeasonInfo.setNegativeSequence(noWinsSequence.toString());
                winsSeasonInfo.setNumMatches(filteredMatches.size());
                winsSeasonInfo.setNumWinsBothHalves(totalWins);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(noWinsSequence));
                winsSeasonInfo.setStdDeviation(stdDev);
                winsSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, noWinsSequence)));
                winsSeasonInfo.setSeason(season);
                winsSeasonInfo.setTeamId(team);
                winsSeasonInfo.setUrl(newSeasonUrl);
                insertStrategySeasonStats(winsSeasonInfo);
            }
        }
    }

    @Override
    public Team updateTeamScore(Team teamByName) {
        List<WinBothHalvesSeasonStats> statsByTeam = winBothHalvesSeasonInfoRepository.getFootballWinBothHalvesStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setWinsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsTotalWinsRateScore = calculateLast3SeasonsTotalWinsRateScore(statsByTeam);
            int allSeasonsTotalWinsRateScore = calculateAllSeasonsTotalWinsRateScore(statsByTeam);
            int last3SeasonsmaxSeqWOWinsScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
            int allSeasonsmaxSeqWOWinsScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double last3SeasonsScore = Utils.beautifyDoubleValue(0.3*last3SeasonsTotalWinsRateScore + 0.4*last3SeasonsmaxSeqWOWinsScore + 0.3*last3SeasonsStdDevScore);
            double allSeasonsScore = Utils.beautifyDoubleValue(0.3*allSeasonsTotalWinsRateScore + 0.4*allSeasonsmaxSeqWOWinsScore + 0.3*allSeasonsStdDevScore);

            double totalScore = Utils.beautifyDoubleValue(0.75*last3SeasonsScore + 0.20*allSeasonsScore + 0.05*totalMatchesScore);

            teamByName.setWinsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        return null;
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<WinBothHalvesSeasonStats> statsByTeam) {
        double winsRates = 0;
        for (int i=0; i<3; i++) {
            winsRates += statsByTeam.get(i).getWinBothHalvesRate();
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
    public int calculateAllSeasonsRateScore(List<WinBothHalvesSeasonStats> statsByTeam) {
        double winsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            winsRates += statsByTeam.get(i).getWinBothHalvesRate();
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
    public int calculateLast3SeasonsTotalWinsRateScore(List<WinBothHalvesSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<3; i++) {
            totalWinsRates += statsByTeam.get(i).getWinBothHalvesRate();
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
    public int calculateAllSeasonsTotalWinsRateScore(List<WinBothHalvesSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            totalWinsRates += statsByTeam.get(i).getWinBothHalvesRate();
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

    static class SortStatsDataBySeason implements Comparator<WinBothHalvesSeasonStats> {

        @Override
        public int compare(WinBothHalvesSeasonStats a, WinBothHalvesSeasonStats b) {
            return Integer.valueOf(SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(SEASONS_LIST.indexOf(b.getSeason())));
        }
    }

}