package com.api.BetStrat.util;

import io.swagger.models.auth.In;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamEHhistoricDataZZ {

    private double mean = this.mean;

    public LinkedHashMap<String, String> extractEHData(String url) {
        Document document = null;
        LinkedHashMap<String,String> returnMap = new LinkedHashMap<>();

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

        int num1OneWins = no1GoalWinSequence.size();
        if (goalWinIndexes.get(goalWinIndexes.size()-1)==1) {
            no1GoalWinSequence.add(0);
        } else {
            no1GoalWinSequence.add(count);
            no1GoalWinSequence.add(-1);
        }

        String winsRate = document.getElementsByAttributeValue("class", "groupbar").stream().collect(Collectors.toList()).get(0).childNode(1).childNode(0).toString();
        String totalWins = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(3).childNode(0).toString();
        String totalGames = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(2).childNode(0).toString();

        returnMap.put("Wins fractionalRate", totalWins+"/"+totalGames);
        returnMap.put("Wins percentageRate", winsRate);
        returnMap.put("1 Goal Wins fractionalRate", num1OneWins+"/"+totalWins);
        returnMap.put("1 Goal Wins percentageRate", String.valueOf(Utils.beautifyDoubleValue(num1OneWins/Double.parseDouble(totalWins)*100)));
        returnMap.put("no1GoalWinSeq", no1GoalWinSequence.toString());
        double stdDev =  Utils.beautifyDoubleValue(calculateSD(no1GoalWinSequence));
        returnMap.put("standardDeviaiton", String.valueOf(stdDev));
        returnMap.put("coefficientVariation", String.valueOf(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev))));

        return returnMap;
    }

    public LinkedHashMap<String, String> extract12MarginGoalsData(String url) {
        Document document = null;
        LinkedHashMap<String,String> returnMap = new LinkedHashMap<>();

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

        int num1OneWins = no1GoalWinSequence.size();
        if (goalWinIndexes.get(goalWinIndexes.size()-1)==1) {
            no1GoalWinSequence.add(0);
        } else {
            no1GoalWinSequence.add(count);
            no1GoalWinSequence.add(-1);
        }

        String winsRate = document.getElementsByAttributeValue("class", "groupbar").stream().collect(Collectors.toList()).get(0).childNode(1).childNode(0).toString();
        String totalWins = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(3).childNode(0).toString();
        String totalGames = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(2).childNode(0).toString();

        returnMap.put("Wins fractionalRate", totalWins+"/"+totalGames);
        returnMap.put("Wins percentageRate", winsRate);
        returnMap.put("1 Goal Wins fractionalRate", num1OneWins+"/"+totalWins);
        returnMap.put("1 Goal Wins percentageRate", String.valueOf(num1OneWins/Double.parseDouble(totalWins)*100));
        returnMap.put("no1GoalWinSeq", no1GoalWinSequence.toString());
        double stdDev =  Utils.beautifyDoubleValue(calculateSD(no1GoalWinSequence));
        returnMap.put("standardDeviaiton", String.valueOf(stdDev));
        returnMap.put("coefficientVariation", String.valueOf(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev))));

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
