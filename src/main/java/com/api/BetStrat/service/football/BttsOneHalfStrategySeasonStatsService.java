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
import com.api.BetStrat.entity.football.BttsOneHalfSeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.repository.football.BttsOneHalfSeasonInfoRepository;
import com.api.BetStrat.repository.football.BttsSeasonInfoRepository;
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
public class BttsOneHalfStrategySeasonStatsService extends StrategyScoreCalculator<BttsOneHalfSeasonStats> implements StrategySeasonStatsInterface<BttsOneHalfSeasonStats> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BttsOneHalfStrategySeasonStatsService.class);

    @Autowired
    private BttsOneHalfSeasonInfoRepository bttsOneHalfSeasonInfoRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Override
    public BttsOneHalfSeasonStats insertStrategySeasonStats(BttsOneHalfSeasonStats strategySeasonStats) {
        LOGGER.info("Inserted " + strategySeasonStats.getClass() + " for " + strategySeasonStats.getTeamId().getName() + " and season " + strategySeasonStats.getSeason());
        return bttsOneHalfSeasonInfoRepository.save(strategySeasonStats);
    }

    @Override
    public List<BttsOneHalfSeasonStats> getStatsByStrategyAndTeam(Team team, String strategyName) {
        return bttsOneHalfSeasonInfoRepository.getFootballBttsOneHalfStatsByTeam(team);
    }

    @Override
    public void updateStrategySeasonStats(Team team, String strategyName) {
        List<BttsOneHalfSeasonStats> statsByTeam = bttsOneHalfSeasonInfoRepository.getFootballBttsOneHalfStatsByTeam(team);
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

                BttsOneHalfSeasonStats bttsSeasonStats = new BttsOneHalfSeasonStats();

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
                    bttsSeasonStats.setBttsOneHalfRate(0);
                } else {
                    bttsSeasonStats.setBttsOneHalfRate(Utils.beautifyDoubleValue(100*totalBtts/teamMatchesBySeason.size()));
                }
                bttsSeasonStats.setCompetition("all");
                bttsSeasonStats.setNegativeSequence(strategySequence.toString());
                bttsSeasonStats.setNumBttsOneHalf(totalBtts);
                bttsSeasonStats.setNumMatches(teamMatchesBySeason.size());

                double stdDev =  Utils.beautifyDoubleValue(calculateSD(strategySequence));
                bttsSeasonStats.setStdDeviation(stdDev);
                bttsSeasonStats.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev, strategySequence)));
                bttsSeasonStats.setSeason(season);
                bttsSeasonStats.setTeamId(team);
                bttsSeasonStats.setUrl(newSeasonUrl);
                statsByTeam.add(bttsSeasonStats);
                insertStrategySeasonStats(bttsSeasonStats);
            }
        }
        team.setBttsOneHalfMaxRedRun(calculateHistoricMaxNegativeSeq(statsByTeam));
        team.setBttsOneHalfAvgRedRun((int)Math.round(calculateHistoricAvgNegativeSeq(statsByTeam)));
        teamRepository.save(team);
    }

    @Override
    public int calculateHistoricMaxNegativeSeq(List<BttsOneHalfSeasonStats> statsByTeam) {
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
    public double calculateHistoricAvgNegativeSeq(List<BttsOneHalfSeasonStats> statsByTeam) {
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
        List<BttsOneHalfSeasonStats> statsByTeam = bttsOneHalfSeasonInfoRepository.getFootballBttsOneHalfStatsByTeam(teamByName);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent()) {
            teamByName.setBttsOneHalfScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            double totalScore = calculateTotalFinalScore(statsByTeam);
            teamByName.setBttsOneHalfScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    private double calculateTotalFinalScore(List<BttsOneHalfSeasonStats> statsByTeam) {
        int last3SeasonsBttsRateScore = calculateLast3SeasonsRateScore(statsByTeam);
        int allSeasonsBttsRateScore = calculateAllSeasonsRateScore(statsByTeam);
        int last3SeasonsmaxSeqWOBttsScore = calculateLast3SeasonsMaxSeqWOGreenScore(statsByTeam);
        int allSeasonsmaxSeqWOBttsScore = calculateAllSeasonsMaxSeqWOGreenScore(statsByTeam);
        int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
        int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
        int last3SeasonsCoefDevScore = calculateLast3SeasonsCoefDevScore(statsByTeam);
        int allSeasonsCoefDevScore = calculateAllSeasonsCoefDevScore(statsByTeam);
        int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

        return Utils.beautifyDoubleValue(0.15*last3SeasonsBttsRateScore + 0.05*allSeasonsBttsRateScore +
            0.15*last3SeasonsmaxSeqWOBttsScore + 0.07*allSeasonsmaxSeqWOBttsScore +
            0.2*last3SeasonsCoefDevScore + 0.11*allSeasonsCoefDevScore +
            0.18*last3SeasonsStdDevScore + 0.07*allSeasonsStdDevScore + 0.02*totalMatchesScore);
    }

    @Override
    public String calculateScoreBySeason(Team team, String season, String strategy) {
        List<BttsOneHalfSeasonStats> statsByTeam = bttsOneHalfSeasonInfoRepository.getFootballBttsOneHalfStatsByTeam(team);
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
    public HashMap<String, Object> getSimulatedMatchesByStrategyAndSeason(String season, Team team, String strategyName) {
        HashMap<String, Object> simuMapForSeason = new HashMap<>();
        List<SimulatedMatchDto> matchesBetted = new ArrayList<>();
        List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
        Collections.sort(teamMatchesBySeason, HistoricMatch.matchDateComparator);

        if (teamMatchesBySeason.size() == 0) {
            return simuMapForSeason;
        }

        List<BttsOneHalfSeasonStats> statsByTeam = bttsOneHalfSeasonInfoRepository.getFootballBttsOneHalfStatsByTeam(team);
        Collections.sort(statsByTeam, StrategySeasonStats.strategySeasonSorter);
        Collections.reverse(statsByTeam);

        int indexOfSeason = WINTER_SEASONS_LIST.indexOf(season);
        statsByTeam = statsByTeam.stream().filter(s -> WINTER_SEASONS_LIST.indexOf(s.getSeason()) < indexOfSeason).collect(Collectors.toList());
        int avgNegativeSeqForSeason = (int) Math.round(calculateHistoricAvgNegativeSeq(statsByTeam));
        int maxNegativeSeqForSeason = calculateHistoricMaxNegativeSeq(statsByTeam);

        boolean isActiveSequence = false; //when true it always bet on the first game
        int actualNegativeSequence = 0;
        for (int i = 0; i < teamMatchesBySeason.size(); i++) {
            HistoricMatch historicMatch = teamMatchesBySeason.get(i);
            if ((actualNegativeSequence >= Math.max(4, maxNegativeSeqForSeason - statsByTeam.get(0).getMaxSeqScale() / 10))) {
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
        try {
            String ftRes = historicMatch.getFtResult().split("\\(")[0];
            String htRes = historicMatch.getHtResult().split("\\(")[0];
            int homeHTResult = Integer.parseInt(htRes.split(":")[0]);
            int awayHTResult = Integer.parseInt(htRes.split(":")[1]);
            int home2HTResult = Math.abs(Integer.parseInt(ftRes.split(":")[0]) - homeHTResult);
            int away2HTResult = Math.abs(Integer.parseInt(ftRes.split(":")[1]) - awayHTResult);
            if ((homeHTResult > 0 && awayHTResult > 0) || (home2HTResult > 0 && away2HTResult > 0)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int calculateLast3SeasonsRateScore(List<BttsOneHalfSeasonStats> statsByTeam) {
        double BttsRates = 0;
        for (int i=0; i<3; i++) {
            BttsRates += statsByTeam.get(i).getBttsOneHalfRate();
        }

        double avgBttsRate = Utils.beautifyDoubleValue(BttsRates / 3);

        if (isBetween(avgBttsRate,60,100)) {
            return 100;
        } else if(isBetween(avgBttsRate,50,60)) {
            return 90;
        } else if(isBetween(avgBttsRate,35,50)) {
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
    public int calculateAllSeasonsRateScore(List<BttsOneHalfSeasonStats> statsByTeam) {
        double BttsRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            BttsRates += statsByTeam.get(i).getBttsOneHalfRate();
        }

        double avgBttsRate = Utils.beautifyDoubleValue(BttsRates / statsByTeam.size());

        if (isBetween(avgBttsRate,60,100)) {
            return 100;
        } else if(isBetween(avgBttsRate,50,60)) {
            return 90;
        } else if(isBetween(avgBttsRate,35,50)) {
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
    public int calculateLast3SeasonsTotalWinsRateScore(List<BttsOneHalfSeasonStats> statsByTeam) {
        return 0;
    }

    @Override
    public int calculateAllSeasonsTotalWinsRateScore(List<BttsOneHalfSeasonStats> statsByTeam) {
        return 0;
    }

}