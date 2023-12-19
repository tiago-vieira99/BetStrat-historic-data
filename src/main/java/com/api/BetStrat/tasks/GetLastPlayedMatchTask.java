package com.api.BetStrat.tasks;


import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.NoNextMatchException;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.util.ScrappingUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.api.BetStrat.constants.BetStratConstants.CURRENT_SEASON;
import static com.api.BetStrat.constants.BetStratConstants.LONG_STREAKS_LEAGUES_LIST;

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
    @Scheduled(cron = "0 4 */2 * * *", zone="Europe/Lisbon") //every two days at 4am
    public void execCronn() {

        List<String> teamsToGetLastMatch = new ArrayList<>();

        for (String leagueUrl : LONG_STREAKS_LEAGUES_LIST) {
            JSONObject leagueTeamsScrappingData = ScrappingUtil.getLeagueTeamsScrappingData(leagueUrl);
            List<String> analysedTeams = new ArrayList<>();

            while (leagueTeamsScrappingData.keys().hasNext()) {
                String team = leagueTeamsScrappingData.keys().next().toString();
                analysedTeams.add(team);
                leagueTeamsScrappingData.remove(team);
            }

            teamsToGetLastMatch.addAll(analysedTeams);
        }
        run(teamRepository, historicMatchRepository, teamsToGetLastMatch);
    }

    @SneakyThrows
    public static void run(TeamRepository teamRepository, HistoricMatchRepository historicMatchRepository, List<String> teams) throws NoNextMatchException {
        LOGGER.info("GetLastPlayedMatchTask() at "+ Instant.now().toString());

        for (String t : teams) {
            Team team = teamRepository.getTeamByNameAndSport(t, "Football");

            if (team != null) {
                JSONArray scrappingData = ScrappingUtil.getLastNMatchesScrappingService(team, 1);
                if (scrappingData != null) {
                    for (int i = 0; i < scrappingData.length(); i++) {
                        JSONObject match = (JSONObject) scrappingData.get(i);
                        HistoricMatch historicMatch = new HistoricMatch();
                        historicMatch.setTeamId(team);
                        historicMatch.setMatchDate(match.getString("date"));
                        historicMatch.setHomeTeam(match.getString("homeTeam"));
                        historicMatch.setAwayTeam(match.getString("awayTeam"));
                        historicMatch.setFtResult(match.getString("ftResult"));
                        historicMatch.setHtResult(match.getString("htResult"));
                        historicMatch.setCompetition(match.getString("competition"));
                        historicMatch.setSport(team.getSport());
                        historicMatch.setSeason(CURRENT_SEASON);
                        try {
                            historicMatchRepository.save(historicMatch);
                            log.info("Inserted match:  " + historicMatch.toString());
                        } catch (Exception e) {
                            log.info("match:  " + historicMatch.toString() + "\nerror:  " + e.toString());
                        }
                    }
                }
            }
        }
    }

}
