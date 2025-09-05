package com.api.BetStrat.tasks;

import static com.api.BetStrat.constants.BetStratConstants.CURRENT_SUMMER_SEASON;
import static com.api.BetStrat.constants.BetStratConstants.CURRENT_WINTER_SEASON;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_BEGIN_MONTH_LIST;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class EventsQueueListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventsQueueListener.class);

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @SneakyThrows
    @RabbitListener(queues = "historic_last_matches")
    public void consumeLastHistoricMatches(String message) {
        LOGGER.info(" [x] Received from historic_last_matches queue: " + message);

        JSONObject jsonObject = new JSONObject(message);
        String teamName = (String) jsonObject.keys().next();
        JSONArray lastMatches = (JSONArray) ((JSONObject) jsonObject.get(teamName)).get("lastMatches");

        Team teamObj = teamRepository.getTeamByNameAndSport(teamName, "Football");

        for (int i = 0; i < lastMatches.length(); i++) {
            HistoricMatch historicMatch = new HistoricMatch();
            try {
                JSONObject match = (JSONObject) lastMatches.get(i);
                historicMatch.setTeamId(teamObj);
                historicMatch.setMatchDate(match.getString("date"));
                historicMatch.setHomeTeam(match.getString("homeTeam"));
                historicMatch.setAwayTeam(match.getString("awayTeam"));
                historicMatch.setFtResult(match.getString("ftResult"));
                historicMatch.setHtResult(match.getString("htResult"));
                historicMatch.setCompetition(match.getString("competition"));
                historicMatch.setSport(teamObj.getSport());
                if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(teamObj.getBeginSeason())) {
                    historicMatch.setSeason(CURRENT_WINTER_SEASON);
                } else {
                    historicMatch.setSeason(CURRENT_SUMMER_SEASON);
                }

                historicMatchRepository.save(historicMatch);
                LOGGER.info("Inserted match:  " + historicMatch.toString());
            } catch (DataIntegrityViolationException cerr) {
                LOGGER.debug("match:  " + historicMatch.toString() + " already exists!");
            } catch (Exception e) {
                LOGGER.error("match:  " + historicMatch.toString() + "\nerror:  " + e.toString());
                //failedTeams += team.getName() + " | ";
            }
        }

    }

}
