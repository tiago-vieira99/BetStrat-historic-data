package com.api.BetStrat.util;

import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.GoalsFestSeasonInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamGoalsFestHistoricData {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamGoalsFestHistoricData.class);

    private double mean = this.mean;

    @SneakyThrows
    public GoalsFestSeasonInfo buildSeasonGoalsFestStatsData(JSONArray allMatches) {
        GoalsFestSeasonInfo goalsFestSeasonInfo = new GoalsFestSeasonInfo();

        ArrayList<Integer> noGoalsFestSequence = new ArrayList<>();
        int count = 0;
        for (int i=0; i < allMatches.length(); i++) {
            JSONObject match = (JSONObject) allMatches.get(i);
            String res = match.getString("ftResult");
            int homeResult = Integer.parseInt(res.split(":")[0]);
            int awayResult = Integer.parseInt(res.split(":")[1]);
            count++;
            if ((homeResult + awayResult > 2) && homeResult > 0 && awayResult > 0) {
                noGoalsFestSequence.add(count);
                count = 0;
            }
        }

        int totalGoalsFest = noGoalsFestSequence.size();

        noGoalsFestSequence.add(count);
        String lastResult = ((JSONObject) allMatches.get(allMatches.length() - 1)).getString("ftResult");
        int lastHomeResult = Integer.parseInt(lastResult.split(":")[0]);
        int lastAwayResult = Integer.parseInt(lastResult.split(":")[1]);
        if (!((lastHomeResult + lastAwayResult > 2) && lastHomeResult > 0 && lastAwayResult > 0)) {
            noGoalsFestSequence.add(-1);
        }

        goalsFestSeasonInfo.setCompetition("all");
        goalsFestSeasonInfo.setGoalsFestRate(Utils.beautifyDoubleValue(100*totalGoalsFest/allMatches.length()));
        goalsFestSeasonInfo.setNoGoalsFestSequence(noGoalsFestSequence.toString());
        goalsFestSeasonInfo.setNumGoalsFest(totalGoalsFest);
        goalsFestSeasonInfo.setNumMatches(allMatches.length());

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noGoalsFestSequence));
        goalsFestSeasonInfo.setStdDeviation(stdDev);
        goalsFestSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return goalsFestSeasonInfo;
    }

    @SneakyThrows
    public LinkedHashMap<String, Object> extractGoalsFestDataFromFBref(String url) {
        Document document = null;
        LinkedHashMap<String, Object> returnMap = new LinkedHashMap<>();
        LOGGER.info("Scraping data: " + url);

        try {
            Thread.sleep(2000);
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Node> allMatches = document.getElementsByAttributeValue("id", "matchlogs_for").get(0).childNode(7).childNodes().stream().filter(c -> c.siblingIndex() % 2 != 0).collect(Collectors.toList());
        allMatches = allMatches.stream().filter(m -> m.childNode(7).childNodes().size() > 0).collect(Collectors.toList());

        ArrayList<Integer> noGoalsFestSequence = new ArrayList<>();
        int count = 0;
        for (Node match : allMatches) {
            try {
                int goalsFor = Integer.parseInt(match.childNode(7).childNode(0).toString().trim());
                int goalsAgainst = Integer.parseInt(match.childNode(8).childNode(0).toString().trim());
                count++;
                if ((goalsFor + goalsAgainst) > 2 && goalsFor > 0 && goalsAgainst > 0) {
                    noGoalsFestSequence.add(count);
                    count = 0;
                }
            } catch (Exception e) {
                log.error(e.toString());
            }
        }

        returnMap.put("competition", "all");

        int lastGoalsFor = Integer.parseInt(allMatches.get(allMatches.size()-1).childNode(7).childNode(0).toString().trim());
        int lastGoalsAgainst = Integer.parseInt(allMatches.get(allMatches.size()-1).childNode(8).childNode(0).toString().trim());

        if ((lastGoalsFor+lastGoalsAgainst) > 2 && lastGoalsFor > 0 && lastGoalsAgainst > 0) {
            noGoalsFestSequence.add(0);
        } else {
            noGoalsFestSequence.add(count);
            noGoalsFestSequence.add(-1);
        }

        int totalMatches = allMatches.size();
        double goalsFestRate = 100*(noGoalsFestSequence.size() - 1) / Double.valueOf(allMatches.size());

        returnMap.put("goalsFestRate", Utils.beautifyDoubleValue(goalsFestRate));
        returnMap.put("noGoalsFestSeq", noGoalsFestSequence.toString());
        returnMap.put("totalGoalsFest", noGoalsFestSequence.size()-1);
        returnMap.put("totalMatches", totalMatches);

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noGoalsFestSequence));
        returnMap.put("standardDeviation", stdDev);
        returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return returnMap;
    }

    public LinkedHashMap<String, String> sequenceAnalysis(String sequence) {
        LinkedHashMap<String,String> returnMap = new LinkedHashMap<>();

        ArrayList<Integer> sequenceArray = new ArrayList<>();
        for (String s : sequence.split(",")) {
            sequenceArray.add(Integer.parseInt(s));
        }

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(sequenceArray));
        returnMap.put("standardDeviaiton", String.valueOf(stdDev));
        returnMap.put("coefficientVariation", String.valueOf(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev))));

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
