package com.api.BetStrat.util;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.FCSTATS_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.HOCKEY_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.SEASONS_LIST;

@Slf4j
@Service
public class TeamGoalsFestHistoricData {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamGoalsFestHistoricData.class);

    private double mean = this.mean;

    public LinkedHashMap<String, Object> extractGoalsFestDataFromZZ(String url) {
        Document document = null;
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();
        LOGGER.info("Scraping data: " + url);

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Element> matches1X2 = document.getElementsByAttributeValue("class", "result").stream().collect(Collectors.toList());
        Collections.reverse(matches1X2);

        if (url.contains("page=2")) {
            try {
                document = Jsoup.connect(url.replace("page=2", "page=1")).get();
            } catch (IOException e) {
                log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
                log.error(e.toString());
            }
            List<Element> page1Matches = document.getElementsByAttributeValue("class", "result").stream().collect(Collectors.toList());
            Collections.reverse(page1Matches);
            matches1X2.addAll(page1Matches);
        }

        ArrayList<Integer> noGoalsFestSequence = new ArrayList<>();
        int count = 0;
        for (Element elem : matches1X2) {
            try {
                int homeScore = Integer.parseInt(elem.childNode(0).childNode(0).toString().split("-")[0]);
                int awayScore = Integer.parseInt(elem.childNode(0).childNode(0).toString().split("-")[1]);
                count++;
                if ((homeScore + awayScore) > 2 && homeScore > 0 && awayScore > 0) {
                    noGoalsFestSequence.add(count);
                    count = 0;
                }
            } catch (NumberFormatException e) {
                log.error(e.toString());
            }
        }

        returnMap.put("competition", "all");

        int lastHomeScore = Integer.parseInt(matches1X2.get(matches1X2.size()-1).childNode(0).childNode(0).toString().split("-")[0]);
        int lastAwayScore = Integer.parseInt(matches1X2.get(matches1X2.size()-1).childNode(0).childNode(0).toString().split("-")[1]);

        if ((lastHomeScore+lastAwayScore) > 2 && lastHomeScore > 0 && lastAwayScore > 0) {
            noGoalsFestSequence.add(0);
        } else {
            noGoalsFestSequence.add(count);
            noGoalsFestSequence.add(-1);
        }

        int totalMatches = matches1X2.size();
        double goalsFestRate = 100*(noGoalsFestSequence.size() - 1) / Double.valueOf(matches1X2.size());

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
