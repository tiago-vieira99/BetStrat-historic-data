package com.api.BetStrat.tasks;

import static com.api.BetStrat.constants.BetStratConstants.CURRENT_SUMMER_SEASON;
import static com.api.BetStrat.constants.BetStratConstants.CURRENT_WINTER_SEASON;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_BEGIN_MONTH_LIST;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.util.TelegramBotNotifications;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                historicMatch.setUrl(match.getString("url"));
                if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(teamObj.getBeginSeason())) {
                    historicMatch.setSeason(CURRENT_WINTER_SEASON);
                } else {
                    historicMatch.setSeason(CURRENT_SUMMER_SEASON);
                }

                HistoricMatch specificMatch = historicMatchRepository.getMatchByUrl(historicMatch.getUrl(), teamObj);

                if (specificMatch == null) {
                    historicMatchRepository.save(historicMatch);
                    LOGGER.info("Inserted match:  " + historicMatch.toString());
                } else {
                    if (specificMatch.getFtResult().equals("null") && !historicMatch.getFtResult().equals("null")) {
                        historicMatchRepository.updateSpecificMatch(teamObj, historicMatch.getSeason(), historicMatch.getHomeTeam(), historicMatch.getAwayTeam(),
                            historicMatch.getMatchDate(), historicMatch.getHtResult(), historicMatch.getFtResult());
                        LOGGER.info("Updated match:  " + historicMatch.toString());
                    }
                }

            } catch (DataIntegrityViolationException cerr) {
                LOGGER.debug("match:  " + historicMatch.toString() + " already exists!");
            } catch (Exception e) {
                LOGGER.error("match:  " + historicMatch.toString() + "\nerror:  " + e.toString());
                //failedTeams += team.getName() + " | ";
            }
        }

    }

    @SneakyThrows
    @RabbitListener(queues = "specific_matches")
    public void consumeSpecificMatches(String message) {
        LOGGER.info(" [x] Received from specific_matches queue: " + message);

        JSONObject jsonObject = new JSONObject(message);
        String teamName = (String) jsonObject.keys().next();
        JSONObject specificMatchFromRabbit = (JSONObject) ((JSONObject) jsonObject.get(teamName)).get("specificMatch");

        Team teamObj = teamRepository.getTeamByNameAndSport(teamName, "Football");

        try {
            HistoricMatch specificMatchFromDB = historicMatchRepository.getMatchByUrl(specificMatchFromRabbit.getString("url"), teamObj);

            if (specificMatchFromDB != null && specificMatchFromDB.getFtResult().equals("null") && !specificMatchFromRabbit.getString("ft_result").equals("null")) {
                specificMatchFromDB.setFtResult(specificMatchFromRabbit.getString("ft_result"));
                specificMatchFromDB.setHtResult(specificMatchFromRabbit.getString("ht_result"));
                specificMatchFromDB.setMatchDate(specificMatchFromRabbit.getString("date"));
                historicMatchRepository.save(specificMatchFromDB);
                LOGGER.info("Updated match:  " + specificMatchFromDB.toString());
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate fiveDaysAgoDate = LocalDate.now().minusDays(5);

                if (LocalDate.parse(specificMatchFromDB.getMatchDate(), formatter).isBefore(fiveDaysAgoDate)) {
                    TelegramBotNotifications.sendToTelegram("ALERT\nMatch need manual update: " + specificMatchFromDB.getId());
                }
            }

        } catch (Exception e) {
            LOGGER.error("match:  " + specificMatchFromRabbit.toString() + "\nerror:  " + e.toString());
        }
    }

}
