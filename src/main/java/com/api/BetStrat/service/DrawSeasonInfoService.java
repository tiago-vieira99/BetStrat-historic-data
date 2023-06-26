package com.api.BetStrat.service;

import com.api.BetStrat.constants.TeamScoreEnum;
import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.repository.DrawSeasonInfoRepository;
import com.api.BetStrat.util.ScrappingUtil;
import com.api.BetStrat.util.TeamDFhistoricData;
import com.api.BetStrat.util.Utils;
import com.google.common.collect.ImmutableList;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.FBREF_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.FOOTBALL_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.FOOTBALL_SUMMER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.FOOTBALL_SUMMER_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.FOOTBALL_WINTER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.FOOTBALL_WINTER_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WORLDFOOTBALL_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.ZEROZERO_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.ZEROZERO_SEASON_CODES;

@Service
@Transactional
public class DrawSeasonInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrawSeasonInfoService.class);

    @Autowired
    private DrawSeasonInfoRepository drawSeasonInfoRepository;

    public DrawSeasonInfo insertDrawInfo(DrawSeasonInfo drawSeasonInfo) {
        LOGGER.info("Inserted " + drawSeasonInfo.getClass() + " for " + drawSeasonInfo.getTeamId().getName() + " and season " + drawSeasonInfo.getSeason());
        return drawSeasonInfoRepository.save(drawSeasonInfo);
    }

    public void updateStatsDataInfo(Team team) {
        List<DrawSeasonInfo> statsByTeam = drawSeasonInfoRepository.getStatsByTeam(team);
        List<String> seasonsList = null;

        if (FOOTBALL_SUMMER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = FOOTBALL_SUMMER_SEASONS_LIST;
        } else if (FOOTBALL_WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = FOOTBALL_WINTER_SEASONS_LIST;
        }

        for (String season : seasonsList) {
            if (!statsByTeam.stream().filter(s -> s.getSeason().equals(season)).findAny().isPresent()) {
                String teamUrl = team.getUrl();
                JSONArray scrappingData = null;
                String newSeasonUrl = "";

                if (teamUrl.contains(ZEROZERO_BASE_URL)) {
                    String seasonZZCode = ZEROZERO_SEASON_CODES.get(season);
                    newSeasonUrl = teamUrl.replaceAll("epoca_id=\\d+", "epoca_id=" + seasonZZCode);
                    scrappingData = ScrappingUtil.getScrappingData(team.getName(), season, newSeasonUrl, false);
                } else if (teamUrl.contains(FBREF_BASE_URL)) {
                    String newSeason = "";
                    if (season.contains("-")) {
                        newSeason = season.split("-")[0] + "-20" + season.split("-")[1];
                    } else {
                        newSeason = season;
                    }
                    newSeasonUrl = teamUrl.split("/matchlogs")[0].substring(0, teamUrl.split("/matchlogs")[0].lastIndexOf('/')) + "/" + newSeason + "/matchlogs" + teamUrl.split("/matchlogs")[1];
                    scrappingData = ScrappingUtil.getScrappingData(team.getName(), newSeason, newSeasonUrl, false);
                } else if (teamUrl.contains(WORLDFOOTBALL_BASE_URL)) {
                    String newSeason = "";
                    if (season.contains("-")) {
                        newSeason = "20" + season.split("-")[1];
                    } else {
                        newSeason = season;
                    }
                    newSeasonUrl = teamUrl + "/" + newSeason + "/3/";
                    scrappingData = ScrappingUtil.getScrappingData(team.getName(), newSeason, newSeasonUrl, false);
                }

                if (scrappingData != null) {
                    TeamDFhistoricData teamDFhistoricData = new TeamDFhistoricData();
                    try {
                        DrawSeasonInfo drawSeasonInfo = teamDFhistoricData.buildSeasonDFStatsData(scrappingData);
                        drawSeasonInfo.setSeason(season);
                        drawSeasonInfo.setTeamId(team);
                        drawSeasonInfo.setUrl(newSeasonUrl);
                        insertDrawInfo(drawSeasonInfo);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    public Team updateTeamScore (Team teamByName) {
        List<DrawSeasonInfo> statsByTeam = drawSeasonInfoRepository.getStatsByTeam(teamByName);
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);

        if (statsByTeam.size() < 3) {
            teamByName.setDrawsHunterScore(TeamScoreEnum.INSUFFICIENT_DATA.getValue());
        } else {
            int last3SeasonsDrawRateScore = calculateLast3SeasonsDrawRateScore(statsByTeam);
            int allSeasonsDrawRateScore = calculateAllSeasonsDrawRateScore(statsByTeam);
            int last3SeasonsmaxSeqWODrawScore = calculateLast3SeasonsmaxSeqWODrawScore(statsByTeam);
            int allSeasonsmaxSeqWODrawScore = calculateAllSeasonsmaxSeqWODrawScore(statsByTeam);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(statsByTeam);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(statsByTeam);
            int totalMatchesScore = calculateLeagueMatchesScore(statsByTeam.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsDrawRateScore + 0.15*allSeasonsDrawRateScore +
                    0.15*last3SeasonsmaxSeqWODrawScore + 0.05*allSeasonsmaxSeqWODrawScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.05*totalMatchesScore);

            teamByName.setDrawsHunterScore(calculateFinalRating(totalScore));
        }

        return teamByName;
    }

    public LinkedHashMap<String, String> getSimulatedScorePartialSeasons(Team teamByName, int seasonsToDiscard) {
        List<DrawSeasonInfo> statsByTeam = drawSeasonInfoRepository.getStatsByTeam(teamByName);
        LinkedHashMap<String, String> outMap = new LinkedHashMap<>();
        List<String> profits = ImmutableList.of("1","0,6","1,2","1,3","2","2,8","4,3","6,6","-20,4","-23,4","-19,8","-19,2","-15","-10,2","-1,2");

        if (statsByTeam.size() <= 2) {
            outMap.put("footballDrawHunter", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
            return outMap;
        }
        Collections.sort(statsByTeam, new SortStatsDataBySeason());
        Collections.reverse(statsByTeam);
        List<DrawSeasonInfo> filteredStats = statsByTeam.subList(seasonsToDiscard, statsByTeam.size());

        if (filteredStats.size() < 3 || !filteredStats.get(0).getSeason().equals(FOOTBALL_SUMMER_SEASONS_LIST.get(FOOTBALL_SUMMER_SEASONS_LIST.size()-1-seasonsToDiscard))) {
            outMap.put("footballDrawHunter", TeamScoreEnum.INSUFFICIENT_DATA.getValue());
            return outMap;
        } else {
            int last3SeasonsDrawRateScore = calculateLast3SeasonsDrawRateScore(filteredStats);
            int allSeasonsDrawRateScore = calculateAllSeasonsDrawRateScore(filteredStats);
            int last3SeasonsmaxSeqWODrawScore = calculateLast3SeasonsmaxSeqWODrawScore(filteredStats);
            int allSeasonsmaxSeqWODrawScore = calculateAllSeasonsmaxSeqWODrawScore(filteredStats);
            int last3SeasonsStdDevScore = calculateLast3SeasonsStdDevScore(filteredStats);
            int allSeasonsStdDevScore = calculateAllSeasonsStdDevScore(filteredStats);
            int totalMatchesScore = calculateLeagueMatchesScore(filteredStats.get(0).getNumMatches());

            double totalScore = Utils.beautifyDoubleValue(0.2*last3SeasonsDrawRateScore + 0.15*allSeasonsDrawRateScore +
                    0.15*last3SeasonsmaxSeqWODrawScore + 0.05*allSeasonsmaxSeqWODrawScore +
                    0.3*last3SeasonsStdDevScore + 0.1*allSeasonsStdDevScore + 0.05*totalMatchesScore);

            String finalScore = calculateFinalRating(totalScore);
            outMap.put("footballDrawHunter", finalScore);
            outMap.put("sequence", statsByTeam.get(seasonsToDiscard-1).getNoDrawsSequence());
            double balance = 0;
            String[] seqArray = statsByTeam.get(seasonsToDiscard - 1).getNoDrawsSequence().replaceAll("\\[","").replaceAll("]","").split(",");
            for (int i=0; i<seqArray.length-2; i++) {
                int excelBadRun = 2;
                int accepBadRun = 3;
                if (Integer.parseInt(seqArray[i].trim())-accepBadRun > 12) {
                    balance = -48;
                    break;
                }
                double drawsScorePoints = Double.parseDouble(finalScore.substring(finalScore.indexOf('(') + 1, finalScore.indexOf(')')));
                if (finalScore.contains("EXCEL") && Integer.parseInt(seqArray[i].trim()) > excelBadRun &&  Integer.parseInt(seqArray[i].trim()) <= 17) {
                   balance += Double.parseDouble(profits.get(Integer.parseInt(seqArray[i].trim())-excelBadRun-1).replaceAll(",","."));
                } else if (drawsScorePoints >= 70 && finalScore.contains("ACCEPTABLE") &&  Integer.parseInt(seqArray[i].trim()) > accepBadRun &&  Integer.parseInt(seqArray[i].trim()) <= 17) {
                    balance += Double.parseDouble(profits.get(Integer.parseInt(seqArray[i].trim())-accepBadRun-1).replaceAll(",","."));
                }
            }
            outMap.put("balance", String.valueOf(balance).replaceAll("\\.",","));
            return outMap;
        }
    }

    private String calculateFinalRating(double score) {
        if (isBetween(score,90,150)) {
            return TeamScoreEnum.EXCELLENT.getValue() + " (" + score + ")";
        } else if(isBetween(score,65,90)) {
            return TeamScoreEnum.ACCEPTABLE.getValue() + " (" + score + ")";
        } else if(isBetween(score,50,65)) {
            return TeamScoreEnum.RISKY.getValue() + " (" + score + ")";
        } else if(isBetween(score,0,50)) {
            return TeamScoreEnum.INAPT.getValue() + " (" + score + ")";
        }
        return "";
    }

    private int calculateLast3SeasonsDrawRateScore(List<DrawSeasonInfo> statsByTeam) {
        double sumDrawRates = 0;
        for (int i=0; i<3; i++) {
            sumDrawRates += statsByTeam.get(i).getDrawRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(sumDrawRates / 3);

        if (isBetween(avgDrawRate,35,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,30,35)) {
            return 90;
        } else if(isBetween(avgDrawRate,27,30)) {
            return 80;
        } else if(isBetween(avgDrawRate,25,27)) {
            return 60;
        } else if(isBetween(avgDrawRate,20,25)) {
            return 50;
        } else if(isBetween(avgDrawRate,0,20)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsDrawRateScore(List<DrawSeasonInfo> statsByTeam) {
        double sumDrawRates = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumDrawRates += statsByTeam.get(i).getDrawRate();
        }

        double avgDrawRate = Utils.beautifyDoubleValue(sumDrawRates / statsByTeam.size());

        if (isBetween(avgDrawRate,35,100)) {
            return 100;
        } else if(isBetween(avgDrawRate,30,35)) {
            return 90;
        } else if(isBetween(avgDrawRate,27,30)) {
            return 80;
        } else if(isBetween(avgDrawRate,25,27)) {
            return 60;
        } else if(isBetween(avgDrawRate,20,25)) {
            return 50;
        } else if(isBetween(avgDrawRate,0,20)) {
            return 30;
        }
        return 0;
    }

    private int calculateRecommendedLevelToStartSequence(List<DrawSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i = 0; i < 3; i++) {
            String sequenceStr = statsByTeam.get(i).getNoDrawsSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }
        return maxValue-6 < 0 ? 0 : maxValue-6;
    }

    private int calculateLast3SeasonsmaxSeqWODrawScore(List<DrawSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getNoDrawsSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,7)) {
            return 100;
        } else if(isBetween(maxValue,7,8)) {
            return 90;
        } else if(isBetween(maxValue,8,9)) {
            return 80;
        } else if(isBetween(maxValue,9,10)) {
            return 70;
        } else if(isBetween(maxValue,10,13)) {
            return 60;
        }  else if(isBetween(maxValue,12,15)) {
            return 50;
        }  else if(isBetween(maxValue,14,15)) {
            return 40;
        } else if(isBetween(maxValue,14,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsmaxSeqWODrawScore(List<DrawSeasonInfo> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getNoDrawsSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        if (isBetween(maxValue,0,7)) {
            return 100;
        } else if(isBetween(maxValue,7,8)) {
            return 90;
        } else if(isBetween(maxValue,8,9)) {
            return 80;
        } else if(isBetween(maxValue,9,10)) {
            return 70;
        } else if(isBetween(maxValue,10,13)) {
            return 60;
        }  else if(isBetween(maxValue,12,15)) {
            return 50;
        }  else if(isBetween(maxValue,14,15)) {
            return 40;
        } else if(isBetween(maxValue,14,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateLast3SeasonsStdDevScore(List<DrawSeasonInfo> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<3; i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/3);

        if (isBetween(avgStdDev,0,2.3)) {
            return 100;
        } else if(isBetween(avgStdDev,2.3,2.4)) {
            return 90;
        } else if(isBetween(avgStdDev,2.4,2.5)) {
            return 80;
        } else if(isBetween(avgStdDev,2.5,2.6)) {
            return 70;
        } else if(isBetween(avgStdDev,2.6,2.7)) {
            return 60;
        }  else if(isBetween(avgStdDev,2.7,2.8)) {
            return 50;
        }  else if(isBetween(avgStdDev,2.8,3)) {
            return 40;
        } else if(isBetween(avgStdDev,3,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateAllSeasonsStdDevScore(List<DrawSeasonInfo> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/statsByTeam.size());

        if (isBetween(avgStdDev,0,2.3)) {
            return 100;
        } else if(isBetween(avgStdDev,2.3,2.4)) {
            return 90;
        } else if(isBetween(avgStdDev,2.4,2.5)) {
            return 80;
        } else if(isBetween(avgStdDev,2.5,2.6)) {
            return 70;
        } else if(isBetween(avgStdDev,2.6,2.7)) {
            return 60;
        }  else if(isBetween(avgStdDev,2.7,2.8)) {
            return 50;
        }  else if(isBetween(avgStdDev,2.8,3)) {
            return 40;
        } else if(isBetween(avgStdDev,3,25)) {
            return 30;
        }
        return 0;
    }

    private int calculateLeagueMatchesScore(int totalMatches) {
        if (isBetween(totalMatches,0,31)) {
            return 100;
        } else if(isBetween(totalMatches,31,33)) {
            return 90;
        } else if(isBetween(totalMatches,33,35)) {
            return 80;
        } else if(isBetween(totalMatches,35,41)) {
            return 60;
        } else if(isBetween(totalMatches,41,50)) {
            return 30;
        }
        return 0;
    }

    private static boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x < upper;
    }

    static class SortStatsDataBySeason implements Comparator<DrawSeasonInfo> {

        @Override
        public int compare(DrawSeasonInfo a, DrawSeasonInfo b) {
            return Integer.valueOf(FOOTBALL_SEASONS_LIST.indexOf(a.getSeason()))
                    .compareTo(Integer.valueOf(FOOTBALL_SEASONS_LIST.indexOf(b.getSeason())));
        }
    }

    /* avaliar cada parametro independentemente:
     *
     * 1) dar peso a cada parametro:
     *   drawRate (last3) - 25
     *   drawRate (total) - 20
     *   maxSeqWODraw (last3) - 15
     *   maxSeqWODraw (total) - 5
     *   stdDev (last3) - 20
     *   stdDev (total) - 10
     *   numTotalMatches - 5
     *
     *
     *   drawRate -> (100 se > 35) ; (90 se < 35) ; (80 entre 27 e 30) ; (60 entre 25 e 27) ; (50 entre 20 e 25) ; (30 se < 20)
     *   maxSeqWODraw -> (100 se < 7) ; (90 se == 7) ; (80 se == 8) ; (70 se == 9) ; (60 se == 10 ou 11) ; (50 se == 12 ou 13) ; (40 se == 14) ; (30 se > 14)
     *   stdDev -> (100 se < 2.3) ; (90 se < 2.4) ; (80 se < 2.5) ; (70 se < 2.6) ; (60 se < 2.7) ; (50 se < 2.8) ; (40 se < 2.9) ; (30 se > 3)
     *   numTotalMatches -> (100 se < 30) ; (90 se < 32) ; (80 se < 34) ; (50 se < 40) ; (30 se > 40)
     *
     *
     * excellent: avg std dev < 2.1 && avg drawRate > 30 && list.size > 3 && maxSeqValue < 9
     * acceptable: ((avg std dev > 2.1 & < 2.5 ; min drawRate > 23) || avg drawRate > 32) && maxSeqValue <= 10
     * risky: (max std dev > 3 && min drawRate < 20) || maxSeqValue > 15
     *
     * */

}
