package com.api.BetStrat.util;

import com.api.BetStrat.entity.DrawSeasonInfo;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.*;


@Slf4j
@Service
public class ScrappingUtil {

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
