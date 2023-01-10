package com.api.BetStrat.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamDFhistoricDataZZ {

    private double mean = this.mean;

    public LinkedHashMap<String, Object> extractDFData(String url) {
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
//        returnMap.put("profit", potencialProfit(noDrawsSequence));
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

        returnMap.put("profit", potencialProfit(sequenceArray));

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(sequenceArray));
        returnMap.put("standardDeviaiton", String.valueOf(stdDev));
        returnMap.put("coefficientVariation", String.valueOf(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev))));

        return returnMap;
    }

    private String potencialProfit (ArrayList<Integer> noDrawsSequence) {
        int avgOdd = 3;
        int[] fiboSeq = {1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987};
        double initialStake = 0.5;

        int[] teamNoDrawsSeq = noDrawsSequence.stream().mapToInt(i -> i).toArray();

        double totalProfit = 0.0;
        for (int i = 0; i<teamNoDrawsSeq.length-1; i++) {

            int numJogosNoDraw = teamNoDrawsSeq[i];
            for (int j = 0; j<numJogosNoDraw;j++) {
                if (8==j) {
                    numJogosNoDraw=numJogosNoDraw-j;
                    for (j = 0; j<numJogosNoDraw;j++) {
                        //quando chega ao jogo do empate,
                        if ((j+1 == numJogosNoDraw)) {
                            //se for a ultima seq da época e o ultimo jogo não é empate, calcula prejuizo
                            if (teamNoDrawsSeq[i+1] == -1){
                                totalProfit = totalProfit - (fiboSeq[j]*initialStake);
                                continue;
                            }
                            totalProfit += (initialStake*avgOdd*fiboSeq[j])-initialStake*fiboSeq[j];

                        } else {
                            totalProfit += (-fiboSeq[j]*initialStake);
                        }

                    }
                    break;
                }

                //quando chega ao jogo do empate,
                if ((j+1 == numJogosNoDraw)) {
                    //se for a ultima seq da época e o ultimo jogo não é empate, calcula prejuizo
                    if (teamNoDrawsSeq[i+1] == -1){
                        totalProfit = totalProfit - (fiboSeq[j]*initialStake);
                        continue;
                    }
                    totalProfit += (initialStake*avgOdd*fiboSeq[j])-initialStake*fiboSeq[j];

                } else {
                    totalProfit += (-fiboSeq[j]*initialStake);
                }

            }
        }
        return String.valueOf(totalProfit);
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
