package com.api.BetStrat.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HockeyEurohockeyScrappingData {

    private double mean = this.mean;

    public LinkedHashMap<String, Object> extractTeamsURLBySeason (String leagueURL) {
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();
        Document document = null;

        List<Node> teamsList = new ArrayList<>();
        try {
            document = Jsoup.connect(leagueURL).get();
            teamsList = document.getElementsByAttributeValueContaining("class", "standings").get(0).childNode(3).childNodes()
                    .stream().filter(t -> t.hasAttr("class")).collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

        for (Node team : teamsList) {
            String teamURL = team.childNode(1).childNode(0).attr("href");
            String teamName = team.childNode(1).childNode(0).childNode(0).toString();
//            int totalMatches = Integer.parseInt(team.childNode(2).childNode(0).toString());
//            int wins = Integer.parseInt(team.childNode(3).childNode(0).toString());
//            int winsOT = Integer.parseInt(team.childNode(4).childNode(0).toString());
//            int lossesOT = Integer.parseInt(team.childNode(5).childNode(0).toString());
//            int losses = Integer.parseInt(team.childNode(6).childNode(0).toString());

            String allMatchesForTeamURL ="https://www.eurohockey.com/games.html?id_season=" + leagueURL.substring(leagueURL.indexOf("=")+1) + "&id_show=2&id_league=" +
                    leagueURL.substring(leagueURL.lastIndexOf('/')+1, leagueURL.indexOf('?')) + "&id_club=" + teamURL.substring(teamURL.lastIndexOf('/')+1,teamURL.indexOf('?')) + "&list_number=-1";


            List<Node> allMatchesList = null;
            try {
                allMatchesList = navigateToMatchesForTeamPage(allMatchesForTeamURL);
                returnMap.put(teamName, allMatchesList);
            } catch (Exception e) {
                log.error("error getting matches links: " + e.toString());
            }
        }

        return returnMap;
    }

    private List<Node> navigateToMatchesForTeamPage(String teamURL) {
        Document document = null;

        try {
            document = Jsoup.connect(teamURL).get();
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return document.getElementsByAttributeValueContaining("class", "games_list").get(0).childNode(3).childNodes();
    }

    public LinkedHashMap<String, Object> buildDrawStatsMap(List<Node> allMatches) {
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        int totalMatches = allMatches.size();

        ArrayList<Integer> noDrawsSequence = new ArrayList<>();
        int count = 0;
        for (Node match : allMatches) {
            if (!match.childNode(3).toString().contains("POST")) {
                count++;
            }
            if (match.childNode(3).toString().contains("OT") || match.childNode(3).toString().contains("SO")) {
                noDrawsSequence.add(count);
                count = 0;
            }
        }

        int totalDraws = noDrawsSequence.size();

        noDrawsSequence.add(count);
        if (!allMatches.get(allMatches.size()-1).childNode(3).toString().contains("O")) {
            noDrawsSequence.add(-1);
        }

        returnMap.put("competition", allMatches.get(0).childNode(1).childNode(1).childNode(0).toString());
        returnMap.put("drawRate", Utils.beautifyDoubleValue(100*totalDraws/totalMatches));
        returnMap.put("noDrawsSeq", noDrawsSequence.toString());
        returnMap.put("totalDraws", totalDraws);
        returnMap.put("totalMatches", totalMatches);

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noDrawsSequence));
        returnMap.put("standardDeviation", stdDev);
        returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return returnMap;
    }

    public LinkedHashMap<String, Object> build23MarginWinStatsMap(List<Node> allMatches, String teamName) {
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        int totalGames = allMatches.size();
        int totalWins = 0;

        ArrayList<Integer> no1GoalWinSequence = new ArrayList<>();
        ArrayList<Integer> goalWinIndexes = new ArrayList<>();
        int count = 0;
        for (Node match : allMatches) {
            String homeTeam = match.childNode(2).childNode(0).childNode(0).toString();
            String awayTeam = match.childNode(2).childNode(2).childNode(0).toString();
            String matchResult = null;
            if (match.childNode(3).childNode(0).hasAttr("href")) {
                matchResult = match.childNode(3).childNode(0).childNode(0).toString();
            } else {
                matchResult = match.childNode(3).childNode(0).toString();
            }
            int homeGoals = 0, awayGoals = 0;
            try {
                homeGoals = Integer.parseInt(matchResult.split("-")[0]);
                awayGoals = Integer.parseInt(matchResult.split("-")[1].trim());
            } catch (Exception e) {
                log.error(e.toString());
            }

            if (((teamName.equals(homeTeam) && homeGoals>awayGoals) || (teamName.equals(awayTeam) && homeGoals<awayGoals)) && !match.childNode(3).toString().contains("O")) {
                totalWins++;
                if ((Math.abs(homeGoals-awayGoals) == 3 || Math.abs(homeGoals-awayGoals) == 2)) {
                    goalWinIndexes.add(1);
                } else {
                    goalWinIndexes.add(0);
                }
            } else {
                goalWinIndexes.add(0);
            }

        }

        for (Integer i : goalWinIndexes) {
            count++;
            if (i==1) {
                no1GoalWinSequence.add(count);
                count = 0;
            }
        }

        int num1OneWins = no1GoalWinSequence.size();
        if (goalWinIndexes.size()>1) {
            if (goalWinIndexes.get(goalWinIndexes.size() - 1) == 1) {
                no1GoalWinSequence.add(0);
            } else {
                no1GoalWinSequence.add(count);
                no1GoalWinSequence.add(-1);
            }
        }

        returnMap.put("competition", allMatches.get(0).childNode(1).childNode(1).childNode(0).toString());
        if (totalGames > 0) {
            returnMap.put("totalWinsRate", Utils.beautifyDoubleValue(totalWins * 100 / totalGames));
        } else {
            returnMap.put("totalWinsRate", 0.0);
        }
        returnMap.put("numMarginWins", num1OneWins);
        returnMap.put("numWins", totalWins);
        if (totalWins > 0) {
            returnMap.put("marginWinsRate", Utils.beautifyDoubleValue(Double.valueOf(num1OneWins)/Double.valueOf(totalWins)*100));
        } else {
            returnMap.put("marginWinsRate", 0.0);
        }

        returnMap.put("noMarginWinsSeq", no1GoalWinSequence.toString());
        returnMap.put("totalMatches", totalGames);
        if (no1GoalWinSequence.size()>1) {
            double stdDev = Utils.beautifyDoubleValue(calculateSD(no1GoalWinSequence));
            returnMap.put("standardDeviation", stdDev);
            returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));
        } else {
            returnMap.put("standardDeviation", 0.0);
            returnMap.put("coefficientVariation", 0.0);
        }

        return returnMap;
    }

    public LinkedHashMap<String, Object> buildAny2MarginWinStatsMap(List<Node> allMatches, String teamName) {
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        int totalGames = allMatches.size();
        int totalWins = 0;

        ArrayList<Integer> no1GoalWinSequence = new ArrayList<>();
        ArrayList<Integer> goalWinIndexes = new ArrayList<>();
        int count = 0;
        for (Node match : allMatches) {
            String homeTeam = match.childNode(2).childNode(0).childNode(0).toString();
            String awayTeam = match.childNode(2).childNode(2).childNode(0).toString();
            String matchResult = null;
            if (match.childNode(3).childNode(0).hasAttr("href")) {
                matchResult = match.childNode(3).childNode(0).childNode(0).toString();
            } else {
                matchResult = match.childNode(3).childNode(0).toString();
            }
            int homeGoals = 0, awayGoals = 0;
            try {
                homeGoals = Integer.parseInt(matchResult.split("-")[0]);
                awayGoals = Integer.parseInt(matchResult.split("-")[1].trim());
            } catch (Exception e) {
                log.error(e.toString());
            }

            if (((teamName.equals(homeTeam) && homeGoals>awayGoals) || (teamName.equals(awayTeam) && homeGoals<awayGoals)) && !match.childNode(3).toString().contains("O")) {
                totalWins++;
            }

            if (Math.abs(homeGoals-awayGoals) == 2) {
                goalWinIndexes.add(1);
            } else {
                goalWinIndexes.add(0);
            }
        }

        for (Integer i : goalWinIndexes) {
            count++;
            if (i==1) {
                no1GoalWinSequence.add(count);
                count = 0;
            }
        }

        int num1OneWins = no1GoalWinSequence.size();
        if (goalWinIndexes.size()>1) {
            if (goalWinIndexes.get(goalWinIndexes.size() - 1) == 1) {
                no1GoalWinSequence.add(0);
            } else {
                no1GoalWinSequence.add(count);
                no1GoalWinSequence.add(-1);
            }
        }

        returnMap.put("competition", allMatches.get(0).childNode(1).childNode(1).childNode(0).toString());
        if (totalGames > 0) {
            returnMap.put("totalWinsRate", Utils.beautifyDoubleValue(totalWins * 100 / totalGames));
        } else {
            returnMap.put("totalWinsRate", 0.0);
        }
        returnMap.put("numMarginWins", num1OneWins);
        returnMap.put("numWins", totalWins);
        if (totalWins > 0) {
            returnMap.put("marginWinsRate", Utils.beautifyDoubleValue(Double.valueOf(num1OneWins)/Double.valueOf(totalWins)*100));
        } else {
            returnMap.put("marginWinsRate", 0.0);
        }

        returnMap.put("noMarginWinsSeq", no1GoalWinSequence.toString());
        returnMap.put("totalMatches", totalGames);
        if (no1GoalWinSequence.size()>1) {
            double stdDev = Utils.beautifyDoubleValue(calculateSD(no1GoalWinSequence));
            returnMap.put("standardDeviation", stdDev);
            returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));
        } else {
            returnMap.put("standardDeviation", 0.0);
            returnMap.put("coefficientVariation", 0.0);
        }

        return returnMap;
    }

    public LinkedHashMap<String, Object> buildMargin3WinStatsMap(List<Node> allMatches, String teamName) {
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        int totalGames = allMatches.size();
        int totalWins = 0;

        ArrayList<Integer> no1GoalWinSequence = new ArrayList<>();
        ArrayList<Integer> goalWinIndexes = new ArrayList<>();
        int count = 0;
        for (Node match : allMatches) {
            String homeTeam = match.childNode(2).childNode(0).childNode(0).toString();
            String awayTeam = match.childNode(2).childNode(2).childNode(0).toString();
            String matchResult = null;
            if (match.childNode(3).childNode(0).hasAttr("href")) {
                matchResult = match.childNode(3).childNode(0).childNode(0).toString();
            } else {
                matchResult = match.childNode(3).childNode(0).toString();
            }
            int homeGoals = 0, awayGoals = 0;
            try {
                homeGoals = Integer.parseInt(matchResult.split("-")[0]);
                awayGoals = Integer.parseInt(matchResult.split("-")[1].trim());
            } catch (Exception e) {
                log.error(e.toString());
            }

            if (((teamName.equals(homeTeam) && homeGoals>awayGoals) || (teamName.equals(awayTeam) && homeGoals<awayGoals)) && !match.childNode(3).toString().contains("O")) {
                totalWins++;
                if (Math.abs(homeGoals-awayGoals) == 3) {
                    goalWinIndexes.add(1);
                } else {
                    goalWinIndexes.add(0);
                }
            } else {
                goalWinIndexes.add(0);
            }

        }

        for (Integer i : goalWinIndexes) {
            count++;
            if (i==1) {
                no1GoalWinSequence.add(count);
                count = 0;
            }
        }

        int num1OneWins = no1GoalWinSequence.size();
        if (goalWinIndexes.size()>1) {
            if (goalWinIndexes.get(goalWinIndexes.size() - 1) == 1) {
                no1GoalWinSequence.add(0);
            } else {
                no1GoalWinSequence.add(count);
                no1GoalWinSequence.add(-1);
            }
        }

        returnMap.put("competition", allMatches.get(0).childNode(1).childNode(1).childNode(0).toString());
        if (totalGames > 0) {
            returnMap.put("totalWinsRate", Utils.beautifyDoubleValue(totalWins * 100 / totalGames));
        } else {
            returnMap.put("totalWinsRate", 0.0);
        }
        returnMap.put("numMarginWins", num1OneWins);
        returnMap.put("numWins", totalWins);
        if (totalWins > 0) {
            returnMap.put("marginWinsRate", Utils.beautifyDoubleValue(Double.valueOf(num1OneWins)/Double.valueOf(totalWins)*100));
        } else {
            returnMap.put("marginWinsRate", 0.0);
        }

        returnMap.put("noMarginWinsSeq", no1GoalWinSequence.toString());
        returnMap.put("totalMatches", totalGames);
        if (no1GoalWinSequence.size()>1) {
            double stdDev = Utils.beautifyDoubleValue(calculateSD(no1GoalWinSequence));
            returnMap.put("standardDeviation", stdDev);
            returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));
        } else {
            returnMap.put("standardDeviation", 0.0);
            returnMap.put("coefficientVariation", 0.0);
        }

        return returnMap;
    }

    private double calculateSD(ArrayList<Integer> sequence) {
        List<Integer> sequence2 = new ArrayList<>();
        if (sequence.get(sequence.size()-1) == 0) {
            sequence2 = sequence.subList(0, sequence.size()-1);
            sequence2.add(1);
        } else {
            sequence2 = sequence.subList(0, sequence.size()-1);
        }

        //mean
        this.mean = sequence2.stream().mapToInt(Integer::intValue).average().getAsDouble();

        //squared deviation
        List<Double> ssList = new ArrayList<>();

        for (int i : sequence2) {
            ssList.add(Math.pow(Math.abs(this.mean-i),2));
        }

        //SS value
        double ssValue = ssList.stream().collect(Collectors.summingDouble(i -> i));

        //s^2
        double s2 = ssValue / (sequence2.size() - 1);

        //standard deviation
        double stdDev = Math.sqrt(s2);

        return stdDev;
    }

    private double calculateCoeffVariation(double stdDev) {
        return (stdDev/this.mean)*100;
    }
}
