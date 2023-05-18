package com.api.BetStrat.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BasketballScrappingData {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasketballScrappingData.class);

    private double mean = this.mean;

    @SneakyThrows
    public LinkedHashMap<String, Object> extractNBAFromBref(String season) {
        Document document = null;
        LinkedHashMap<String, Object> returnMap = new LinkedHashMap<>();
        LOGGER.info("Scraping data: " + season);

        List<String> months = Arrays.asList("october", "november", "december", "january", "february", "march", "april", "may", "june");
        List<Node> allMatches = new ArrayList<>();

        for (String month : months) {
            try {
                document = Jsoup.connect(String.format("https://www.basketball-reference.com/leagues/NBA_%s_games-%s.html", season, month)).get();
            } catch (IOException e) {
                log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
                log.error(e.toString());
            }

            allMatches.addAll(document.getElementsByAttributeValue("id", "schedule").get(0).childNode(7).childNodes().stream().filter(c -> c.siblingIndex() % 2 == 0).collect(Collectors.toList()));
        }

        int numComebacks = 0;
        for (Node match : allMatches) {
            Thread.sleep(3500);
            String matchUrl = match.childNodes().stream().filter(m -> m.attributes().get("data-stat").equals("box_score_text")).collect(Collectors.toList()).get(0).childNode(0).attributes().get("href");
            String homeTeam = match.childNodes().stream().filter(m -> m.attributes().get("data-stat").equals("home_team_name")).collect(Collectors.toList()).get(0).childNode(0).childNode(0).toString();
            String awayTeam = match.childNodes().stream().filter(m -> m.attributes().get("data-stat").equals("visitor_team_name")).collect(Collectors.toList()).get(0).childNode(0).childNode(0).toString();
            String gameDate = match.childNodes().stream().filter(m -> m.attributes().get("data-stat").equals("date_game")).collect(Collectors.toList()).get(0).childNode(0).childNode(0).toString();
            System.out.println(gameDate + ": " + homeTeam + " - " + awayTeam);
            try {
                document = Jsoup.connect("https://www.basketball-reference.com" + matchUrl.replace("boxscores", "boxscores/pbp")).get();
            } catch (IOException e) {
                log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
                log.error(e.toString());
            }

            try {
                List<Node> pointsElements = document.getElementsByAttributeValue("id", "pbp").get(0).childNode(3).childNodes().stream().filter(c -> c.siblingIndex() % 2 == 0).collect(Collectors.toList());

                Node firstQuarterPoints = pointsElements.stream().filter(p -> p.toString().contains(">End of")).collect(Collectors.toList()).get(0).previousSibling().previousSibling();
                Node secondQuarterPoints = pointsElements.stream().filter(p -> p.toString().contains(">End of")).collect(Collectors.toList()).get(1).previousSibling().previousSibling();
                Node thirdQuarterPoints = pointsElements.stream().filter(p -> p.toString().contains(">End of")).collect(Collectors.toList()).get(2).previousSibling().previousSibling();
                Node fourthQuarterPoints = pointsElements.stream().filter(p -> p.toString().contains(">End of")).collect(Collectors.toList()).get(3).previousSibling().previousSibling();

                int home_1Qpoints = Integer.parseInt(firstQuarterPoints.childNode(5).childNode(0).toString().split("-")[0]);
                int away_1Qpoints = Integer.parseInt(firstQuarterPoints.childNode(5).childNode(0).toString().split("-")[1]);

                int home_2Qpoints = Integer.parseInt(secondQuarterPoints.childNode(5).childNode(0).toString().split("-")[0]) - home_1Qpoints;
                int away_2Qpoints = Integer.parseInt(secondQuarterPoints.childNode(5).childNode(0).toString().split("-")[1]) - away_1Qpoints;

                int home_3Qpoints = Integer.parseInt(thirdQuarterPoints.childNode(5).childNode(0).toString().split("-")[0]) - home_1Qpoints - home_2Qpoints;
                int away_3Qpoints = Integer.parseInt(thirdQuarterPoints.childNode(5).childNode(0).toString().split("-")[1]) - away_1Qpoints - away_2Qpoints;

                int home_4Qpoints = Integer.parseInt(fourthQuarterPoints.childNode(5).childNode(0).toString().split("-")[0]) - home_1Qpoints - home_2Qpoints - home_3Qpoints;
                int away_4Qpoints = Integer.parseInt(fourthQuarterPoints.childNode(5).childNode(0).toString().split("-")[1]) - away_1Qpoints - away_2Qpoints - away_3Qpoints;

                int firstHalfHomePoints = home_1Qpoints + home_2Qpoints;
                int firstHalfAwayPoints = away_1Qpoints + away_2Qpoints;
                int totalHomePoints = home_1Qpoints + home_2Qpoints + home_3Qpoints + home_4Qpoints;
                int totalAwayPoints = away_1Qpoints + away_2Qpoints + away_3Qpoints + away_4Qpoints;

                if ((firstHalfHomePoints > firstHalfAwayPoints && totalHomePoints < totalAwayPoints) || (firstHalfHomePoints < firstHalfAwayPoints && totalHomePoints > totalAwayPoints)) {
                    System.out.println("COMEBACK -> " + gameDate + ": " + homeTeam + " - " + awayTeam);
                    numComebacks++;
                }
            } catch (Exception e) {
                log.error(e.toString());
                System.out.println("Error Match -> " + gameDate + ": " + homeTeam + " - " + awayTeam);
            }
        }
        System.out.println("num of comeback -> " + numComebacks);

        return returnMap;
    }

    @SneakyThrows
    public LinkedHashMap<String, Object> extractNBAComebacksFromESPN(String url) {
        Document document = null;
        LinkedHashMap<String, Object> returnMap = new LinkedHashMap<>();
        LOGGER.info("Scraping data: " + url);

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Element> allMatches = document.getElementsByAttributeValueContaining("class", "Table__even").stream().filter(l -> l.childNodeSize() > 1 && !l.childNode(0).toString().contains("DATE")).collect(Collectors.toList());

        try {
            document = Jsoup.connect(url.replace("seasontype/2", "seasontype/3")).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }
        List<Element> playOffMatches = document.getElementsByAttributeValueContaining("class", "Table__even").stream().filter(l -> l.childNodeSize() > 1 && !l.childNode(0).toString().contains("DATE")).collect(Collectors.toList());
        allMatches.addAll(playOffMatches);

        ArrayList<Integer> noComebacksSequence = new ArrayList<>();
        int count = 0;
        int numWins = 0;
        for (Element match : allMatches) {
            if (match.toString().contains("Postponed")) {
                continue;
            }
            String gameDate = match.childNode(0).childNode(0).childNode(0).toString();
            System.out.println(gameDate);
            Thread.sleep(300);
//            boolean isAwayMatch = document.getElementsByAttributeValueContaining("class", "Table__even").get(2).childNode(1).childNode(0).childNode(0).childNode(0).toString().equals("@");
            String result = match.childNode(2).childNode(0).childNode(0).toString();
            String matchUrl = match.childNode(2).childNode(1).childNode(0).attr("href").toString();

            count++;
            if (result.equals("W")) {
                numWins++;
                if (isComebackWinESPN(matchUrl)) {
                    noComebacksSequence.add(count);
                    count = 0;
                }
            }
        }

        returnMap.put("competition", "NBA");

        if (isComebackWinESPN(allMatches.get(allMatches.size()-1).childNode(2).childNode(1).childNode(0).attr("href").toString())) {
            noComebacksSequence.add(0);
        } else {
            noComebacksSequence.add(count);
            noComebacksSequence.add(-1);
        }

        int totalMatches = allMatches.size();
        double comebacksRate = 100*(noComebacksSequence.size() - 1) / Double.valueOf(allMatches.size());
        double winsRate = 100*numWins / Double.valueOf(allMatches.size());

        returnMap.put("comebacksRate", Utils.beautifyDoubleValue(comebacksRate));
        returnMap.put("winsRate", Utils.beautifyDoubleValue(winsRate));
        returnMap.put("noComebacksSequence", noComebacksSequence.toString());
        returnMap.put("totalComebacks", noComebacksSequence.size()-1);
        returnMap.put("totalWins", numWins);
        returnMap.put("totalMatches", totalMatches);

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noComebacksSequence));
        returnMap.put("standardDeviation", stdDev);
        returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return returnMap;
    }

    private boolean isComebackWinESPN (String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        int home_1Qpoints = Integer.parseInt(document.getElementsByAttributeValueContaining("class", "Table__TR--sm").get(0).childNode(1).childNode(0).toString());
        int away_1Qpoints = Integer.parseInt(document.getElementsByAttributeValueContaining("class", "Table__TR--sm").get(1).childNode(1).childNode(0).toString());

        int home_2Qpoints = Integer.parseInt(document.getElementsByAttributeValueContaining("class", "Table__TR--sm").get(0).childNode(2).childNode(0).toString());
        int away_2Qpoints = Integer.parseInt(document.getElementsByAttributeValueContaining("class", "Table__TR--sm").get(1).childNode(2).childNode(0).toString());

        int home_3Qpoints = Integer.parseInt(document.getElementsByAttributeValueContaining("class", "Table__TR--sm").get(0).childNode(3).childNode(0).toString());
        int away_3Qpoints = Integer.parseInt(document.getElementsByAttributeValueContaining("class", "Table__TR--sm").get(1).childNode(3).childNode(0).toString());

        int home_4Qpoints = Integer.parseInt(document.getElementsByAttributeValueContaining("class", "Table__TR--sm").get(0).childNode(4).childNode(0).toString());
        int away_4Qpoints = Integer.parseInt(document.getElementsByAttributeValueContaining("class", "Table__TR--sm").get(1).childNode(4).childNode(0).toString());

        int firstHalfHomePoints = home_1Qpoints + home_2Qpoints;
        int firstHalfAwayPoints = away_1Qpoints + away_2Qpoints;
        int totalHomePoints = home_1Qpoints + home_2Qpoints + home_3Qpoints + home_4Qpoints;
        int totalAwayPoints = away_1Qpoints + away_2Qpoints + away_3Qpoints + away_4Qpoints;

        if ((firstHalfHomePoints > firstHalfAwayPoints && totalHomePoints < totalAwayPoints) || (firstHalfHomePoints < firstHalfAwayPoints && totalHomePoints > totalAwayPoints)) {
            return true;
        }

        return false;
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
