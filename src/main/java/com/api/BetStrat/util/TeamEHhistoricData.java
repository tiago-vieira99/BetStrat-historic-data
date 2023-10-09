package com.api.BetStrat.util;

import com.api.BetStrat.entity.football.EuroHandicapSeasonInfo;
import com.api.BetStrat.entity.football.WinsMarginSeasonInfo;
import com.google.common.base.Splitter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.FCSTATS_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.SEASONS_LIST;

@Slf4j
@Service
public class TeamEHhistoricData {

    private double mean = this.mean;

    @SneakyThrows
    public WinsMarginSeasonInfo buildSeason12MarginWinStatsData(JSONArray allMatches, String teamName) {
        WinsMarginSeasonInfo winsMarginSeasonInfo = new WinsMarginSeasonInfo();

        ArrayList<Integer> noMarginWinsSequence = new ArrayList<>();
        int count = 0;
        int totalWins= 0;
        for (int i=0; i < allMatches.length(); i++) {
            JSONObject match = (JSONObject) allMatches.get(i);
            String res = match.getString("ftResult");
            count++;
            int homeResult = Integer.parseInt(res.split(":")[0]);
            int awayResult = Integer.parseInt(res.split(":")[1]);
            if ((match.getString("homeTeam").equals(teamName) && homeResult>awayResult) || (match.getString("awayTeam").equals(teamName) && homeResult<awayResult)) {
                totalWins++;
                if (Math.abs(homeResult - awayResult) <= 2) {
                    noMarginWinsSequence.add(count);
                    count = 0;
                }
            }
        }

        int totalMarginWins = noMarginWinsSequence.size();

        noMarginWinsSequence.add(count);
        JSONObject lastMatch = (JSONObject) allMatches.get(allMatches.length() - 1);
        String lastResult = lastMatch.getString("ftResult");
        if (!((lastMatch.getString("homeTeam").equals(teamName) && Integer.parseInt(lastResult.split(":")[0])>Integer.parseInt(lastResult.split(":")[1])) ||
                (lastMatch.getString("awayTeam").equals(teamName) && Integer.parseInt(lastResult.split(":")[0])<Integer.parseInt(lastResult.split(":")[1]))) ||
                (Math.abs(Integer.parseInt(lastResult.split(":")[0]) - Integer.parseInt(lastResult.split(":")[1])) > 2)) {
            noMarginWinsSequence.add(-1);
        }

        String selectedCompetition = ((JSONObject) allMatches.get(0)).getString("competition");

        winsMarginSeasonInfo.setCompetition(selectedCompetition);
        winsMarginSeasonInfo.setMarginWinsRate(Utils.beautifyDoubleValue(100*totalMarginWins/totalWins));
        winsMarginSeasonInfo.setNoMarginWinsSequence(noMarginWinsSequence.toString());
        winsMarginSeasonInfo.setNumMarginWins(totalMarginWins);
        winsMarginSeasonInfo.setNumMatches(allMatches.length());
        winsMarginSeasonInfo.setNumWins(totalWins);
        winsMarginSeasonInfo.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/allMatches.length()));

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noMarginWinsSequence));
        winsMarginSeasonInfo.setStdDeviation(stdDev);
        winsMarginSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return winsMarginSeasonInfo;
    }

    @SneakyThrows
    public EuroHandicapSeasonInfo buildSeasonEuroHandicapStatsData(JSONArray allMatches, String teamName) {
        EuroHandicapSeasonInfo euroHandicapSeasonInfo = new EuroHandicapSeasonInfo();

        ArrayList<Integer> noEuroHandicapsSequence = new ArrayList<>();
        int count = 0;
        int totalWins= 0;
        for (int i=0; i < allMatches.length(); i++) {
            JSONObject match = (JSONObject) allMatches.get(i);
            String res = match.getString("ftResult");
            count++;
            int homeResult = Integer.parseInt(res.split(":")[0]);
            int awayResult = Integer.parseInt(res.split(":")[1]);
            if ((match.getString("homeTeam").equals(teamName) && homeResult>awayResult) || (match.getString("awayTeam").equals(teamName) && homeResult<awayResult)) {
                totalWins++;
                if (Math.abs(homeResult - awayResult) == 1) {
                    noEuroHandicapsSequence.add(count);
                    count = 0;
                }
            }
        }

        int totalMarginWins = noEuroHandicapsSequence.size();

        noEuroHandicapsSequence.add(count);
        JSONObject lastMatch = (JSONObject) allMatches.get(allMatches.length() - 1);
        String lastResult = lastMatch.getString("ftResult");
        if (!((lastMatch.getString("homeTeam").equals(teamName) && Integer.parseInt(lastResult.split(":")[0])>Integer.parseInt(lastResult.split(":")[1])) ||
                (lastMatch.getString("awayTeam").equals(teamName) && Integer.parseInt(lastResult.split(":")[0])<Integer.parseInt(lastResult.split(":")[1]))) ||
                (Math.abs(Integer.parseInt(lastResult.split(":")[0]) - Integer.parseInt(lastResult.split(":")[1])) > 1)) {
            noEuroHandicapsSequence.add(-1);
        }

        String selectedCompetition = ((JSONObject) allMatches.get(0)).getString("competition");

        euroHandicapSeasonInfo.setCompetition(selectedCompetition);
        euroHandicapSeasonInfo.setMarginWinsRate(Utils.beautifyDoubleValue(100*totalMarginWins/totalWins));
        euroHandicapSeasonInfo.setNoMarginWinsSequence(noEuroHandicapsSequence.toString());
        euroHandicapSeasonInfo.setNumMarginWins(totalMarginWins);
        euroHandicapSeasonInfo.setNumMatches(allMatches.length());
        euroHandicapSeasonInfo.setNumWins(totalWins);
        euroHandicapSeasonInfo.setWinsRate(Utils.beautifyDoubleValue(100*totalWins/allMatches.length()));

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noEuroHandicapsSequence));
        euroHandicapSeasonInfo.setStdDeviation(stdDev);
        euroHandicapSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return euroHandicapSeasonInfo;
    }

    public LinkedHashMap<String, Object> extract12MarginGoalsDataFromFC(String url) {
        Document document = null;
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        Elements allMatches = document.getElementsByAttributeValueContaining("class", "matchRow");
        int totalGames = allMatches.size();

        ArrayList<Integer> no1GoalWinSequence = new ArrayList<>();
        ArrayList<Integer> goalWinIndexes = new ArrayList<>();
        int count = 0;
        for (Element match : allMatches) {
            String result = match.childNodes().stream().filter(m -> m.attributes().get("class").equals("boxIcon"))
                    .collect(Collectors.toList()).get(0).toString();
            String score = match.getElementsByAttributeValueContaining("class", "matchResult").get(0).childNode(0).childNode(0).toString();
            if (score.contains(":")) {
                int homeScore = Integer.parseInt(score.substring(0, score.indexOf(':')).replaceAll("[^0-9]", ""));
                int awayScore = Integer.parseInt(score.substring(score.indexOf(':')+1).replaceAll("[^0-9]", ""));
                if (result.contains(" boxIconW") && (Math.abs(homeScore-awayScore) == 1 || Math.abs(homeScore-awayScore) == 2)) {
                    goalWinIndexes.add(1);
                } else {
                    goalWinIndexes.add(0);
                }
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
        if (goalWinIndexes.get(goalWinIndexes.size()-1)==1) {
            no1GoalWinSequence.add(0);
        } else {
            no1GoalWinSequence.add(count);
            no1GoalWinSequence.add(-1);
        }

        int totalWins = document.getElementsByAttributeValueContaining("class", "boxIconWide boxIconW").size();

        String selectedCompetition = document.getElementsByAttribute("selected").get(1).childNode(0).toString();

        returnMap.put("competition", selectedCompetition);
        returnMap.put("totalWinsRate", Utils.beautifyDoubleValue(totalWins*100/totalGames));
        returnMap.put("numMarginWins", num1OneWins);
        returnMap.put("numWins", totalWins);
        returnMap.put("marginWinsRate", Utils.beautifyDoubleValue((double) num1OneWins/totalWins*100));
        returnMap.put("noMarginWinsSeq", no1GoalWinSequence.toString());
        returnMap.put("totalMatches", totalGames);
        double stdDev =  Utils.beautifyDoubleValue(calculateSD(no1GoalWinSequence));
        returnMap.put("standardDeviation", stdDev);
        returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return returnMap;
    }

    public LinkedHashMap<String, Object> extract12MarginGoalsDataZZ(String url) {
        Document document = null;
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Element> matches1X2 = document.getElementsByAttributeValue("class", "parent").stream().collect(Collectors.toList());
        Collections.reverse(matches1X2);
        ArrayList<Integer> no1GoalWinSequence = new ArrayList<>();
        ArrayList<Integer> goalWinIndexes = new ArrayList<>();
        int count = 0;
        for (Element elem : matches1X2) {
            String res = elem.childNode(0).childNode(0).toString();
            String score = elem.getElementsByAttributeValue("class", "result").get(0).childNode(0).childNode(0).toString();
            int homeScore = Integer.parseInt(score.substring(0, score.indexOf('-')));
            int awayScore = Integer.parseInt(score.substring(score.indexOf('-') + 1));
            if (res.contains("V") && (Math.abs(homeScore-awayScore) == 1 || Math.abs(homeScore-awayScore) == 2)) {
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

        List<Node> competitionNodes = document.getElementsByAttributeValue("name", "compet_id_jogos").get(0).childNodes().stream().filter(n -> n.hasAttr("selected")).collect(Collectors.toList());
        if (competitionNodes.size() > 0) {
            String competitionName = competitionNodes.get(0).childNode(0).toString();
            returnMap.put("competition", competitionName);
        }

        int num1OneWins = no1GoalWinSequence.size();
        if (goalWinIndexes.get(goalWinIndexes.size()-1)==1) {
            no1GoalWinSequence.add(0);
        } else {
            no1GoalWinSequence.add(count);
            no1GoalWinSequence.add(-1);
        }

//        String winsRate = document.getElementsByAttributeValue("class", "groupbar").stream().collect(Collectors.toList()).get(0).childNode(1).childNode(0).toString();
        String totalWins = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(3).childNode(0).toString();
        String totalGames = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(2).childNode(0).toString();

        returnMap.put("totalWinsRate", Utils.beautifyDoubleValue(Double.parseDouble(totalWins)*100/Double.parseDouble(totalGames)));
        returnMap.put("numMarginWins", num1OneWins);
        returnMap.put("numWins", totalWins);
        returnMap.put("marginWinsRate", Utils.beautifyDoubleValue(num1OneWins/Double.parseDouble(totalWins)*100));
        returnMap.put("noMarginWinsSeq", no1GoalWinSequence.toString());
        returnMap.put("totalMatches", totalGames);
        double stdDev =  Utils.beautifyDoubleValue(calculateSD(no1GoalWinSequence));
        returnMap.put("standardDeviation", stdDev);
        returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return returnMap;
    }

    public LinkedHashMap<String, Object> extractMarginWinsDataFromLastSeasonsFCStats(String teamUrl) {
        Document document = null;

        try {
            document = Jsoup.connect(teamUrl).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        String teamId = teamUrl.split(",")[3].replaceAll("[^0-9]", "");
        String teamUrlName = teamUrl.split(",")[2];

        List<Element> allSeasons = document.getElementsByAttributeValueContaining("class", "league_select_phase").stream().collect(Collectors.toList());
        Collections.reverse(allSeasons);

        String selectedCompetition = document.getElementsByAttribute("selected").get(1).childNode(0).toString();
        List<Element> availableSeasons = new ArrayList<>();

        for (int  i=0; i< allSeasons.size(); i++) {
            List<Node> element = allSeasons.get(i).childNodes().stream().filter(s -> s.getClass().toString().contains("Element")).collect(Collectors.toList())
                    .stream().filter(e -> e.childNode(0).toString().equals(selectedCompetition)).collect(Collectors.toList());
            if (element.size() > 0) {
                availableSeasons.add((Element) element.get(0).parentNode());
            }
        }

        LinkedHashMap<String, Object> returnMap = new LinkedHashMap<>();
        for (int i = 0; i < availableSeasons.size(); i++) {
            String seasonID = availableSeasons.get(i).getElementsByAttribute("value").stream()
                    .filter(s -> s.childNode(0).toString().equals(selectedCompetition)).collect(Collectors.toList()).get(0).attributes().get("value");
            String season = availableSeasons.get(i).attributes().get("id").substring(7);
            List<String> splittedSeason = Splitter.fixedLength(4).splitToList(season);
            if (splittedSeason.get(0).equals(splittedSeason.get(1))) {
                season = splittedSeason.get(0);
            } else {
                season = splittedSeason.get(0) + "-" + splittedSeason.get(1).substring(2);
            }
            if (SEASONS_LIST.contains(season)) {
                String seasonURL = FCSTATS_BASE_URL + "club,matches," + teamUrlName + "," + teamId + "," + seasonID + ".php";
                returnMap.put(season, extract12MarginGoalsDataFromFC(seasonURL));
            }
        }

        return returnMap;
    }


    public LinkedHashMap<String, Object> extractEuroHandicapDataFromFC(String url) {
        Document document = null;
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        Elements allMatches = document.getElementsByAttributeValueContaining("class", "matchRow");
        int totalGames = allMatches.size();

        ArrayList<Integer> no1GoalWinSequence = new ArrayList<>();
        ArrayList<Integer> goalWinIndexes = new ArrayList<>();
        int count = 0;
        for (Element match : allMatches) {
            String result = match.childNodes().stream().filter(m -> m.attributes().get("class").equals("boxIcon"))
                    .collect(Collectors.toList()).get(0).toString();
            String score = match.getElementsByAttributeValueContaining("class", "matchResult").get(0).childNode(0).childNode(0).toString();
            if (score.contains(":")) {
                int homeScore = Integer.parseInt(score.substring(0, score.indexOf(':')).replaceAll("[^0-9]", ""));
                int awayScore = Integer.parseInt(score.substring(score.indexOf(':')+1).replaceAll("[^0-9]", ""));
                if (result.contains(" boxIconW") && Math.abs(homeScore-awayScore) == 1) {
                    goalWinIndexes.add(1);
                } else {
                    goalWinIndexes.add(0);
                }
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
        if (goalWinIndexes.get(goalWinIndexes.size()-1)==1) {
            no1GoalWinSequence.add(0);
        } else {
            no1GoalWinSequence.add(count);
            no1GoalWinSequence.add(-1);
        }

        int totalWins = document.getElementsByAttributeValueContaining("class", "boxIconWide boxIconW").size();

        String selectedCompetition = document.getElementsByAttribute("selected").get(1).childNode(0).toString();

        returnMap.put("competition", selectedCompetition);
        returnMap.put("totalWinsRate", Utils.beautifyDoubleValue(totalWins*100/totalGames));
        returnMap.put("numMarginWins", num1OneWins);
        returnMap.put("numWins", totalWins);
        returnMap.put("marginWinsRate", Utils.beautifyDoubleValue((double) num1OneWins/totalWins*100));
        returnMap.put("noMarginWinsSeq", no1GoalWinSequence.toString());
        returnMap.put("totalMatches", totalGames);
        double stdDev =  Utils.beautifyDoubleValue(calculateSD(no1GoalWinSequence));
        returnMap.put("standardDeviation", stdDev);
        returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return returnMap;
    }

    public LinkedHashMap<String, Object> extractEuroHandicapDataZZ(String url) {
        Document document = null;
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Element> matches1X2 = document.getElementsByAttributeValue("class", "parent").stream().collect(Collectors.toList());
        Collections.reverse(matches1X2);
        ArrayList<Integer> no1GoalWinSequence = new ArrayList<>();
        ArrayList<Integer> goalWinIndexes = new ArrayList<>();
        int count = 0;
        for (Element elem : matches1X2) {
            String res = elem.childNode(0).childNode(0).toString();
            String score = elem.getElementsByAttributeValue("class", "result").get(0).childNode(0).childNode(0).toString();
            int homeScore = Integer.parseInt(score.substring(0, score.indexOf('-')));
            int awayScore = Integer.parseInt(score.substring(score.indexOf('-') + 1));
            if (res.contains("V") && Math.abs(homeScore-awayScore) == 1) {
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

        List<Node> competitionNodes = document.getElementsByAttributeValue("name", "compet_id_jogos").get(0).childNodes().stream().filter(n -> n.hasAttr("selected")).collect(Collectors.toList());
        if (competitionNodes.size() > 0) {
            String competitionName = competitionNodes.get(0).childNode(0).toString();
            returnMap.put("competition", competitionName);
        }

        int num1OneWins = no1GoalWinSequence.size();
        if (goalWinIndexes.get(goalWinIndexes.size()-1)==1) {
            no1GoalWinSequence.add(0);
        } else {
            no1GoalWinSequence.add(count);
            no1GoalWinSequence.add(-1);
        }

//        String winsRate = document.getElementsByAttributeValue("class", "groupbar").stream().collect(Collectors.toList()).get(0).childNode(1).childNode(0).toString();
        String totalWins = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(3).childNode(0).toString();
        String totalGames = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(2).childNode(0).toString();

        returnMap.put("totalWinsRate", Utils.beautifyDoubleValue(Double.parseDouble(totalWins)*100/Double.parseDouble(totalGames)));
        returnMap.put("numMarginWins", num1OneWins);
        returnMap.put("numWins", totalWins);
        returnMap.put("marginWinsRate", Utils.beautifyDoubleValue(num1OneWins/Double.parseDouble(totalWins)*100));
        returnMap.put("noMarginWinsSeq", no1GoalWinSequence.toString());
        returnMap.put("totalMatches", totalGames);
        double stdDev =  Utils.beautifyDoubleValue(calculateSD(no1GoalWinSequence));
        returnMap.put("standardDeviation", stdDev);
        returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return returnMap;
    }

    public LinkedHashMap<String, Object> extractEuroHandicapDataFromLastSeasonsFCStats(String teamUrl) {
        Document document = null;

        try {
            document = Jsoup.connect(teamUrl).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        String teamId = teamUrl.split(",")[3].replaceAll("[^0-9]", "");
        String teamUrlName = teamUrl.split(",")[2];

        List<Element> allSeasons = document.getElementsByAttributeValueContaining("class", "league_select_phase").stream().collect(Collectors.toList());
        Collections.reverse(allSeasons);

        String selectedCompetition = document.getElementsByAttribute("selected").get(1).childNode(0).toString();
        List<Element> availableSeasons = new ArrayList<>();

        for (int  i=0; i< allSeasons.size(); i++) {
            List<Node> element = allSeasons.get(i).childNodes().stream().filter(s -> s.getClass().toString().contains("Element")).collect(Collectors.toList())
                    .stream().filter(e -> e.childNode(0).toString().equals(selectedCompetition)).collect(Collectors.toList());
            if (element.size() > 0) {
                availableSeasons.add((Element) element.get(0).parentNode());
            }
        }

        LinkedHashMap<String, Object> returnMap = new LinkedHashMap<>();
        for (int i = 0; i < availableSeasons.size(); i++) {
            String seasonID = availableSeasons.get(i).getElementsByAttribute("value").stream()
                    .filter(s -> s.childNode(0).toString().equals(selectedCompetition)).collect(Collectors.toList()).get(0).attributes().get("value");
            String season = availableSeasons.get(i).attributes().get("id").substring(7);
            List<String> splittedSeason = Splitter.fixedLength(4).splitToList(season);
            if (splittedSeason.get(0).equals(splittedSeason.get(1))) {
                season = splittedSeason.get(0);
            } else {
                season = splittedSeason.get(0) + "-" + splittedSeason.get(1).substring(2);
            }
            if (SEASONS_LIST.contains(season)) {
                String seasonURL = FCSTATS_BASE_URL + "club,matches," + teamUrlName + "," + teamId + "," + seasonID + ".php";
                returnMap.put(season, extractEuroHandicapDataFromFC(seasonURL));
            }
        }

        return returnMap;
    }

    // https://www.socscistatistics.com/descriptive/coefficientvariation/default.aspx
    private double calculateSD(ArrayList<Integer> sequence) {
        List<Integer> sequence2 = new ArrayList<>();
        sequence2 = sequence.subList(0, sequence.size()-1);

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
