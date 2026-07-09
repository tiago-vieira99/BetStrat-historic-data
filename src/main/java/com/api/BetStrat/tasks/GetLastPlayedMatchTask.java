package com.api.BetStrat.tasks;


import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.NoNextMatchException;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.util.ScrappingUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.api.BetStrat.constants.BetStratConstants.CURRENT_SUMMER_SEASON;
import static com.api.BetStrat.constants.BetStratConstants.CURRENT_WINTER_SEASON;
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

    //@EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 1 * * *", zone="Europe/Lisbon") //every two days at 1am
    public void execCronn() {

        List<String> teamsToGetLastMatch = new ArrayList<>();

        run(teamRepository, historicMatchRepository, teamsToGetLastMatch);
    }

    @Scheduled(cron = "0 0 8 * * MON", zone="Europe/Lisbon") //every Monday at 8am
    public void execForceUpdate() {
        forceUpdateLastResults(historicMatchRepository);
    }

    @SneakyThrows
    public static void run(TeamRepository teamRepository, HistoricMatchRepository historicMatchRepository, List<String> teams) throws NoNextMatchException {
        LOGGER.info("GetLastPlayedMatchTask() at "+ Instant.now().toString());

        Map<String, Map> teamsUrls = new HashMap<>();

        List<Team> teams2 = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football") && t.getAdmin()).collect(Collectors.toList());

        // build teamsUrls list
        for (Team team : teams2) {
            if (team != null) {
                String newSeason = "";

                if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
                    newSeason = CURRENT_WINTER_SEASON;
                } else {
                    newSeason = CURRENT_SUMMER_SEASON;
                }

                HashMap<String, String> teamInfoMap = new HashMap<>();
                teamInfoMap.put("url", team.getUrl());
                teamInfoMap.put("season", newSeason);
                teamsUrls.put(team.getName(), teamInfoMap);
            }
        }

        JSONObject scrappingData = ScrappingUtil.getLastNMatchesScrappingService(teamsUrls, 3);
    }

    public static void forceUpdateLastResults(HistoricMatchRepository historicMatchRepository) {
        LOGGER.info("forceUpdateLastResults() at "+ Instant.now().toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate today = LocalDate.now();

        List<HistoricMatch> outdatedMatches = historicMatchRepository.getUpcomingMatches()
            .stream()
            .filter(match -> LocalDate.parse(match.getMatchDate(), formatter).isBefore(today))
            .collect(Collectors.toList());

        Map<String, Map> teamsUrls = new HashMap<>();

        for (HistoricMatch outdatedMatch : outdatedMatches) {
            Team team = outdatedMatch.getTeamId();
            if (team != null) {
                String newSeason = "";

                if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
                    newSeason = CURRENT_WINTER_SEASON;
                } else {
                    newSeason = CURRENT_SUMMER_SEASON;
                }

                HashMap<String, String> teamInfoMap = new HashMap<>();
                teamInfoMap.put("url", outdatedMatch.getUrl());
                teamInfoMap.put("season", newSeason);
                teamsUrls.put(team.getName(), teamInfoMap);
            }
        }

        JSONObject scrappingData = ScrappingUtil.getSpecificNMatchesScrappingService(teamsUrls);
    }

}
