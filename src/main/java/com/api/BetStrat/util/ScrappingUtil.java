package com.api.BetStrat.util;

import com.api.BetStrat.entity.Team;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.*;


@Slf4j
@Service
public class ScrappingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrappingUtil.class);

    // one instance, reuse
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

//    public static List<DrawSeasonInfo> getNilMatches() {
//        List<DrawSeasonInfo> nilHFmatches = new ArrayList<>();
//
//        Document document = null;
//
//        try {
//            document = Jsoup.connect(SOCCERSTATS_BASE_URL).get();
//            nilHFmatches = getLiveMatches(document);
//        } catch (IOException e) {
//            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
//            log.error(e.toString());
//        }
//
//        return nilHFmatches;
//    }

//    private static List<DrawSeasonInfo> getLiveMatches(Document document) {
//
//        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//
//        Date today = new Date();
//        Date todayWithZeroTime = null;
//
//        try {
//            todayWithZeroTime = formatter.parse(formatter.format(today));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        List<DrawSeasonInfo> drawSeasonInfoList = new ArrayList<>();
//        Elements liveMatches = document.select("tr[class=odd]").select("tr[class=height:28px;]");
//
//        for (Element match : liveMatches) {
//            if (match.child(0).select("td[align=center]").size() > 0) {
//                String matchTime = match.child(0).text();
//                String matchResult = match.child(2).text();
//                try {
//                    if (matchResult.equals("0 - 0") && (matchTime.equals("HT") || (Integer.parseInt(matchTime.substring(0, matchTime.length()-1)) >= 30
//                            && Integer.parseInt(matchTime.substring(0, matchTime.length()-1)) <= 60))) {
//                        String statsURL = match.select("a[class=vsmall]").first().attr("href");
//                        if (isGoodHTMatch(statsURL)) {
//                            DrawSeasonInfo drawSeasonInfoDTO = new DrawSeasonInfo();
//                            drawSeasonInfoDTO.setDate(todayWithZeroTime);
//                            drawSeasonInfoDTO.setHomeTeam(match.child(1).text());
//                            drawSeasonInfoDTO.setAwayTeam(match.child(3).text());
//                            drawSeasonInfoList.add(drawSeasonInfoDTO);
//                        }
//                    }
//                } catch (Exception e) {
//
//                }
//
//            }
//        }
//
//        return drawSeasonInfoList;
//    }

    @SneakyThrows
    public static  JSONArray getScrappingData(String teamName, String season, String url, boolean allLeagues) {
        HttpPost request = new HttpPost(SCRAPPER_SERVICE_URL);
        JSONArray teamStatsDataObj = null;

        String queryParams = "team=" + URLEncoder.encode(teamName, "UTF-8") + "&season=" + URLEncoder.encode(season, "UTF-8") + "&allleagues=" + allLeagues;

        request.setURI(new URI(SCRAPPER_SERVICE_URL + "football-stats/all-season-matches?" + queryParams));

        // Set the request body
        String requestBody = url;
        StringEntity requestEntity = new StringEntity(requestBody);
        request.setEntity(requestEntity);

        // Set the Content-Type header
        request.setHeader("Content-Type", "text/plain");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();

            LOGGER.info("Response from ScappingService: " + response.getStatusLine().toString());

            if (entity != null) {
                // return it as a String
                String result = EntityUtils.toString(entity);
                if (result.contains("error")) {
                    LOGGER.info(result);
                }
                teamStatsDataObj = new JSONArray(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return teamStatsDataObj;
    }

    @SneakyThrows
    public static JSONObject getLeagueTeamsScrappingData(List<String> url) {
        HttpPost request = new HttpPost(SCRAPPER_SERVICE_URL);
        JSONObject teamStatsDataObj = null;

        request.setURI(new URI(SCRAPPER_SERVICE_URL + "league-teams"));

        // Set the request body
        String requestBody = String.join(System.lineSeparator(), url);
        StringEntity requestEntity = new StringEntity(requestBody);
        request.setEntity(requestEntity);

        // Set the Content-Type header
        request.setHeader("Content-Type", "text/plain");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();

            LOGGER.info("Response from ScappingService: " + response.getStatusLine().toString());

            if (entity != null) {
                // return it as a String
                String result = EntityUtils.toString(entity);
                if (result.contains("error")) {
                    LOGGER.info(result);
                }
                teamStatsDataObj = new JSONObject(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return teamStatsDataObj;
    }

    @SneakyThrows
    public static JSONObject getLastNMatchesScrappingService(Map<String, Map> teamsUrls, int numberLastMatches) {
        HttpPost request = new HttpPost(SCRAPPER_SERVICE_URL);

        // 1. Convert the Map to a JSON String
        ObjectMapper mapper = new ObjectMapper(); // Jackson library for JSON conversion
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(teamsUrls);
        } catch (Exception e) {
            System.err.println("Error converting Map to JSON: " + e.getMessage());
            throw e; // Re-throw the exception to signal failure
        }

        // 2. Create an HttpEntity from the JSON String
        HttpEntity reqEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);

        request.setURI(new URI(SCRAPPER_SERVICE_URL + "last-matches/" + numberLastMatches));
        request.setEntity(reqEntity);
        JSONObject lastNMatches = null;

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();

            LOGGER.info("Response from ScrappingService: " + response.getStatusLine().toString());

            if (entity != null) {
                // return it as a String
                String result = EntityUtils.toString(entity);
                lastNMatches = new JSONObject(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return lastNMatches;
    }

    private static boolean isGoodHTMatch(String url) {

        Document document = null;

        try {
            document = Jsoup.connect(SOCCERSTATS_BASE_URL +url).get();

        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        //evaluate avg total goals > 2.60
        Element avgTotalGoals = document.select("tr[height=24]").stream().filter(element -> element.text().contains("Avg Total Goals")).collect(Collectors.toList()).get(0);
        double avgTotalGoalsValue = Double.parseDouble(avgTotalGoals.getElementsByAttributeValueMatching("align", "center").get(1).text());

        if (avgTotalGoalsValue < 2.60) { return false;}

        //evaluate % of >1.5 matches >= 80%
        Element over15Goals = document.select("tr[height=24]").stream().filter(element -> element.text().contains("Over 1.5 goals")).collect(Collectors.toList()).get(0);
        String over15PercentStr = over15Goals.getElementsByAttributeValueMatching("align", "center").get(1).text();
        int over15Percent = Integer.parseInt(over15PercentStr.substring(0, over15PercentStr.length() - 1));

        if (over15Percent < 80) { return false;}

        return true;
    }

    public static String testDrawLeagues (String league) {
        Document document = null;

        try {
            document = Jsoup.connect("https://www.soccerstats.com/results.asp?league="+league+"&pmtype=bydate").get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        Elements allGames = document.select("table[id=btable]").get(0).getElementsByAttributeValueContaining("class", "odd").select("td[style=padding-right:5px;]");
        int flag = 0;
        if (allGames.size() == 0)
        {
            flag = 1;
            allGames = document.select("table[id=btable]").get(0).getElementsByAttributeValueContaining("class", "trow3");
        }
        if (allGames.size() == 0)
        {
            flag = 1;
            allGames = document.select("table[id=btable]").get(0).getElementsByAttributeValueContaining("class", "odd");
        }
        List<String> resultsList = new ArrayList<>();

        String result = null;
        for(Element match : allGames) {
            try {
                if (0 == flag) {
                    result = ((Element) match.parentNode()).getElementsByAttributeValue("align", "center").get(0).getAllElements().get(1).childNode(0).toString();
                } else {
                    result = match.getElementsByAttributeValue("align", "center").get(1).getAllElements().get(2).toString();
                }
                if (result.contains("-")){
                    resultsList.add(result);
                }
            } catch (IllegalStateException | IndexOutOfBoundsException i)
            {

            }
        }

        int numDraws = 0;
        int fiboSeq[] = {1,1,2,3,5,8,13,21,34,55,89,144,233,377};
        int cursor = 0;
        double stake = 0.4;
        double profit = 0.0;

        for (int i = 0; i<resultsList.size(); i++) {
            int homeGoals = Integer.parseInt(resultsList.get(i).substring(resultsList.get(i).indexOf('>')+1,resultsList.get(i).indexOf('-')-1));
            int awayGoals = Integer.parseInt(resultsList.get(i).substring(resultsList.get(i).indexOf('-')+2,resultsList.get(i).lastIndexOf('<')));
            if (homeGoals == awayGoals) {
                numDraws++;
                profit += (stake*fiboSeq[cursor]*3)-(stake*fiboSeq[cursor]);
                cursor = 0;
            } else {
                profit += -stake*fiboSeq[cursor];
                if (10 == cursor) {
                    cursor = 0;
                } else {
                    cursor++;
                }
            }
        }
        double drawRate = numDraws*1.0/resultsList.size();


        return league+": \n\t-drawRate: "+Double.toString(drawRate)+"\n\t-profit: "+Double.toString(profit);
    }

    public static String testDrawTeams (String league, String team) {
        Document document = null;

        try {
            document = Jsoup.connect("https://www.soccerstats.com/team.asp?league="+league+"&stats="+team).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        Elements allGames = document.select("tbody").select("tr[bgcolor]").select("td[align=center]").select("td[width=38]");
        if (allGames.size() == 0) {
            allGames = document.select("tbody").select("tr[bgcolor]").select("td[align=center]").select("td[width=40]");
        }

        List<String> resultsList = new ArrayList<>();

        String result = null;
        for(Element match : allGames) {
            try {
                result = match.select("td[align=center]").get(0).select("b").get(0).toString();
                if (result.contains("-")){
                    resultsList.add(result);
                }
            } catch (IllegalStateException | IndexOutOfBoundsException i)
            {

            }
        }

        int numDraws = 0;
        int fiboSeq[] = {1,1,2,3,5,8,13,21,34,55,89,144,233,377};
        int cursor = 0;
        double stake = 0.5;
        double profit = 0.0;
        List<Integer> noDrawSeries = new ArrayList<>();
        int serieCount = 0;

        for (int i = 0; i<resultsList.size(); i++) {
            int homeGoals = Integer.parseInt(resultsList.get(i).substring(resultsList.get(i).indexOf('>')+1,resultsList.get(i).indexOf('-')-1));
            int awayGoals = Integer.parseInt(resultsList.get(i).substring(resultsList.get(i).indexOf('-')+2,resultsList.get(i).lastIndexOf('<')));
            if (homeGoals == awayGoals) {
                noDrawSeries.add(serieCount);
                serieCount = 0;
                numDraws++;
                profit += (stake*fiboSeq[cursor]*3)-(stake*fiboSeq[cursor]);
                cursor = 0;
            } else {
                serieCount++;
                profit += -stake*fiboSeq[cursor];
                if (10 == cursor) {
                    cursor = 0;
                } else {
                    cursor++;
                }
            }
        }
        double drawRate = numDraws*1.0/resultsList.size();


        return league+" - "+team+": \n\t-drawRate: "+Double.toString(drawRate)+"\n\t-profit: "+Double.toString(profit)+"\n\t-noDrawseries: "+noDrawSeries.toString();
    }

}
