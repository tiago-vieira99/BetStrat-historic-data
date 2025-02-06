package com.api.BetStrat.service.football;

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
import com.api.BetStrat.entity.football.WinsMarginHomeSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.repository.football.WinsMarginHomeSeasonInfoRepository;
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
public class WinsMarginHomeStrategySeasonStatsService extends StrategyScoreCalculator<WinsMarginHomeSeasonStats> implements StrategySeasonStatsInterface<WinsMarginHomeSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinsMarginHomeStrategySeasonStatsService.class);

    @Autowired
    private WinsMarginHomeSeasonInfoRepository winsMarginHomeSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Override
    public WinsMarginHomeSeasonStats insertStrategySeasonStats(WinsMarginHomeSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return winsMarginHomeSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<WinsMarginHomeSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return winsMarginHomeSeasonInfoRepository.getFootballWinsMarginHomeStatsByTeam(team);
    }

    @Override
    public HashMap<String, Object> getSimulatedMatchesByStrategyAndSeason(String season, Team team, String strategyName) {
        HashMap<String, Object> simuMapForSeason = new HashMap<>();
        List<SimulatedMatchDto> matchesBetted = new ArrayList<>();
        List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
        String mainCompetition = Utils.findMainCompetition(teamMatchesBySeason);
        List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(t -> t.getCompetition().equals(mainCompetition) && t.getHomeTeam().equals(team.getName())).collect(Collectors.toList());
        filteredMatches.sort(HistoricMatch.matchDateComparator);

        if (filteredMatches.size() == 0) {
            return simuMapForSeason;
        }

        List<WinsMarginHomeSeasonStats> statsByTeam = winsMarginHomeSeasonInfoRepository.getFootballWinsMarginHomeStatsByTeam(team);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        int indexOfSeason = WINTER_SEASONS_LIST.indexOf(season);
        statsByTeam = statsByTeam.stream().filter(s -> WINTER_SEASONS_LIST.indexOf(s.getSeason()) < indexOfSeason).collect(Collectors.toList());
        int avgNegativeSeqForSeason = (int) Math.round(calculateHistoricAvgNegativeSeq(statsByTeam));
        int maxNegativeSeqForSeason = calculateHistoricMaxNegativeSeq(statsByTeam);

        boolean isActiveSequence = true; //when true it always bet on the first game
        int actualNegativeSequence = 0;
        for (int i = 0; i < filteredMatches.size(); i++) {
            HistoricMatch historicMatch = filteredMatches.get(i);
            if ((actualNegativeSequence >= 4)) {
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
        simuMapForSeason.put("matchesBetted", matchesBetted);
        simuMapForSeason.put("avgNegativeSeq", avgNegativeSeqForSeason);
        simuMapForSeason.put("maxNegativeSeq", maxNegativeSeqForSeason);
        return simuMapForSeason;
    }

    @Override
    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategyName) {
        String res = historicMatch.getFtResult().split("\\(")[0];
        int homeResult = Integer.parseInt(res.split(":")[0]);
        int awayResult = Integer.parseInt(res.split(":")[1]);
        if (((historicMatch.getHomeTeam().equals(teamName) && homeResult>awayResult) || (historicMatch.getAwayTeam().equals(teamName) && homeResult<awayResult))
                && (Math.abs(homeResult - awayResult) == 1 || Math.abs(homeResult - awayResult) == 2)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<WinsMarginHomeSeasonStats> statsByTeam = winsMarginHomeSeasonInfoRepository.getFootballWinsMarginHomeStatsByTeam(team);
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
                List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(t -> t.getCompetition().contains(mainCompetition) && t.getHomeTeam().equals(team.getName())).collect(Collectors.toList());
                filteredMatches.sort(HistoricMatch.matchDateComparator);

                if (filteredMatches.size() == 0) {
                    continue;
                }

                WinsMarginHomeSeasonStats winsMarginSeasonInfo = new WinsMarginHomeSeasonStats();

                ArrayList<Integer> noMarginWinsSequence = new ArrayList<>();
                int count = 0;
                int totalWins= 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    String res = historicMatch.getFtResult().split("\\(")[0];
                    count++;
                    int homeResult = Integer.parseInt(res.split(":")[0]);
                    int awayResult = Integer.parseInt(res.split(":")[1]);
                    if ((historicMatch.getHomeTeam().equals(team.getName()) && homeResult>awayResult)) {
                        totalWins++;
                        if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                            noMarginWinsSequence.add(count);
                            count = 0;
                        }
                    }
                }

                int totalMarginWins = noMarginWinsSequence.size();

                noMarginWinsSequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    noMarginWinsSequence.add(-1);
                }

                if (totalWins == 0) {
                    winsMarginSeasonInfo.setMarginWinsRate(0);
                    winsMarginSeasonInfo.setWinsRate(0);
                } else {
                    winsMarginSeasonInfo.setMarginWinsRate(Utils.beautifyDoubleValue(100*totalMarginWins/totalWins));
                    winsMarginSeasonInfo.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/filteredMatches.size()));
                }
                winsMarginSeasonInfo.setCompetition(mainCompetition);
                winsMarginSeasonInfo.setNegativeSequence(noMarginWinsSequence.toString());
                winsMarginSeasonInfo.setNumMarginWins(totalMarginWins);
                winsMarginSeasonInfo.setNumMatches(filteredMatches.size());
                winsMarginSeasonInfo.setNumWins(totalWins);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(noMarginWinsSequence));
                winsMarginSeasonInfo.setStdDeviation(stdDev);
                winsMarginSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, noMarginWinsSequence)));
                winsMarginSeasonInfo.setSeason(season);
                winsMarginSeasonInfo.setTeamId(team);
                winsMarginSeasonInfo.setUrl(newSeasonUrl);
                insertStrategySeasonStats(winsMarginSeasonInfo);
            }
        }
        team.setMarginWinsHomeMaxRedRun(calculateHistoricMaxNegativeSeq(statsByTeam));
        team.setMarginWinsHomeAvgRedRun((int)Math.round(calculateHistoricAvgNegativeSeq(statsByTeam)));
        teamRepository.save(team);
    }

    @Override
    public int calculateHistoricMaxNegativeSeq(List<WinsMarginHomeSeasonStats> statsByTeam) {
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
    public double calculateHistoricAvgNegativeSeq(List<WinsMarginHomeSeasonStats> statsByTeam) {
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
        List<WinsMarginHomeSeasonStats> statsByTeam = winsMarginHomeSeasonInfoRepository.getFootballWinsMarginHomeStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setMarginWinsHomeScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setMarginWinsHomeScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<WinsMarginHomeSeasonStats> statsByTeam) {
        int last3SeasonsGreensRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsGreensRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsTotalWinsRateScore = calculateLast3SeasonsTotalWinsRateScore(statsByTeam);
        int allSeasonsTotalWinsRateScore = calculateAllSeasonsTotalWinsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWOGreensScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWOGreensScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int last3SeasonsCoefDevScore = calculateLast3SeasonsCoefDevScore(statsByTeam);
        int allSeasonsCoefDevScore = calculateAllSeasonsCoefDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.1*last3SeasonsGreensRateScore + 0.05*allSeasonsGreensRateScore +
            0.1*last3SeasonsTotalWinsRateScore + 0.05*allSeasonsTotalWinsRateScore +
            0.14*last3SeasonsmaxSeqWOGreensScore + 0.05*allSeasonsmaxSeqWOGreensScore +
            0.2*last3SeasonsCoefDevScore + 0.07*allSeasonsCoefDevScore +
            0.15*last3SeasonsStdDevScore + 0.07*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<WinsMarginHomeSeasonStats> statsByTeam = winsMarginHomeSeasonInfoRepository.getFootballWinsMarginHomeStatsByTeam(team);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        int indexOfSeason = WINTER_SEASONS_LIST.indexOf(season);
        statsByTeam = statsByTeam.stream().filter(s -> WINTER_SEASONS_LIST.indexOf(s.getSeason()) < indexOfSeason).collect(Collectors.toList());

        if (statsByTeam.size() < 3) {
            return TeamScoreEnum.INSUFFICIENT_DATA.getValue();
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            return calculateFinalRating(totalScore);
        }
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<WinsMarginHomeSeasonStats> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<3; i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgMarginWinsRate = Utils.beautifyDoubleValue(marginWinsRates / 3);

        if (isBetween(avgMarginWinsRate,80,100)) {
            return 100;
        } else if(isBetween(avgMarginWinsRate,70,80)) {
            return 90;
        } else if(isBetween(avgMarginWinsRate,60,70)) {
            return 80;
        } else if(isBetween(avgMarginWinsRate,50,60)) {
            return 70;
        } else if(isBetween(avgMarginWinsRate,40,50)) {
            return 60;
        } else if(isBetween(avgMarginWinsRate,0,40)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsRateScore(List<WinsMarginHomeSeasonStats> statsByTeam) {
        double marginWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            marginWinsRates += statsByTeam.get(i).getMarginWinsRate();
        }

        double avgMarginWinsRate = Utils.beautifyDoubleValue(marginWinsRates / statsByTeam.size());

        if (isBetween(avgMarginWinsRate,80,100)) {
            return 100;
        } else if(isBetween(avgMarginWinsRate,70,80)) {
            return 90;
        } else if(isBetween(avgMarginWinsRate,60,70)) {
            return 80;
        } else if(isBetween(avgMarginWinsRate,50,60)) {
            return 70;
        } else if(isBetween(avgMarginWinsRate,40,50)) {
            return 60;
        } else if(isBetween(avgMarginWinsRate,0,40)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateLast3SeasonsTotalWinsRateScore(List<WinsMarginHomeSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<3; i++) {
            totalWinsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / 3);

        if (isBetween(avgWinsRate,70,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,60,70)) {
            return 90;
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
    public int calculateAllSeasonsTotalWinsRateScore(List<WinsMarginHomeSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            totalWinsRates += statsByTeam.get(i).getWinsRate();
        }

        double avgWinsRate = Utils.beautifyDoubleValue(totalWinsRates / statsByTeam.size());

        if (isBetween(avgWinsRate,70,100)) {
            return 100;
        } else if(isBetween(avgWinsRate,60,70)) {
            return 90;
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