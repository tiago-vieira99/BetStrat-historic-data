package com.api.BetStrat.service.football;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
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
                teamMatchesBySeason.sort(new Utils.MatchesByDateSorter());

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
    public Team updateTeamScore(Team teamByName) {
        List<ConcedeBothHalvesSeasonStats> statsByTeam = concedeBothHalvesSeasonInfoRepository.getFootballConcedeBothHalvesStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
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
        List<ConcedeBothHalvesSeasonStats> statsByTeam = concedeBothHalvesSeasonInfoRepository.getFootballConcedeBothHalvesStatsByTeam(team);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        int indexOfSeason = WINTER_SEASONS_LIST.indexOf(season);
        statsByTeam = statsByTeam.stream().filter(s -> WINTER_SEASONS_LIST.indexOf(s.getSeason()) < indexOfSeason).collect(Collectors.toList());

        if (statsByTeam.size() < 3 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent()) {
            return TeamScoreEnum.INSUFFICIENT_DATA.getValue();
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

            return calculateFinalRating(totalScore);
        }
    }

    @Override
    public List<SimulatedMatchDto> simulateStrategyBySeason(String season, Team team, String strategyName) {
        List<SimulatedMatchDto> matchesBetted = new ArrayList<>();
        List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);

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

    public LinkedHashMap<String, String> getSimulatedScorePartialSeasons(Team teamByName, int seasonsToDiscard) {
        List<ConcedeBothHalvesSeasonStats> statsByTeam = concedeBothHalvesSeasonInfoRepository.getFootballConcedeBothHalvesStatsByTeam(teamByName);
        LinkedHashMap<String, String> outMap = new LinkedHashMap<>();

        if (statsByTeam.size() <= 2 || statsByTeam.stream().filter(s -> s.getNumMatches() < 15).findAny().isPresent() || statsByTeam.size() < seasonsToDiscard) {
            outMap.put("footballGoalsFest", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
            return outMap;
        }
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);
        List<ConcedeBothHalvesSeasonStats> filteredStats = statsByTeam.subList(seasonsToDiscard, statsByTeam.size());

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

    public String calculateFinalRating(double score) {
        return super.calculateFinalRating(score);
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
    public int calculateLast3SeasonsMaxSeqWOGreenScore(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,5)) {
            return 100;
        } else if(isBetween(maxValue,5,6)) {
            return 90;
        } else if(isBetween(maxValue,6,7)) {
            return 80;
        } else if(isBetween(maxValue,7,8)) {
            return 50;
        } else if(isBetween(maxValue,8,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsMaxSeqWOGreenScore(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,5)) {
            return 100;
        } else if(isBetween(maxValue,5,6)) {
            return 90;
        } else if(isBetween(maxValue,6,7)) {
            return 80;
        } else if(isBetween(maxValue,7,8)) {
            return 50;
        } else if(isBetween(maxValue,8,25)) {
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

    @Override
    public int calculateLast3SeasonsStdDevScore(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<3; i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/3);

        if (isBetween(avgStdDev,0,1.7)) {
            return 100;
        } else if(isBetween(avgStdDev,1.7,2.0)) {
            return 80;
        } else if(isBetween(avgStdDev,2.0,2.2)) {
            return 70;
        } else if(isBetween(avgStdDev,2.2,2.4)) {
            return 50;
        } else if(isBetween(avgStdDev,2.4,25)) {
            return 30;
        }
        return 0;
    }

    @Override
    public int calculateAllSeasonsStdDevScore(List<ConcedeBothHalvesSeasonStats> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/statsByTeam.size());

        if (isBetween(avgStdDev,0,1.7)) {
            return 100;
        } else if(isBetween(avgStdDev,1.7,2.0)) {
            return 80;
        } else if(isBetween(avgStdDev,2.0,2.2)) {
            return 70;
        } else if(isBetween(avgStdDev,2.2,2.4)) {
            return 50;
        } else if(isBetween(avgStdDev,2.4,25)) {
            return 30;
        }
        return 0;
    }

    static class SortStatsDataBySeason implements Comparator<ConcedeBothHalvesSeasonStats> {

        @Override
        public int compare(ConcedeBothHalvesSeasonStats a, ConcedeBothHalvesSeasonStats b) {
            return Integer.valueOf(SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(SEASONS_LIST.indexOf(b.getSeason())));
        }
    }

}