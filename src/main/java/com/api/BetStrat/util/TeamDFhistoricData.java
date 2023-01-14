package com.api.BetStrat.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamDFhistoricData {

    private double mean = this.mean;

    public LinkedHashMap<String, Object> extractDFDataFromZZ(String url) {
        Document document = null;
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Element> matches1X2 = document.getElementsByAttributeValue("class", "form").stream().collect(Collectors.toList());
        Collections.reverse(matches1X2);
        ArrayList<Integer> noDrawsSequence = new ArrayList<>();
        int count = 0;
        for (Element elem : matches1X2) {
            String res = elem.childNode(0).childNode(0).toString();
            count++;
            if (res.contains("E")) {
                noDrawsSequence.add(count);
                count = 0;
            }
        }

        List<Node> competitionNodes = document.getElementsByAttributeValue("name", "compet_id_jogos").get(0).childNodes().stream().filter(n -> n.hasAttr("selected")).collect(Collectors.toList());
        if (competitionNodes.size() > 0) {
            String competitionName = competitionNodes.get(0).childNode(0).toString();
            returnMap.put("competition", competitionName);
        }


        noDrawsSequence.add(count);
        if (matches1X2.get(matches1X2.size()-1).childNode(0).childNode(0).toString().contains("E")) {
            noDrawsSequence.add(0);
        } else {
            noDrawsSequence.add(-1);
        }

        String drawsRate = document.getElementsByAttributeValue("class", "groupbar").stream().collect(Collectors.toList()).get(1).childNode(1).childNode(0).toString();
        String totalMatches = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(2).childNode(0).toString();
        String totalDraws = document.getElementsByAttributeValue("class", "totals").stream().collect(Collectors.toList()).get(4).childNode(0).toString();

        returnMap.put("drawRate", drawsRate.substring(0, drawsRate.length()-1));
        returnMap.put("noDrawsSeq", noDrawsSequence.toString());
        returnMap.put("totalDraws", totalDraws);
        returnMap.put("totalMatches", totalMatches);

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noDrawsSequence));
        returnMap.put("standardDeviation", stdDev);
        returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return returnMap;
    }

    public LinkedHashMap<String, Object> extractDFDataFromFC(String url) {
        Document document = null;
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        Elements allMatches = document.getElementsByAttributeValueContaining("class", "matchRow");
        int totalMatches = allMatches.size();

        ArrayList<Integer> noDrawsSequence = new ArrayList<>();
        int count = 0;
        for (Element match : allMatches) {
            String res = match.childNodes().stream().filter(m -> m.attributes().get("class").equals("boxIcon"))
                    .collect(Collectors.toList()).get(0).toString();
            count++;
            if (res.contains("IconD")) {
                noDrawsSequence.add(count);
                count = 0;
            }
        }

        int totalDraws = noDrawsSequence.size();

        noDrawsSequence.add(count);
        if (allMatches.get(allMatches.size()-1).childNodes().stream().filter(m -> m.attributes().get("class").equals("boxIcon"))
                .collect(Collectors.toList()).get(0).toString().contains("IconD")) {
            noDrawsSequence.add(0);
        } else {
            noDrawsSequence.add(-1);
        }

        //possible to get season too
        String selectedCompetition = document.getElementsByAttribute("selected").get(1).childNode(0).toString();

        returnMap.put("competition", selectedCompetition.substring(0, selectedCompetition.indexOf('-')));
        returnMap.put("drawRate", Utils.beautifyDoubleValue(100*totalDraws/totalMatches));
        returnMap.put("noDrawsSeq", noDrawsSequence.toString());
        returnMap.put("totalDraws", totalDraws);
        returnMap.put("totalMatches", totalMatches);

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noDrawsSequence));
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
