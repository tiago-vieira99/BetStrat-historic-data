package com.api.BetStrat.controller;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.service.StrategySeasonStatsService;
import com.api.BetStrat.service.TeamService;
import com.api.BetStrat.tasks.GetLastPlayedMatchTask;
import com.api.BetStrat.util.ScrappingUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.API_SPORTS_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.FBREF_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.FOOTBALL_STRATEGIES_LIST;
import static com.api.BetStrat.constants.BetStratConstants.LONG_STREAKS_LEAGUES_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WORLDFOOTBALL_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.ZEROZERO_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.ZEROZERO_SEASON_CODES;

@Slf4j
@Api("Historical Data Analysis for Football")
@RestController
@CrossOrigin
@RequestMapping("/api/bhd")
public class FootballDataStatsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FootballDataStatsController.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private StrategySeasonStatsService strategySeasonStatsService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    // one instance, reuse
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @ApiOperation(value = "get All Teams")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> allTeams = teamRepository.findAll();
        return ResponseEntity.ok().body(allTeams);
    }

    @ApiOperation(value = "get Team Stats by Strategy", notes = "Strategy values:\n\"Draw\",\"GoalsFest\",\"WinsMargin\",\"Btts\", \"CleanSheet\", \n" +
            "            \"ConcedeBothHalves\", \"EuroHandicap\", \"NoBtts\", \"NoGoalsFest\", \"NoWins\", \"ScoreBothHalves\", \n" +
            "            \"SecondHalfBigger\", \"WinAndGoals\", \"WinBothHalves\", \"Wins\"")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/stats/{strategy}/{teamName}")
    public ResponseEntity<List<StrategySeasonStats>> getStrategyStats(@PathVariable("teamName") String teamName, @PathVariable("strategy") String strategy) {
        Team team = teamRepository.getTeamByNameAndSport(teamName, "Football");
        List<StrategySeasonStats> statsByStrategyAndTeam = strategySeasonStatsService.getStatsByStrategyAndTeam(team, strategy.concat("SeasonStats"));
        return ResponseEntity.ok().body(statsByStrategyAndTeam);
    }

    @ApiOperation(value = "update Team score for Strategy", notes = "Strategy values:\n\"Draw\",\"GoalsFest\",\"WinsMargin\",\"Btts\", \"CleanSheet\", \n" +
            "            \"ConcedeBothHalves\", \"EuroHandicap\", \"NoBtts\", \"NoGoalsFest\", \"NoWins\", \"ScoreBothHalves\", \n" +
            "            \"SecondHalfBigger\", \"WinAndGoals\", \"WinBothHalves\", \"Wins\"")
    @PostMapping("/score/{strategy}/{teamName}/update")
    public Team updateTeamScore (@PathVariable("teamName") String teamName, @PathVariable("strategy")  String strategy) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (null == teamByName) {
            throw new NotFoundException();
        }
        return teamService.updateTeamScore(teamByName, strategy.concat("SeasonStats"));
    }

    @PostMapping("/team")
    public Team insertTeam (@Valid @RequestParam String teamName, @Valid @RequestParam  String url,
                            @Valid @RequestParam  String beginSeason, @Valid @RequestParam  String endSeason, @Valid @RequestParam  String country) {
        Team team = new Team();
        team.setName(teamName);
        team.setUrl(url);
        team.setBeginSeason(beginSeason);
        team.setEndSeason(endSeason);
        team.setCountry(country);
        team.setSport("Football");
        Team newTeam = teamService.insertTeam(team);

        return newTeam;
    }

    @ApiOperation(value = "update all Teams score for Strategy", notes = "Strategy values:\n\"Draw\",\"GoalsFest\",\"WinsMargin\",\"Btts\", \"CleanSheet\", \n" +
            "            \"ConcedeBothHalves\", \"EuroHandicap\", \"NoBtts\", \"NoGoalsFest\", \"NoWins\", \"ScoreBothHalves\", \n" +
            "            \"SecondHalfBigger\", \"WinAndGoals\", \"WinBothHalves\", \"Wins\"")
    @PostMapping("/score-by-strategy/all-teams/update")
    public ResponseEntity<String> updateAllTeamsScoreByStrategy (@Valid @RequestParam  String strategy) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());
        for (int i=0; i< allTeams.size(); i++) {
            try {
                teamService.updateTeamScore(allTeams.get(i), strategy.concat("SeasonStats"));
            } catch (NumberFormatException er) {
                log.error(er.toString());
            }
        }
        return ResponseEntity.ok().body("OK");
    }

    @ApiOperation(value = "update all Teams stats for Strategy", notes = "Strategy values:\n\"Draw\",\"GoalsFest\",\"WinsMargin\",\"Btts\", \"CleanSheet\", \n" +
            "            \"ConcedeBothHalves\", \"EuroHandicap\", \"NoBtts\", \"NoGoalsFest\", \"NoWins\", \"ScoreBothHalves\", \n" +
            "            \"SecondHalfBigger\", \"WinAndGoals\", \"WinBothHalves\", \"Wins\"")
    @PostMapping("/stats-by-strategy/all-teams/update")
    public ResponseEntity<String> updateAllTeamsStatsByStrategy (@Valid @RequestParam  String strategy) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());

        for (int i=0; i< allTeams.size(); i++) {
            log.info("handling " + allTeams.get(i).getName());
            strategySeasonStatsService.updateStrategySeasonStats(allTeams.get(i), strategy.concat("SeasonStats"));
            teamService.updateTeamScore(allTeams.get(i), strategy.concat("SeasonStats"));
        }

        return ResponseEntity.ok().body("OK");
    }

    @ApiOperation(value = "update Team stats for Strategy", notes = "Strategy values:\n\"Draw\",\"GoalsFest\",\"WinsMargin\",\"Btts\", \"CleanSheet\", \n" +
            "            \"ConcedeBothHalves\", \"EuroHandicap\", \"NoBtts\", \"NoGoalsFest\", \"NoWins\", \"ScoreBothHalves\", \n" +
            "            \"SecondHalfBigger\", \"WinAndGoals\", \"WinBothHalves\", \"Wins\"")
    @PostMapping("/stats-by-strategy/{team}/update")
    public ResponseEntity<String> updateTeamStatsByStrategy (@Valid @RequestParam  String strategy, @PathVariable("team")  String teamName) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (teamByName == null) {
            throw new NotFoundException();
        }

        strategySeasonStatsService.updateStrategySeasonStats(teamByName, strategy.concat("SeasonStats"));
        teamService.updateTeamScore(teamByName, strategy.concat("SeasonStats"));

        return ResponseEntity.ok().body("OK");
    }

    @GetMapping("/historic-matches")
    public List<HistoricMatch> getHistoricMatches(@Valid @RequestParam(value = "teamId", required = false) Long teamId,
                                                  @Valid @RequestParam  String season,
                                                  @Valid @RequestParam(value = "teamName", required = false) String teamName) {

        Team team = null;
        if (teamId != null) {
            team = teamRepository.getOne(teamId);
        } else {
            team = teamRepository.getTeamByNameAndSport(teamName, "Football");
        }

        return historicMatchRepository.getTeamMatchesBySeason(team, season);
    }

    @ApiOperation(value = "get historic matches of Team and Season from scrapper-service and save them in db")
    @SneakyThrows
    @PostMapping("/historic-matches/insert")
    public void insertHistoricalMatches(@Valid @RequestParam  Long teamId, @Valid @RequestParam String season) {

        Team team = teamRepository.getOne(teamId);

        LOGGER.info(team.getName() + " - " + season);
        String teamUrl = team.getUrl();
        JSONArray scrappingData = null;
        String newSeasonUrl = "";

        if (teamUrl == null) {
            return;
        }

        if (teamUrl.contains(ZEROZERO_BASE_URL)) {
            String seasonZZCode = ZEROZERO_SEASON_CODES.get(season);
            newSeasonUrl = teamUrl.replaceAll("epoca_id=\\d+", "epoca_id=" + seasonZZCode);
            scrappingData = ScrappingUtil.getScrappingData(team.getName(), season, newSeasonUrl, true);
        } else if (teamUrl.contains(FBREF_BASE_URL)) {
            String newSeason = "";
            if (season.contains("-")) {
                newSeason = season.split("-")[0] + "-20" + season.split("-")[1];
            } else {
                newSeason = season;
            }
            newSeasonUrl = teamUrl.split("/matchlogs")[0].substring(0, teamUrl.split("/matchlogs")[0].lastIndexOf('/')) + "/" + newSeason + "/matchlogs" + teamUrl.split("/matchlogs")[1];
            scrappingData = ScrappingUtil.getScrappingData(team.getName(), newSeason, newSeasonUrl, true);
        } else if (teamUrl.contains(WORLDFOOTBALL_BASE_URL)) {
            String newSeason = "";
            if (season.contains("-")) {
                newSeason = "20" + season.split("-")[1];
            } else {
                newSeason = season;
            }
            newSeasonUrl = teamUrl + "/" + newSeason + "/3/";
            scrappingData = ScrappingUtil.getScrappingData(team.getName(), newSeason, newSeasonUrl, true);
        } else if (teamUrl.contains(API_SPORTS_BASE_URL)) {
            String newSeason = "";
            if (season.contains("-")) {
                newSeason = season.split("-")[0];
            } else {
                newSeason = season;
            }
            newSeasonUrl = teamUrl + "&season=" + newSeason;
            scrappingData = ScrappingUtil.getScrappingData(team.getName(), newSeason, newSeasonUrl, true);
        }

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
                historicMatch.setSeason(season);
                try {
                    historicMatchRepository.save(historicMatch);
                } catch (Exception e) {
                    log.info("match:  " + historicMatch.toString() + "\nerror:  " + e.toString());
                }
            }
        }
    }

    @ApiOperation(value = "Trigger GetLastPlayedMatchTask")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @PostMapping("/update-last-played-match")
    public ResponseEntity<String> triggerGetLastPlayedMatchTask() {

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
        GetLastPlayedMatchTask.run(teamRepository, historicMatchRepository, teamsToGetLastMatch);

        return ResponseEntity.ok().body("triggered GetLastPlayedMatchTask");
    }
}
