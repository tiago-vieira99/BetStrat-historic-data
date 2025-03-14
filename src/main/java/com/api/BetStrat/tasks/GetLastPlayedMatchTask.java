package com.api.BetStrat.tasks;


import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.NoNextMatchException;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.util.ScrappingUtil;
import com.api.BetStrat.util.TelegramBotNotifications;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    //@EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 2 * * *", zone="Europe/Lisbon") //every two days at 2am
    public void execCronn() {

        List<String> teamsToGetLastMatch = new ArrayList<>();

        for (String leagueUrl : LEAGUES_LIST) {
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

        int numNewMatches = 0;
        String failedTeams = "";

        for (String t : teams) {
            Team team = teamRepository.getTeamByNameAndSport(t, "Football");

            if (team != null) {
                JSONArray scrappingData = ScrappingUtil.getLastNMatchesScrappingService(team, 3);
                if (scrappingData != null) {
                    for (int i = 0; i < scrappingData.length(); i++) {
                        HistoricMatch historicMatch = new HistoricMatch();
                        try {
                            JSONObject match = (JSONObject) scrappingData.get(i);
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
                            failedTeams += t + " | ";
                        }
                    }
                } else {
                    failedTeams += t + " | ";
                }
            }
        }

        String telegramMessage = String.format("ℹ number of new matches added: " + numNewMatches + "\nfailed: " + failedTeams);
        Thread.sleep(1000);
        TelegramBotNotifications.sendToTelegram(telegramMessage);
    }

    //TODO cases to recommend a new sequence:
    // 1 - when the actual sequence is higher than the default_max_bad_seq
    // 2 - when the actual sequence is Max(3, -2(?) games to reach the historic avg_bad_seq)
    // 3 - when the actual sequence is Max(3, -4(?) games to reach the historic max_bad_seq)

}
