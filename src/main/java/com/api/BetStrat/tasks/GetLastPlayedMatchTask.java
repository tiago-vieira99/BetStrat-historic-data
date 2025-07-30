package com.api.BetStrat.tasks;


import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.NoNextMatchException;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.util.ScrappingUtil;
import com.api.BetStrat.util.TelegramBotNotifications;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.api.BetStrat.constants.BetStratConstants.CURRENT_SUMMER_SEASON;
import static com.api.BetStrat.constants.BetStratConstants.CURRENT_WINTER_SEASON;
import static com.api.BetStrat.constants.BetStratConstants.LEAGUES_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_BEGIN_MONTH_LIST;

@Slf4j
@Service
public class GetLastPlayedMatchTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetLastPlayedMatchTask.class);

    // one instance, reuse
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 5 * * *", zone="Europe/Lisbon") //every two days at 5am
    public void execCronn() {

        List<String> teamsToGetLastMatch = new ArrayList<>();

//        JSONObject leagueTeamsScrappingData = ScrappingUtil.getLeagueTeamsScrappingData(LEAGUES_LIST);
//
//        while (leagueTeamsScrappingData.keys().hasNext()) {
//            String team = leagueTeamsScrappingData.keys().next().toString();
//            teamsToGetLastMatch.add(team);
//            leagueTeamsScrappingData.remove(team);
//        }

        run(teamRepository, historicMatchRepository, teamsToGetLastMatch);
    }

    @SneakyThrows
    public static void run(TeamRepository teamRepository, HistoricMatchRepository historicMatchRepository, List<String> teams) throws NoNextMatchException {
        LOGGER.info("GetLastPlayedMatchTask() at "+ Instant.now().toString());

        int numNewMatches = 0;
        String failedTeams = "";

        Map<String, Map> teamsUrls = new HashMap<>();

        List<Team> teams2 = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());

        // build teamsUrls list
        for (Team team : teams2) {
            if (team != null) {
                String newSeason = "";

                if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
                    newSeason = "20" + CURRENT_WINTER_SEASON.split("-")[1];
                } else {
                    newSeason = CURRENT_SUMMER_SEASON;
                }

                String newUrl = "";
                if (team.getUrl().contains("world")) {
                    newUrl = team.getUrl() + "/" + newSeason + "/3/";
                } else {
                    newUrl = team.getUrl();
                }
                HashMap<String, String> teamInfoMap = new HashMap<>();
                teamInfoMap.put("url", newUrl);
                teamInfoMap.put("season", newSeason);
                teamsUrls.put(team.getName(), teamInfoMap);
            }
        }

        JSONObject scrappingData = ScrappingUtil.getLastNMatchesScrappingService(teamsUrls, 3);

        // 1. Extract Keys from JSONObject
        Set<String> scrappingDataKeys = new HashSet<>();
        Iterator<String> keys = scrappingData.keys();
        while (keys.hasNext()) {
            scrappingDataKeys.add(keys.next());
        }

        // 2. Iterate and Compare
        for (Team team : teams2) {
            if (!scrappingDataKeys.contains(team.getName())) {
                failedTeams += team.getName() + ",";
            }
        }

        for (Iterator it = scrappingData.sortedKeys(); it.hasNext(); ) {
            String key = it.next().toString();
            Team team = teams2.stream().filter(t -> t.getName().equals(key)).findFirst().get();
            JSONArray lastMatches = (JSONArray) ((JSONObject) scrappingData.get(key)).get("lastMatches");

            for (int i = 0; i < lastMatches.length(); i++) {
                HistoricMatch historicMatch = new HistoricMatch();
                try {
                    JSONObject match = (JSONObject) lastMatches.get(i);
                    historicMatch.setTeamId(team);
                    historicMatch.setMatchDate(match.getString("date"));
                    historicMatch.setHomeTeam(match.getString("homeTeam"));
                    historicMatch.setAwayTeam(match.getString("awayTeam"));
                    historicMatch.setFtResult(match.getString("ftResult"));
                    historicMatch.setHtResult(match.getString("htResult"));
                    historicMatch.setCompetition(match.getString("competition"));
                    historicMatch.setSport(team.getSport());
                    if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
                        historicMatch.setSeason(CURRENT_WINTER_SEASON);
                    } else {
                        historicMatch.setSeason(CURRENT_SUMMER_SEASON);
                    }

                    historicMatchRepository.save(historicMatch);
                    log.info("Inserted match:  " + historicMatch.toString());
                    numNewMatches++;
                } catch (DataIntegrityViolationException cerr) {
                    log.debug("match:  " + historicMatch.toString() + " already exists!");
                } catch (Exception e) {
                    log.error("match:  " + historicMatch.toString() + "\nerror:  " + e.toString());
                    failedTeams += team.getName() + " | ";
                }
            }
        }

        String telegramMessage = String.format("\u2139\uFE0F number of new matches added: " + numNewMatches + "\nfailed: " + failedTeams);
        Thread.sleep(1000);
        TelegramBotNotifications.sendToTelegram(telegramMessage);
    }

}
