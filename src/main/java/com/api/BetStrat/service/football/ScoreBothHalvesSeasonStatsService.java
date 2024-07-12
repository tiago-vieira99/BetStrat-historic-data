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
import com.api.BetStrat.entity.football.ScoreBothHalvesSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.football.ScoreBothHalvesSeasonInfoRepository;
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
public class ScoreBothHalvesSeasonStatsService extends StrategyScoreCalculator<ScoreBothHalvesSeasonStats> implements StrategySeasonStatsInterface<ScoreBothHalvesSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreBothHalvesSeasonStatsService.class);

    @Autowired
    private ScoreBothHalvesSeasonInfoRepository scoreBothHalvesSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Override
    public ScoreBothHalvesSeasonStats insertStrategySeasonStats(ScoreBothHalvesSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return scoreBothHalvesSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<ScoreBothHalvesSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return scoreBothHalvesSeasonInfoRepository.getFootballScoreBothHalvesStatsByTeam(team);
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
        // //add try catch here
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
        String ftRes = historicMatch.getFtResult().split("\\(")[0];
        String htRes = historicMatch.getHtResult().split("\\(")[0];
        int homeHTResult = Integer.parseInt(htRes.split(":")[0]);
        int awayHTResult = Integer.parseInt(htRes.split(":")[1]);
        int home2HTResult = Math.abs(Integer.parseInt(ftRes.split(":")[0]) - homeHTResult);
        int away2HTResult = Math.abs(Integer.parseInt(ftRes.split(":")[1]) - awayHTResult);
        if ((historicMatch.getHomeTeam().equals(teamName) && homeHTResult > 0 && home2HTResult > 0) ||
            (historicMatch.getAwayTeam().equals(teamName) && 0 < awayHTResult && 0 < away2HTResult)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<ScoreBothHalvesSeasonStats> statsByTeam = scoreBothHalvesSeasonInfoRepository.getFootballScoreBothHalvesStatsByTeam(team);
        List<String> seasonsList = null;

        if (SUMMER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = SUMMER_SEASONS_LIST;
        } else if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = WINTER_SEASONS_LIST;
        }

        for (String season : seasonsList) {
            if (!statsByTeam.stream().filter(s -> s.getSeason().equals(season)).findAny().isPresent()) {
                String newSeasonUrl = "";

                List<HistoricMatch> filteredMatches = historicMatchRepository.getTeamMatchesBySeason(team, season);
                filteredMatches.sort(HistoricMatch.matchDateComparator);

                if (filteredMatches.size() == 0) {
                    continue;
                }

                ScoreBothHalvesSeasonStats scoreBothHalvesSeasonStats = new ScoreBothHalvesSeasonStats();

                ArrayList<Integer> negativeSequence = new ArrayList<>();
                int count = 0;
                int totalWins= 0;
                for (HistoricMatch historicMatch : filteredMatches) {
                    count++;
                    try {
                        if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                            negativeSequence.add(count);
                            count = 0;
                        }
                    } catch (NumberFormatException e) {
                        return;
                    }
                }

                negativeSequence.add(count);
                HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
                if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
                    negativeSequence.add(-1);
                }

                int totalScoreBothHalves = negativeSequence.size();

                if (totalScoreBothHalves == 0) {
                    scoreBothHalvesSeasonStats.setScoreBothHalvesRate(0);
                } else {
                    scoreBothHalvesSeasonStats.setScoreBothHalvesRate(Utils.beautifyDoubleValue(100*totalScoreBothHalves/filteredMatches.size()));
                }
                scoreBothHalvesSeasonStats.setCompetition("all");
                scoreBothHalvesSeasonStats.setNegativeSequence(negativeSequence.toString());
                scoreBothHalvesSeasonStats.setNumMatches(filteredMatches.size());
                scoreBothHalvesSeasonStats.setNumScoreBothHalves(totalWins);

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(negativeSequence));
                scoreBothHalvesSeasonStats.setStdDeviation(stdDev);
                scoreBothHalvesSeasonStats.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, negativeSequence)));
                scoreBothHalvesSeasonStats.setSeason(season);
                scoreBothHalvesSeasonStats.setTeamId(team);
                scoreBothHalvesSeasonStats.setUrl(newSeasonUrl);
                insertStrategySeasonStats(scoreBothHalvesSeasonStats);
            }
        }
    }

    @Override
    public int calculateHistoricMaxNegativeSeq(List<ScoreBothHalvesSeasonStats> statsByTeam) {
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
    public double calculateHistoricAvgNegativeSeq(List<ScoreBothHalvesSeasonStats> statsByTeam) {
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
        List<ScoreBothHalvesSeasonStats> statsByTeam = scoreBothHalvesSeasonInfoRepository.getFootballScoreBothHalvesStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setWinsScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setNoWinsScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<ScoreBothHalvesSeasonStats> statsByTeam) {
        int last3SeasonsScoreBothHalvesRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsScoreBothHalvesRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWOScoreBothHalvesScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWOScoreBothHalvesScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.2*last3SeasonsScoreBothHalvesRateScore + 0.1*allSeasonsScoreBothHalvesRateScore +
            0.18*last3SeasonsmaxSeqWOScoreBothHalvesScore + 0.1*allSeasonsmaxSeqWOScoreBothHalvesScore +
            0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<ScoreBothHalvesSeasonStats> statsByTeam = scoreBothHalvesSeasonInfoRepository.getFootballScoreBothHalvesStatsByTeam(team);
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
    public int calculateLast3SeasonsRateScore(List<ScoreBothHalvesSeasonStats> statsByTeam) {
        double winsRates = 0;
        for (int i=0; i<3; i++) {
            winsRates += statsByTeam.get(i).getScoreBothHalvesRate();
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
    public int calculateAllSeasonsRateScore(List<ScoreBothHalvesSeasonStats> statsByTeam) {
        double winsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            winsRates += statsByTeam.get(i).getScoreBothHalvesRate();
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
    public int calculateLast3SeasonsTotalWinsRateScore(List<ScoreBothHalvesSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<3; i++) {
            totalWinsRates += statsByTeam.get(i).getScoreBothHalvesRate();
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
    public int calculateAllSeasonsTotalWinsRateScore(List<ScoreBothHalvesSeasonStats> statsByTeam) {
        double totalWinsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            totalWinsRates += statsByTeam.get(i).getScoreBothHalvesRate();
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