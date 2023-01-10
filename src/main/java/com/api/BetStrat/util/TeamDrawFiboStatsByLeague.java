package com.api.BetStrat.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamDrawFiboStatsByLeague {

    public HashMap<String, Map> findFirstDrawsByLeague(String leagueURL) {
        Document document = null;
//        HashMap<String,Integer> candidateTeamsBySeasonMap = new HashMap<>();

//        String leagueURL = "https://fcstats.com/table,nemzeti-bajnoksag-i-hungary,66,1.php";

        try {
            document = Jsoup.connect(leagueURL).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Element> availableSeasons = document.getElementsByAttributeValueContaining("class", "league_select_phase").stream().collect(Collectors.toList());
        Collections.reverse(availableSeasons);

        HashMap<String, Map> returnMap = new LinkedHashMap<>();
        for (int i = 1; (i < availableSeasons.size()) && (i < 7); i++) {
            String seasonID = availableSeasons.get(i).getElementsByAttribute("value").get(0).attributes().get("value");
            String season = availableSeasons.get(i).attributes().get("id").substring(7);
            String seasonLeagueURL = leagueURL.substring(0, leagueURL.lastIndexOf('1')+1) + "," + seasonID + ".php";
            returnMap.put(season, navigateToOtherSeasonPage(seasonLeagueURL));
        }

        return returnMap;
    }

    private HashMap<String, String> navigateToOtherSeasonPage(String seasonLeagueURL) {
        Document document = null;
        HashMap<String, String> firstDrawRoundByTeamMap = new HashMap<>();

        try {
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setUseInsecureSSL(true);
            webClient.getCookieManager().setCookiesEnabled(true);
            webClient.getOptions().setRedirectEnabled(true);

            // Get the first page
            HtmlPage firstLoadedPage = webClient.getPage(seasonLeagueURL);

            // Get the form that we are dealing with and within that form,
            // find the submit button and the field that we want to change.
            List<HtmlForm> form = firstLoadedPage.getForms();
            HtmlForm htmlForm = form.get(0);
            HtmlSelect custom_typeSelection = htmlForm.getSelectByName("custom_type");
            custom_typeSelection.setSelectedAttribute("first", true);
            HtmlInput custom_amount = htmlForm.getInputByName("custom_amount");

            int numTeams = 0;
            int roundNumber = 1;
            do {
                custom_amount.setValue(String.valueOf(roundNumber));

                HtmlSubmitInput showButton = htmlForm.getInputByValue("Show");

                // Now submit the form by clicking the button and get back the reloaded page.
                HtmlPage reloadedPage = showButton.click();
                List<HtmlElement> tableBody = reloadedPage.getByXPath("/html[1]/body[1]/div[1]/div[6]/div[2]/div[1]/div[4]/div[6]/table[1]/tbody[1]");
//                List<HtmlElement> tableBody = reloadedPage.getByXPath("/html[1]/body[1]/div[1]/div[6]/div[2]/div[1]/div[3]/div[6]/table[1]/tbody[1]");
                HtmlTableBody htmlTableBody = (HtmlTableBody) tableBody.get(0);
                List<DomNode> tableLines = htmlTableBody.getChildNodes().stream().filter(t -> t.hasChildNodes()).collect(Collectors.toList());
                numTeams = tableLines.size();

                for (int i = 0; i < tableLines.size(); i++) {
                    DomNode line = tableLines.get(i);
                    HtmlTableDataCell teamNameCell = (HtmlTableDataCell) line.getByXPath("td[2]").get(0);
                    String teamName = ((DomText) teamNameCell.getFirstChild().getFirstChild()).getData();
                    HtmlTableDataCell drawsCell = (HtmlTableDataCell) line.getByXPath("td[5]").get(0);
                    String drawsValue = ((DomText) drawsCell.getFirstChild()).getData();

                    if (!firstDrawRoundByTeamMap.containsKey(teamName) && !drawsValue.equals("0")) {
                        firstDrawRoundByTeamMap.put(teamName, String.valueOf(roundNumber));
                    }
                }

                roundNumber++;
            } while (numTeams != firstDrawRoundByTeamMap.size() && (roundNumber <= 25));


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            document = Jsoup.connect(seasonLeagueURL).get();
//        } catch (IOException e) {
//            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
//            log.error(e.toString());
//        }
//
//        Elements allTeams = document.getElementsByAttributeValueContaining("class", "teamNameRow");
//
//        for (Element team : allTeams) {
//            double numMatches = Double.parseDouble(team.getElementsByTag("td").get(2).text());
//            double numDraws = Double.parseDouble(team.getElementsByTag("td").get(4).text());
//            double drawsRate = (numDraws/numMatches)*100;
//
//            if (drawsRate > 25) {
//                candidateSeasonTeams.add(team.getElementsByAttributeValueContaining("class", "teamName").get(1).text());
//            }
//        }

        return firstDrawRoundByTeamMap;
    }

    public HashMap<String, Map> findFirstDrawsByLeague2(String leagueURL) {
        Document document = null;
//        HashMap<String,Integer> candidateTeamsBySeasonMap = new HashMap<>();

//        String leagueURL = "https://fcstats.com/table,nemzeti-bajnoksag-i-hungary,66,1.php";

        try {
            document = Jsoup.connect(leagueURL).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Element> availableSeasons = document.getElementsByAttributeValueContaining("class", "league_select_phase").stream().collect(Collectors.toList());
        Collections.reverse(availableSeasons);

        HashMap<String, Map> returnMap = new LinkedHashMap<>();
        for (int i = 1; (i < availableSeasons.size()) && (i < 7); i++) {
            String seasonID = availableSeasons.get(i).getElementsByAttribute("value").get(0).attributes().get("value");
            String season = availableSeasons.get(i).attributes().get("id").substring(7);
            String seasonLeagueURL = leagueURL.substring(0, leagueURL.lastIndexOf('1')+1) + "," + seasonID + ".php";
            returnMap.put(season, navigateToOtherSeasonPage2(seasonLeagueURL));
        }

        return returnMap;
    }

    private HashMap<String, String> navigateToOtherSeasonPage2(String seasonLeagueURL) {
        Document document = null;
        HashMap<String, String> firstDrawRoundByTeamMap = new HashMap<>();

        try {
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setUseInsecureSSL(true);
            webClient.getCookieManager().setCookiesEnabled(true);
            webClient.getOptions().setRedirectEnabled(true);

            // Get the first page
            HtmlPage firstLoadedPage = webClient.getPage(seasonLeagueURL);

            // Get the form that we are dealing with and within that form,
            // find the submit button and the field that we want to change.
            List<HtmlForm> form = firstLoadedPage.getForms();
            HtmlForm htmlForm = form.get(0);
            HtmlSelect custom_typeSelection = htmlForm.getSelectByName("custom_type");
            custom_typeSelection.setSelectedAttribute("first", true);
            HtmlInput custom_amount = htmlForm.getInputByName("custom_amount");

            int numTeams = 0;
            int roundNumber = 1;
            do {
                custom_amount.setValue(String.valueOf(roundNumber));

                HtmlSubmitInput showButton = htmlForm.getInputByValue("Show");

                // Now submit the form by clicking the button and get back the reloaded page.
                HtmlPage reloadedPage = showButton.click();
//                List<HtmlElement> tableBody = reloadedPage.getByXPath("/html[1]/body[1]/div[1]/div[6]/div[2]/div[1]/div[4]/div[6]/table[1]/tbody[1]");
                List<HtmlElement> tableBody = reloadedPage.getByXPath("/html[1]/body[1]/div[1]/div[6]/div[2]/div[1]/div[3]/div[6]/table[1]/tbody[1]");
                HtmlTableBody htmlTableBody = (HtmlTableBody) tableBody.get(0);
                List<DomNode> tableLines = htmlTableBody.getChildNodes().stream().filter(t -> t.hasChildNodes()).collect(Collectors.toList());
                numTeams = tableLines.size();

                for (int i = 0; i < tableLines.size(); i++) {
                    DomNode line = tableLines.get(i);
                    HtmlTableDataCell teamNameCell = (HtmlTableDataCell) line.getByXPath("td[2]").get(0);
                    String teamName = ((DomText) teamNameCell.getFirstChild().getFirstChild()).getData();
                    HtmlTableDataCell drawsCell = (HtmlTableDataCell) line.getByXPath("td[5]").get(0);
                    String drawsValue = ((DomText) drawsCell.getFirstChild()).getData();

                    if (!firstDrawRoundByTeamMap.containsKey(teamName) && !drawsValue.equals("0")) {
                        firstDrawRoundByTeamMap.put(teamName, String.valueOf(roundNumber));
                    }
                }

                roundNumber++;
            } while (numTeams != firstDrawRoundByTeamMap.size() && (roundNumber <= 25));


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            document = Jsoup.connect(seasonLeagueURL).get();
//        } catch (IOException e) {
//            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
//            log.error(e.toString());
//        }
//
//        Elements allTeams = document.getElementsByAttributeValueContaining("class", "teamNameRow");
//
//        for (Element team : allTeams) {
//            double numMatches = Double.parseDouble(team.getElementsByTag("td").get(2).text());
//            double numDraws = Double.parseDouble(team.getElementsByTag("td").get(4).text());
//            double drawsRate = (numDraws/numMatches)*100;
//
//            if (drawsRate > 25) {
//                candidateSeasonTeams.add(team.getElementsByAttributeValueContaining("class", "teamName").get(1).text());
//            }
//        }

        return firstDrawRoundByTeamMap;
    }

    private void navigateToMatchesPage(String teamMatchesURL) {
        Document document = null;

        try {
            document = Jsoup.connect("https://fcstats.com/"+teamMatchesURL).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Element> seasonMatches = document.getElementsByAttributeValueContaining("class", "matchRow").stream().collect(Collectors.toList());
    }
}
