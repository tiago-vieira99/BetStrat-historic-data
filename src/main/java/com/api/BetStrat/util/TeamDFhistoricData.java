package com.api.BetStrat.util;

import com.api.BetStrat.entity.DrawSeasonInfo;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.FCSTATS_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.HOCKEY_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.FOOTBALL_SEASONS_LIST;

@Slf4j
@Service
public class TeamDFhistoricData {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamDFhistoricData.class);

    private double mean = this.mean;

    @SneakyThrows
    public DrawSeasonInfo buildSeasonDFStatsData(JSONArray allMatches) {
        DrawSeasonInfo drawSeasonInfo = new DrawSeasonInfo();

        ArrayList<Integer> noDrawsSequence = new ArrayList<>();
        int count = 0;
        for (int i=0; i < allMatches.length(); i++) {
            JSONObject match = (JSONObject) allMatches.get(i);
            String res = match.getString("ftResult");
            count++;
            if (res.split(":")[0].equals(res.split(":")[1])) {
                noDrawsSequence.add(count);
                count = 0;
            }
        }

        int totalDraws = noDrawsSequence.size();

        noDrawsSequence.add(count);
        String lastResult = ((JSONObject) allMatches.get(allMatches.length() - 1)).getString("ftResult");
        if (!lastResult.split(":")[0].equals(lastResult.split(":")[1])) {
            noDrawsSequence.add(-1);
        }

        String selectedCompetition = ((JSONObject) allMatches.get(0)).getString("competition");

        drawSeasonInfo.setCompetition(selectedCompetition);
        drawSeasonInfo.setDrawRate(Utils.beautifyDoubleValue(100*totalDraws/allMatches.length()));
        drawSeasonInfo.setNoDrawsSequence(noDrawsSequence.toString());
        drawSeasonInfo.setNumDraws(totalDraws);
        drawSeasonInfo.setNumMatches(allMatches.length());

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noDrawsSequence));
        drawSeasonInfo.setStdDeviation(stdDev);
        drawSeasonInfo.setCoefDeviation(Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

        return drawSeasonInfo;
    }

    public LinkedHashMap<String, Object> extractDFDataFromZZ(String url) {
        Document document = null;
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();
        LOGGER.info("Scraping data: " + url);

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
        LOGGER.info("Scraping data: " + url);

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
        if (!allMatches.get(allMatches.size()-1).childNodes().stream().filter(m -> m.attributes().get("class").equals("boxIcon"))
                .collect(Collectors.toList()).get(0).toString().contains("IconD")) {
            noDrawsSequence.add(-1);
        }

        //possible to get season too
        String selectedCompetition = document.getElementsByAttribute("selected").get(1).childNode(0).toString();

        returnMap.put("competition", selectedCompetition);
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

    public LinkedHashMap<String, Object> extractDFDataFromLastSeasonsFCStats(String teamUrl) {
        Document document = null;
        LOGGER.info("Scraping data: " + teamUrl);

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
            if (FOOTBALL_SEASONS_LIST.contains(season)) {
                String seasonURL = FCSTATS_BASE_URL + "club,matches," + teamUrlName + "," + teamId + "," + seasonID + ".php";
                returnMap.put(season, extractDFDataFromFC(seasonURL));
            }
        }

        return returnMap;
    }

    public LinkedHashMap<String, Object> extractHockeyDFDataFromLastSeasons(String teamUrl) {
        Document originalDocument = null;
        LOGGER.info("Scraping data: " + teamUrl);

        try {
            originalDocument = Jsoup.connect(teamUrl).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }
//next match
//        document.getElementsByAttributeValueContaining("class", "table-games").get(0).child(1).childNodes().stream().filter(c -> c.hasAttributes()).collect(Collectors.toList()).get(0)


        //link to matches page
        String matchesPageLink = originalDocument.getElementsByAttributeValueContaining("class", "table-games").get(0).parent().nextElementSibling().attributes().get("href");

        Document document = null;
        try {
            document = Jsoup.connect(matchesPageLink).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }


        //result in format '0 - 4'
        //matchesList.get(1).childNode(7).childNode(1).childNode(0).toString()

        //date in format ' 2023-02-24T18:00:00+0100 '
        //matchesList.get(1).childNode(1).childNode(0).toString()

        //homeTeam
        //matchesList.get(1).childNode(3).childNode(3).childNode(1).childNode(0).toString().trim()

        //awayTeam
        //matchesList.get(1).childNode(5).childNode(3).childNode(1).childNode(0).toString().trim()

        List<Element> availableSeasons = document.getElementsByAttributeValueContaining("title", "Select Season").get(0).getElementsByAttribute("value");

        LinkedHashMap<String, Object> returnMap = new LinkedHashMap<>();
        for (int i = 0; i < availableSeasons.size(); i++) {
            String season = availableSeasons.get(i).childNode(0).toString();
            if (HOCKEY_SEASONS_LIST.contains(season)) {
                String seasonURL = matchesPageLink.replaceFirst("(?<=/games/)[^/]+(?=/all)", season);
                returnMap.put(season, extractHockeyDFData(seasonURL));
                System.out.println();
            }
        }

        return returnMap;
    }

    public LinkedHashMap<String, Object> extractHockeyDFData(String url) {
        Document document = null;
        LinkedHashMap<String,Object> returnMap = new LinkedHashMap<>();

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Node> allMatches = document.getElementsByAttributeValueContaining("class", "table-games").get(0).child(1).childNodes().stream().filter(c -> c.childNodes().size() > 3).collect(Collectors.toList());
        int totalMatches = allMatches.size();
        Collections.reverse(allMatches);

        ArrayList<Integer> noDrawsSequence = new ArrayList<>();
        int count = 0;
        for (Node match : allMatches) {
            count++;
            if (match.toString().contains("(OT") || match.toString().contains("(SO")) {
                noDrawsSequence.add(count);
                count = 0;
            }
        }

        int totalDraws = noDrawsSequence.size();

        noDrawsSequence.add(count);
        if (!(allMatches.get(allMatches.size()-1).toString().contains("(OT") || allMatches.get(allMatches.size()-1).toString().contains("(SO"))) {
            noDrawsSequence.add(-1);
        }

        String selectedCompetition = allMatches.get(0).childNode(9).childNode(1).childNode(0).toString().trim();

        returnMap.put("competition", selectedCompetition);
        returnMap.put("drawRate", Utils.beautifyDoubleValue(100*totalDraws/totalMatches));
        returnMap.put("noDrawsSeq", noDrawsSequence.toString());
        returnMap.put("totalDraws", totalDraws);
        returnMap.put("totalMatches", totalMatches);

        double stdDev =  Utils.beautifyDoubleValue(calculateSD(noDrawsSequence));
        returnMap.put("standardDeviation", stdDev);
        returnMap.put("coefficientVariation", Utils.beautifyDoubleValue(calculateCoeffVariation(stdDev)));

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
