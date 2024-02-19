package com.api.BetStrat.controller;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.football.DrawSeasonInfo;
import com.api.BetStrat.entity.football.EuroHandicapSeasonInfo;
import com.api.BetStrat.entity.football.FlipFlopOversUndersInfo;
import com.api.BetStrat.entity.football.GoalsFestSeasonInfo;
import com.api.BetStrat.entity.football.WinsMarginSeasonInfo;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.repository.football.DrawSeasonInfoRepository;
import com.api.BetStrat.repository.football.FlipFlopOversUndersInfoRepository;
import com.api.BetStrat.repository.football.GoalsFestSeasonInfoRepository;
import com.api.BetStrat.repository.football.WinsMarginSeasonInfoRepository;
import com.api.BetStrat.service.StatsBySeasonService;
import com.api.BetStrat.service.TeamService;
import com.api.BetStrat.tasks.GetLastPlayedMatchTask;
import com.api.BetStrat.util.ScrappingUtil;
import com.api.BetStrat.util.TeamDFhistoricData;
import com.api.BetStrat.util.TeamEHhistoricData;
import com.api.BetStrat.util.TeamGoalsFestHistoricData;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.API_SPORTS_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.FBREF_BASE_URL;
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
    private StatsBySeasonService statsBySeasonService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    @Autowired
    private DrawSeasonInfoRepository drawSeasonInfoRepository;

    @Autowired
    private WinsMarginSeasonInfoRepository winsMarginSeasonInfoRepository;

    @Autowired
    private GoalsFestSeasonInfoRepository goalsFestSeasonInfoRepository;

    @Autowired
    private FlipFlopOversUndersInfoRepository flipFlopOversUndersInfoRepository;

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

    @ApiOperation(value = "get Team Margin Wins Stats")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/team-margin-wins-stats/{teamName}")
    public ResponseEntity<List<WinsMarginSeasonInfo>> getTeamMarginWinsStats(@PathVariable("teamName") String teamName) {
        List<WinsMarginSeasonInfo> teamStats = teamService.getTeamMarginWinStats(teamName);
        return ResponseEntity.ok().body(teamStats);
    }

    @ApiOperation(value = "get Team Euro Handicap Stats")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/team-euro-handicap-stats/{teamName}")
    public ResponseEntity<List<EuroHandicapSeasonInfo>> getTeamEuroHandicapStats(@PathVariable("teamName") String teamName) {
        List<EuroHandicapSeasonInfo> teamStats = teamService.getTeamEuroHandicapStats(teamName);
        return ResponseEntity.ok().body(teamStats);
    }

    @ApiOperation(value = "get Team Draw Stats")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/team-draw-stats/{teamName}")
    public ResponseEntity<List<DrawSeasonInfo>> getTeamStats(@PathVariable("teamName") String teamName) {
        List<DrawSeasonInfo> teamStats = teamService.getTeamDrawStats(teamName);
        return ResponseEntity.ok().body(teamStats);
    }

    @ApiOperation(value = "get Team Goals Fest Stats")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/team-goals-fest-stats/{teamName}")
    public ResponseEntity<List<GoalsFestSeasonInfo>> getTeamGoalsFestStats(@PathVariable("teamName") String teamName) {
        List<GoalsFestSeasonInfo> teamStats = teamService.getTeamGoalsFestStats(teamName);
        return ResponseEntity.ok().body(teamStats);
    }

    @ApiOperation(value = "get Team Goals Fest Stats")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/team-flip-flop-stats/{teamName}")
    public ResponseEntity<List<FlipFlopOversUndersInfo>> getTeamFlipFlopStats(@PathVariable("teamName") String teamName) {
        List<FlipFlopOversUndersInfo> teamStats = teamService.getTeamFlipFlopStats(teamName);
        return ResponseEntity.ok().body(teamStats);
    }

    @PostMapping("/updateTeamScore/{teamName}")
    public Team updateTeamScore (@PathVariable("teamName") String teamName, @Valid @RequestParam  String strategy) {
        return teamService.updateTeamScore(teamName, strategy, "Football");
    }

    @PostMapping("/newTeam")
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

//        List<String> seasonsList = null;
//
//        if (SUMMER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
//            seasonsList = new ArrayList<>(SUMMER_SEASONS_LIST);
//        } else if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
//            seasonsList = new ArrayList<>(WINTER_SEASONS_LIST);
//            seasonsList.add("2023-24");
//        }
//
//        for (String season : seasonsList) {
//            insertHistoricalMatches(newTeam.getId(), season);
//        }
//
//        updateTeamStatsByStrategy("footballDrawHunter", teamName);
//        updateTeamStatsByStrategy("footballMarginWins", teamName);
//        updateTeamStatsByStrategy("footballGoalsFest", teamName);
//        updateTeamStatsByStrategy("footballEuroHandicap", teamName);
//        updateTeamStatsByStrategy("footballFlipFlop", teamName);

        return newTeam;
    }

    @ApiOperation(value = "updateAllTeamsScoreByStrategy", notes = "Strategy values: hockeyDraw, hockeyWinsMarginAny2, hockeyWinsMargin3, footballDrawHunter, footballMarginWins, footballGoalsFest, footballEuroHandicap, basketComebacks.")
    @PostMapping("/updateAllTeamsScoreByStrategy")
    public ResponseEntity<String> updateAllTeamsScoreByStrategy (@Valid @RequestParam  String strategy) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());
        for (int i=0; i< allTeams.size(); i++) {
            try {
                teamService.updateTeamScore(allTeams.get(i).getName(), strategy, "Football");
            } catch (NumberFormatException er) {
                log.error(er.toString());
            }
        }
        return ResponseEntity.ok().body("OK");
    }

    @ApiOperation(value = "simulateAllTeamsScoreByStrategyAndFilteredSeasons", notes = "Strategy values: hockeyDraw, hockeyWinsMarginAny2, hockeyWinsMargin3, footballDrawHunter, footballMarginWins, footballGoalsFest, footballEuroHandicap, basketComebacks.")
    @PostMapping("/simulateAllTeamsScoreByStrategyAndFilteredSeasons")
    public ResponseEntity<HashMap<String, HashMap>> simulateAllTeamsScoreByStrategyAndFilteredSeasons (@Valid @RequestParam  String strategy, @RequestParam @Valid int seasonsToDiscard) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());
        HashMap<String, HashMap> newOutMap = new HashMap<>();
        for (int j=1;j<5;j++) {
            HashMap<String, HashMap> outMap = new HashMap<>();
            for (int i = 0; i < allTeams.size(); i++) {
                log.info("handling " + allTeams.get(i).getName());
                try {
                    HashMap<String, String> simulatedTeamScoreByFilteredSeason = teamService.getSimulatedTeamScoreByFilteredSeason(allTeams.get(i), strategy, j);
                    outMap.put(allTeams.get(i).getName(), simulatedTeamScoreByFilteredSeason);
                } catch (NumberFormatException er) {
                    log.error(er.toString());
                }
            }
            newOutMap.put("discarded " + j, outMap);
        }
        return ResponseEntity.ok().body(newOutMap);
    }

    @ApiOperation(value = "updateAllTeamsStatsByStrategy", notes = "Strategy values: hockeyDraw, hockeyWinsMarginAny2, hockeyWinsMargin3, footballDrawHunter, footballMarginWins, footballGoalsFest, footballEuroHandicap, footballFlipFlop, basketComebacks. \nData sources:  \n FBRef:\n" +
            " \n https://fbref.com/en/squads/d48ad4ff/2022-2023/matchlogs/schedule/Napoli-Scores-and-Fixturesn" +
            " \n\n" +
            " \n ZZ:\n" +
            " \n https://www.zerozero.pt/team_matches.php?grp=1&ond=&epoca_id=152&compet_id_jogos=0&ved=&epoca_id=151&comfim=0&id=9&equipa_1=9&menu=allmatches&type=season&op=ver_confronto\n" +
            " \n\n" +
            " \n WF:\n" +
            " \n https://www.worldfootball.net/teams/fc-porto/")
    @PostMapping("/updateAllTeamsStatsByStrategy")
    public ResponseEntity<String> updateAllTeamsStatsByStrategy (@Valid @RequestParam  String strategy) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());

        for (int i=0; i< allTeams.size(); i++) {
            log.info("handling " + allTeams.get(i).getName());
            teamService.updateTeamStats(allTeams.get(i), strategy);
            teamService.updateTeamScore(allTeams.get(i).getName(), strategy, "Football");
        }

        return ResponseEntity.ok().body("OK");
    }

    @ApiOperation(value = "updateTeamStatsByStrategy", notes = "Strategy values: hockeyDraw, hockeyWinsMarginAny2, hockeyWinsMargin3, footballDrawHunter, footballMarginWins, footballGoalsFest, footballEuroHandicap, footballFlipFlop, basketComebacks. \nData sources:  \n FBRef:\n" +
            " \n https://fbref.com/en/squads/d48ad4ff/2022-2023/matchlogs/schedule/Napoli-Scores-and-Fixturesn" +
            " \n\n" +
            " \n ZZ:\n" +
            " \n https://www.zerozero.pt/team_matches.php?grp=1&ond=&epoca_id=152&compet_id_jogos=0&ved=&epoca_id=151&comfim=0&id=9&equipa_1=9&menu=allmatches&type=season&op=ver_confronto\n" +
            " \n\n" +
            " \n WF:\n" +
            " \n https://www.worldfootball.net/teams/fc-porto/")
    @PostMapping("/updateTeamStatsByStrategy")
    public ResponseEntity<String> updateTeamStatsByStrategy (@Valid @RequestParam  String strategy, @Valid @RequestParam  String teamName) {
        Team teamByName = teamRepository.getTeamByName(teamName);
        if (teamByName == null) {
            throw new NotFoundException();
        }

        teamService.updateTeamStats(teamByName, strategy);
        teamService.updateTeamScore(teamByName.getName(), strategy, "Football");

        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("/draw-stats-manually")
    public DrawSeasonInfo setDrawStatsManually (@Valid @RequestParam  String teamName,
                                                @Valid @RequestParam(value = "season", required = false) String season,
                                                @Valid @RequestParam(value = "url", required = false) String url,
                                                @Valid @RequestParam(value = "drawRate", required = false) Double drawRate,
                                                @Valid @RequestParam(value = "numDraws", required = false) Integer numDraws,
                                                @Valid @RequestParam(value = "numMatches", required = false) Integer numMatches,
                                                @Valid @RequestParam(value = "noDrawsSeq", required = false) String noDrawsSeq,
                                                @Valid @RequestParam(value = "stdDev", required = false) Double stdDev,
                                                @Valid @RequestParam(value = "coefDev", required = false) Double coefDev,
                                                @Valid @RequestParam(value = "competition", required = false) String competition) {


        DrawSeasonInfo drawSeasonInfo = new DrawSeasonInfo();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            throw new NotFoundException();
        }

        drawSeasonInfo.setTeamId(team);
        drawSeasonInfo.setSeason(season);
        drawSeasonInfo.setUrl(url);
        drawSeasonInfo.setDrawRate(drawRate);
        drawSeasonInfo.setNumDraws(numDraws);
        drawSeasonInfo.setNumMatches(numMatches);
        drawSeasonInfo.setNegativeSequence(noDrawsSeq);
        drawSeasonInfo.setStdDeviation(stdDev);
        drawSeasonInfo.setCoefDeviation(coefDev);
        drawSeasonInfo.setCompetition(competition);

        return (DrawSeasonInfo) statsBySeasonService.insertStatsBySeasonInfo(drawSeasonInfo);
    }

    @PostMapping("/margin-wins-manually")
    public WinsMarginSeasonInfo setMarginWinsManually (@Valid @RequestParam  String teamName,
                                                       @Valid @RequestParam(value = "season", required = false) String season,
                                                       @Valid @RequestParam(value = "url", required = false) String url,
                                                       @Valid @RequestParam(value = "winsRate", required = false) Double winsRate,
                                                       @Valid @RequestParam(value = "marginWinsRate", required = false) Double marginWinsRate,
                                                       @Valid @RequestParam(value = "numWins", required = false) Integer numWins,
                                                       @Valid @RequestParam(value = "numMarginWins", required = false) Integer numMarginWins,
                                                       @Valid @RequestParam(value = "numMatches", required = false) Integer numMatches,
                                                       @Valid @RequestParam(value = "noMarginWinsSeq", required = false) String noMarginWinsSeq,
                                                       @Valid @RequestParam(value = "stdDev", required = false) Double stdDev,
                                                       @Valid @RequestParam(value = "coefDev", required = false) Double coefDev,
                                                       @Valid @RequestParam(value = "competition", required = false) String competition) {

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            throw new NotFoundException();
        }

        WinsMarginSeasonInfo winsMarginSeasonInfo = new WinsMarginSeasonInfo();

        winsMarginSeasonInfo.setNumMatches(numMatches);
        winsMarginSeasonInfo.setNumMarginWins(numMarginWins);
        winsMarginSeasonInfo.setNumWins(numWins);

        winsMarginSeasonInfo.setTeamId(team);
        winsMarginSeasonInfo.setSeason(season);
        winsMarginSeasonInfo.setUrl(url);

        winsMarginSeasonInfo.setWinsRate(winsRate);
        winsMarginSeasonInfo.setMarginWinsRate(marginWinsRate);
        winsMarginSeasonInfo.setNegativeSequence(noMarginWinsSeq);
        winsMarginSeasonInfo.setStdDeviation(stdDev);
        winsMarginSeasonInfo.setCoefDeviation(coefDev);
        winsMarginSeasonInfo.setCompetition(competition);

        //teamService.updateTeamScore(teamName);

        return (WinsMarginSeasonInfo) statsBySeasonService.insertStatsBySeasonInfo(winsMarginSeasonInfo);
    }

    @PostMapping("/goals-fest-manually")
    public GoalsFestSeasonInfo setGoalsFestStatsManually (@Valid @RequestParam  String teamName,
                                                @Valid @RequestParam(value = "season", required = false) String season,
                                                @Valid @RequestParam(value = "url", required = false) String url,
                                                @Valid @RequestParam(value = "goalsFestRate", required = false) Double goalsFestRate,
                                                @Valid @RequestParam(value = "numGoalsFest", required = false) Integer numGoalsFest,
                                                @Valid @RequestParam(value = "numMatches", required = false) Integer numMatches,
                                                @Valid @RequestParam(value = "noGoalsFestSeq", required = false) String noGoalsFestSeq,
                                                @Valid @RequestParam(value = "stdDev", required = false) Double stdDev,
                                                @Valid @RequestParam(value = "coefDev", required = false) Double coefDev) {


        GoalsFestSeasonInfo goalsFestSeasonInfo = new GoalsFestSeasonInfo();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            throw new NotFoundException();
        }

        goalsFestSeasonInfo.setTeamId(team);
        goalsFestSeasonInfo.setSeason(season);
        goalsFestSeasonInfo.setUrl(url);
        goalsFestSeasonInfo.setGoalsFestRate(goalsFestRate);
        goalsFestSeasonInfo.setNumGoalsFest(numGoalsFest);
        goalsFestSeasonInfo.setNumMatches(numMatches);
        goalsFestSeasonInfo.setNegativeSequence(noGoalsFestSeq);
        goalsFestSeasonInfo.setStdDeviation(stdDev);
        goalsFestSeasonInfo.setCoefDeviation(coefDev);
        goalsFestSeasonInfo.setCompetition("all");

        //teamService.updateTeamScore(teamName);

        return (GoalsFestSeasonInfo) statsBySeasonService.insertStatsBySeasonInfo(goalsFestSeasonInfo);
    }

    @PostMapping("/draw-stats-by-team-season-fcstats")
    public LinkedHashMap<String, DrawSeasonInfo> setDrawStatsByTeamSeasonFC(@Valid @RequestParam  String teamName,
                                                                            @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                            @Valid @RequestParam(value = "end-season", required = false) String endSeason,
                                                                            @Valid @RequestParam(value = "url", required = false) String url) {
        LinkedHashMap<String, DrawSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setSport("Football");
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        TeamDFhistoricData teamDFhistoricData = new TeamDFhistoricData();
        LinkedHashMap<String, Object> scrappedInfoMap = teamDFhistoricData.extractDFDataFromLastSeasonsFCStats(url);

        for (Map.Entry<String,Object> entry : scrappedInfoMap.entrySet()){
            LinkedHashMap<String, Object> scrappedInfo = (LinkedHashMap<String, Object>) entry.getValue();
            DrawSeasonInfo drawSeasonInfo = new DrawSeasonInfo();
            try {
                drawSeasonInfo.setDrawRate((Double) scrappedInfo.get("drawRate"));
                drawSeasonInfo.setNumDraws((Integer) scrappedInfo.get("totalDraws"));
                drawSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));

            } catch (Exception e) {
                return null;
            }

            drawSeasonInfo.setTeamId(team);
            drawSeasonInfo.setSeason(entry.getKey());
            drawSeasonInfo.setUrl(url);

            drawSeasonInfo.setNegativeSequence((String) scrappedInfo.get("noDrawsSeq"));
            drawSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
            drawSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
            drawSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
            statsBySeasonService.insertStatsBySeasonInfo(drawSeasonInfo);
            returnMap.put(entry.getKey(), drawSeasonInfo);
        }

        return returnMap;
    }


    @PostMapping("/draw-stats-by-team-season")
    public LinkedHashMap<String, DrawSeasonInfo> setDrawStatsByTeamSeason(@Valid @RequestParam  String teamName,
                                                                          @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                          @Valid @RequestParam(value = "end-season", required = false) String endSeason,
                                                                          @Valid @RequestParam(value = "2016", required = false) Optional<String> url2016,
                                                                          @Valid @RequestParam(value = "2016-17", required = false) Optional<String> url201617,
                                                                          @Valid @RequestParam(value = "2017", required = false) Optional<String> url2017,
                                                                          @Valid @RequestParam(value = "2017-18", required = false) Optional<String> url201718,
                                                                          @Valid @RequestParam(value = "2018", required = false) Optional<String> url2018,
                                                                          @Valid @RequestParam(value = "2018-19", required = false) Optional<String> url201819,
                                                                          @Valid @RequestParam(value = "2019", required = false) Optional<String> url2019,
                                                                          @Valid @RequestParam(value = "2019-20", required = false) Optional<String> url201920,
                                                                          @Valid @RequestParam(value = "2020", required = false) Optional<String> url2020,
                                                                          @Valid @RequestParam(value = "2020-21", required = false) Optional<String> url202021,
                                                                          @Valid @RequestParam(value = "2021", required = false) Optional<String> url2021,
                                                                          @Valid @RequestParam(value = "2021-22", required = false) Optional<String> url202122,
                                                                          @Valid @RequestParam(value = "2022", required = false) Optional<String> url2022,
                                                                          @Valid @RequestParam(value = "2022-23", required = false) Optional<String> url202223,
                                                                          @Valid @RequestParam(value = "2023", required = false) Optional<String> url2023) {

        LinkedHashMap<String, DrawSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setSport("Football");
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        if (url2016.isPresent()) {
            returnMap.put("2016", insertDrawInfoBySeason(team, "2016", url2016.get()));
        }
        if (url201617.isPresent()) {
            returnMap.put("2016-17", insertDrawInfoBySeason(team, "2016-17", url201617.get()));
        }
        if (url2017.isPresent()) {
            returnMap.put("2017", insertDrawInfoBySeason(team, "2017", url2017.get()));
        }
        if (url201718.isPresent()) {
            returnMap.put("2017-18", insertDrawInfoBySeason(team, "2017-18", url201718.get()));
        }
        if (url2018.isPresent()) {
            returnMap.put("2018", insertDrawInfoBySeason(team, "2018", url2018.get()));
        }
        if (url201819.isPresent()) {
            returnMap.put("2018-19", insertDrawInfoBySeason(team, "2018-19", url201819.get()));
        }
        if (url2019.isPresent()) {
            returnMap.put("2019", insertDrawInfoBySeason(team, "2019", url2019.get()));
        }
        if (url201920.isPresent()) {
            returnMap.put("2019-20", insertDrawInfoBySeason(team, "2019-20", url201920.get()));
        }
        if (url2020.isPresent()) {
            returnMap.put("2020", insertDrawInfoBySeason(team, "2020", url2020.get()));
        }
        if (url202021.isPresent()) {
            returnMap.put("2020-21", insertDrawInfoBySeason(team, "2020-21", url202021.get()));
        }
        if (url2021.isPresent()) {
            returnMap.put("2021", insertDrawInfoBySeason(team, "2021", url2021.get()));
        }
        if (url202122.isPresent()) {
            returnMap.put("2021-22", insertDrawInfoBySeason(team, "2021-22", url202122.get()));
        }
        if (url2022.isPresent()) {
            returnMap.put("2022", insertDrawInfoBySeason(team, "2022", url2022.get()));
        }
        if (url202223.isPresent()) {
            returnMap.put("2022-23", insertDrawInfoBySeason(team, "2022-23", url202223.get()));
        }
        if (url2023.isPresent()) {
            returnMap.put("2023", insertDrawInfoBySeason(team, "2023", url2023.get()));
        }

        return returnMap;
    }

    private DrawSeasonInfo insertDrawInfoBySeason (Team team, String season, String url) {
        TeamDFhistoricData teamDFhistoricData = new TeamDFhistoricData();
        LinkedHashMap<String, Object> scrappedInfo = null;
        DrawSeasonInfo drawSeasonInfo = new DrawSeasonInfo();
        try {
            if (url.contains("team_matches")) {
                scrappedInfo = teamDFhistoricData.extractDFDataFromZZ(url);
                drawSeasonInfo.setDrawRate(Double.parseDouble((String) scrappedInfo.get("drawRate")));
                drawSeasonInfo.setNumDraws(Integer.parseInt((String) scrappedInfo.get("totalDraws")));
                drawSeasonInfo.setNumMatches(Integer.parseInt((String) scrappedInfo.get("totalMatches")));
            } else {
                scrappedInfo = teamDFhistoricData.extractDFDataFromFC(url);
                drawSeasonInfo.setDrawRate((Double) scrappedInfo.get("drawRate"));
                drawSeasonInfo.setNumDraws((Integer) scrappedInfo.get("totalDraws"));
                drawSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));
            }

        } catch (Exception e) {
            return null;
        }

        drawSeasonInfo.setTeamId(team);
        drawSeasonInfo.setSeason(season);
        drawSeasonInfo.setUrl(url);

        drawSeasonInfo.setNegativeSequence((String) scrappedInfo.get("noDrawsSeq"));
        drawSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
        drawSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
        drawSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
        return (DrawSeasonInfo) statsBySeasonService.insertStatsBySeasonInfo(drawSeasonInfo);
    }

    @GetMapping("/getHistoricMatches")
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

    @SneakyThrows
    @PostMapping("/historicalMatchesResults")
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

    @ApiOperation(value = "setGoalsFestStatsByTeamSeason", notes = "set goals fest stats from FBref in bulk. Provide teamId and season time (WINTER/SUMMER)")
    @PostMapping("/goals-fest-stats-by-team-season")
    public LinkedHashMap<String, GoalsFestSeasonInfo> setGoalsFestStatsByTeamSeason(@Valid @RequestParam  String teamName,
                                                                                    @Valid @RequestParam  String teamId,
                                                                                    @Valid @RequestParam  String seasonTime,
                                                                                    @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                                    @Valid @RequestParam(value = "end-season", required = false) String endSeason) {

        LinkedHashMap<String, GoalsFestSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setSport("Football");
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        if (seasonTime.equals("WINTER")) {
            Map<String, String> winterSeasonsIds  = new HashMap<String, String>() {{
                put("2016-17", "2016-2017");
                put("2017-18", "2017-2018");
                put("2018-19", "2018-2019");
                put("2019-20", "2019-2020");
                put("2020-21", "2020-2021");
                put("2021-22", "2021-2022");
            }};

            for (Map.Entry<String,String> entry : winterSeasonsIds.entrySet()) {
                String url = String.format("https://fbref.com/en/squads/%s/%s/all_comps", teamId, entry.getValue());
                returnMap.put(entry.getKey(), insertGoalsFestInfoBySeason(team, entry.getKey(), url));
            }
        } else {
            List<String> summerSeasonss = Arrays.asList("2016", "2017", "2018", "2019", "2020", "2021", "2022");
            for (String season : summerSeasonss) {
                String url = String.format("https://fbref.com/en/squads/%s/%s/all_comps", teamId, season);
                returnMap.put(season, insertGoalsFestInfoBySeason(team, season, url));
            }
        }

        return returnMap;
    }

    private GoalsFestSeasonInfo insertGoalsFestInfoBySeason (Team team, String season, String url) {
        TeamGoalsFestHistoricData teamGoalsFestHistoricData = new TeamGoalsFestHistoricData();
        LinkedHashMap<String, Object> scrappedInfo = null;
        GoalsFestSeasonInfo goalsFestSeasonInfo = new GoalsFestSeasonInfo();
        try {
            scrappedInfo = teamGoalsFestHistoricData.extractGoalsFestDataFromFBref(url);
            goalsFestSeasonInfo.setGoalsFestRate((Double) scrappedInfo.get("goalsFestRate"));
            goalsFestSeasonInfo.setNumGoalsFest((Integer) scrappedInfo.get("totalGoalsFest"));
            goalsFestSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));
            goalsFestSeasonInfo.setTeamId(team);
            goalsFestSeasonInfo.setSeason(season);
            goalsFestSeasonInfo.setUrl(url);

            goalsFestSeasonInfo.setNegativeSequence((String) scrappedInfo.get("noGoalsFestSeq"));
            goalsFestSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
            goalsFestSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
            goalsFestSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));

        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
        GoalsFestSeasonInfo insertedData = null;
        try {
            insertedData = (GoalsFestSeasonInfo) statsBySeasonService.insertStatsBySeasonInfo(goalsFestSeasonInfo);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return insertedData;
    }

    @PostMapping("/margin-wins-stats-by-team-season-fcstats")
    public LinkedHashMap<String, WinsMarginSeasonInfo> setMarginWinsStatsByTeamSeasonFC(@Valid @RequestParam  String teamName,
                                                                            @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                            @Valid @RequestParam(value = "end-season", required = false) String endSeason,
                                                                            @Valid @RequestParam(value = "url", required = false) String url) {
        LinkedHashMap<String, WinsMarginSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setSport("Football");
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        TeamEHhistoricData teamEHhistoricData = new TeamEHhistoricData();
        LinkedHashMap<String, Object> scrappedInfoMap = teamEHhistoricData.extractMarginWinsDataFromLastSeasonsFCStats(url);

        for (Map.Entry<String, Object> entry : scrappedInfoMap.entrySet()) {
            LinkedHashMap<String, Object> scrappedInfo = (LinkedHashMap<String, Object>) entry.getValue();
            WinsMarginSeasonInfo winsMarginSeasonInfo = new WinsMarginSeasonInfo();

            winsMarginSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));
            winsMarginSeasonInfo.setNumMarginWins((Integer) scrappedInfo.get("numMarginWins"));
            winsMarginSeasonInfo.setNumWins((Integer) scrappedInfo.get("numWins"));

            winsMarginSeasonInfo.setTeamId(team);
            winsMarginSeasonInfo.setSeason(entry.getKey());
            winsMarginSeasonInfo.setUrl(url);

            winsMarginSeasonInfo.setWinsRate((Double) scrappedInfo.get("totalWinsRate"));
            winsMarginSeasonInfo.setMarginWinsRate((Double) scrappedInfo.get("marginWinsRate"));
            winsMarginSeasonInfo.setNegativeSequence((String) scrappedInfo.get("noMarginWinsSeq"));
            winsMarginSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
            winsMarginSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
            winsMarginSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
            statsBySeasonService.insertStatsBySeasonInfo(winsMarginSeasonInfo);
            returnMap.put(entry.getKey(), winsMarginSeasonInfo);
        }

        return returnMap;
    }

    @PostMapping("/12margin-goal-stats-by-team-season")
    public LinkedHashMap<String, WinsMarginSeasonInfo> setMarginWinsStatsByTeamSeason(@Valid @RequestParam  String teamName,
                                                                          @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                          @Valid @RequestParam(value = "end-season", required = false) String endSeason,
                                                                          @Valid @RequestParam(value = "2016", required = false) Optional<String> url2016,
                                                                          @Valid @RequestParam(value = "2016-17", required = false) Optional<String> url201617,
                                                                          @Valid @RequestParam(value = "2017", required = false) Optional<String> url2017,
                                                                          @Valid @RequestParam(value = "2017-18", required = false) Optional<String> url201718,
                                                                          @Valid @RequestParam(value = "2018", required = false) Optional<String> url2018,
                                                                          @Valid @RequestParam(value = "2018-19", required = false) Optional<String> url201819,
                                                                          @Valid @RequestParam(value = "2019", required = false) Optional<String> url2019,
                                                                          @Valid @RequestParam(value = "2019-20", required = false) Optional<String> url201920,
                                                                          @Valid @RequestParam(value = "2020", required = false) Optional<String> url2020,
                                                                          @Valid @RequestParam(value = "2020-21", required = false) Optional<String> url202021,
                                                                          @Valid @RequestParam(value = "2021", required = false) Optional<String> url2021,
                                                                          @Valid @RequestParam(value = "2021-22", required = false) Optional<String> url202122,
                                                                          @Valid @RequestParam(value = "2022", required = false) Optional<String> url2022,
                                                                          @Valid @RequestParam(value = "2022-23", required = false) Optional<String> url202223,
                                                                          @Valid @RequestParam(value = "2023", required = false) Optional<String> url2023) {

        LinkedHashMap<String, WinsMarginSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        if (url2016.isPresent()) {
            returnMap.put("2016", insertWinsMarginBySeason(team, "2016", url2016.get()));
        }
        if (url201617.isPresent()) {
            returnMap.put("2016-17", insertWinsMarginBySeason(team, "2016-17", url201617.get()));
        }
        if (url2017.isPresent()) {
            returnMap.put("2017", insertWinsMarginBySeason(team, "2017", url2017.get()));
        }
        if (url201718.isPresent()) {
            returnMap.put("2017-18", insertWinsMarginBySeason(team, "2017-18", url201718.get()));
        }
        if (url2018.isPresent()) {
            returnMap.put("2018", insertWinsMarginBySeason(team, "2018", url2018.get()));
        }
        if (url201819.isPresent()) {
            returnMap.put("2018-19", insertWinsMarginBySeason(team, "2018-19", url201819.get()));
        }
        if (url2019.isPresent()) {
            returnMap.put("2019", insertWinsMarginBySeason(team, "2019", url2019.get()));
        }
        if (url201920.isPresent()) {
            returnMap.put("2019-20", insertWinsMarginBySeason(team, "2019-20", url201920.get()));
        }
        if (url2020.isPresent()) {
            returnMap.put("2020", insertWinsMarginBySeason(team, "2020", url2020.get()));
        }
        if (url202021.isPresent()) {
            returnMap.put("2020-21", insertWinsMarginBySeason(team, "2020-21", url202021.get()));
        }
        if (url2021.isPresent()) {
            returnMap.put("2021", insertWinsMarginBySeason(team, "2021", url2021.get()));
        }
        if (url202122.isPresent()) {
            returnMap.put("2021-22", insertWinsMarginBySeason(team, "2021-22", url202122.get()));
        }
        if (url2022.isPresent()) {
            returnMap.put("2022", insertWinsMarginBySeason(team, "2022", url2022.get()));
        }
        if (url202223.isPresent()) {
            returnMap.put("2022-23", insertWinsMarginBySeason(team, "2022-23", url202223.get()));
        }
        if (url2023.isPresent()) {
            returnMap.put("2023", insertWinsMarginBySeason(team, "2023", url2023.get()));
        }

        return returnMap;
    }

    private WinsMarginSeasonInfo insertWinsMarginBySeason (Team team, String season, String url) {
        TeamEHhistoricData teamEHhistoricData = new TeamEHhistoricData();
        LinkedHashMap<String, Object> scrappedInfo = null;
        WinsMarginSeasonInfo winsMarginSeasonInfo = new WinsMarginSeasonInfo();
        try {
            if (url.contains("team_matches")) {
                scrappedInfo = teamEHhistoricData.extract12MarginGoalsDataZZ(url);
                winsMarginSeasonInfo.setNumMatches(Integer.parseInt((String) scrappedInfo.get("totalMatches")));
                winsMarginSeasonInfo.setNumMarginWins(Integer.parseInt((String) scrappedInfo.get("numMarginWins")));
                winsMarginSeasonInfo.setNumWins(Integer.parseInt((String) scrappedInfo.get("numWins")));
            } else {
                scrappedInfo = teamEHhistoricData.extract12MarginGoalsDataFromFC(url);
                winsMarginSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));
                winsMarginSeasonInfo.setNumMarginWins((Integer) scrappedInfo.get("numMarginWins"));
                winsMarginSeasonInfo.setNumWins((Integer) scrappedInfo.get("numWins"));
            }

        } catch (Exception e) {
            return null;
        }

        winsMarginSeasonInfo.setTeamId(team);
        winsMarginSeasonInfo.setSeason(season);
        winsMarginSeasonInfo.setUrl(url);

        winsMarginSeasonInfo.setWinsRate((Double) scrappedInfo.get("totalWinsRate"));
        winsMarginSeasonInfo.setMarginWinsRate((Double) scrappedInfo.get("marginWinsRate"));
        winsMarginSeasonInfo.setNegativeSequence((String) scrappedInfo.get("noMarginWinsSeq"));
        winsMarginSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
        winsMarginSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
        winsMarginSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
        return (WinsMarginSeasonInfo) statsBySeasonService.insertStatsBySeasonInfo(winsMarginSeasonInfo);
    }



    ////////
    @PostMapping("/euro-handicap-stats-by-team-season-fcstats")
    public LinkedHashMap<String, EuroHandicapSeasonInfo> setEuroHandicapStatsByTeamSeasonFC(@Valid @RequestParam  String teamName,
                                                                                            @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                                            @Valid @RequestParam(value = "end-season", required = false) String endSeason,
                                                                                            @Valid @RequestParam(value = "url", required = false) String url) {
        LinkedHashMap<String, EuroHandicapSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setSport("Football");
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        TeamEHhistoricData teamEHhistoricData = new TeamEHhistoricData();
        LinkedHashMap<String, Object> scrappedInfoMap = teamEHhistoricData.extractEuroHandicapDataFromLastSeasonsFCStats(url);

        for (Map.Entry<String, Object> entry : scrappedInfoMap.entrySet()) {
            LinkedHashMap<String, Object> scrappedInfo = (LinkedHashMap<String, Object>) entry.getValue();
            EuroHandicapSeasonInfo euroHandicapSeasonInfo = new EuroHandicapSeasonInfo();

            euroHandicapSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));
            euroHandicapSeasonInfo.setNumMarginWins((Integer) scrappedInfo.get("numMarginWins"));
            euroHandicapSeasonInfo.setNumWins((Integer) scrappedInfo.get("numWins"));

            euroHandicapSeasonInfo.setTeamId(team);
            euroHandicapSeasonInfo.setSeason(entry.getKey());
            euroHandicapSeasonInfo.setUrl(url);

            euroHandicapSeasonInfo.setWinsRate((Double) scrappedInfo.get("totalWinsRate"));
            euroHandicapSeasonInfo.setMarginWinsRate((Double) scrappedInfo.get("marginWinsRate"));
            euroHandicapSeasonInfo.setNegativeSequence((String) scrappedInfo.get("noMarginWinsSeq"));
            euroHandicapSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
            euroHandicapSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
            euroHandicapSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
            statsBySeasonService.insertStatsBySeasonInfo(euroHandicapSeasonInfo);
            returnMap.put(entry.getKey(), euroHandicapSeasonInfo);
        }

        return returnMap;
    }

    @PostMapping("/euro-handicap-stats-by-team-season")
    public LinkedHashMap<String, EuroHandicapSeasonInfo> setEuroHandicapStatsByTeamSeason(@Valid @RequestParam  String teamName,
                                                                                      @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                                      @Valid @RequestParam(value = "end-season", required = false) String endSeason,
                                                                                      @Valid @RequestParam(value = "2016", required = false) Optional<String> url2016,
                                                                                      @Valid @RequestParam(value = "2016-17", required = false) Optional<String> url201617,
                                                                                      @Valid @RequestParam(value = "2017", required = false) Optional<String> url2017,
                                                                                      @Valid @RequestParam(value = "2017-18", required = false) Optional<String> url201718,
                                                                                      @Valid @RequestParam(value = "2018", required = false) Optional<String> url2018,
                                                                                      @Valid @RequestParam(value = "2018-19", required = false) Optional<String> url201819,
                                                                                      @Valid @RequestParam(value = "2019", required = false) Optional<String> url2019,
                                                                                      @Valid @RequestParam(value = "2019-20", required = false) Optional<String> url201920,
                                                                                      @Valid @RequestParam(value = "2020", required = false) Optional<String> url2020,
                                                                                      @Valid @RequestParam(value = "2020-21", required = false) Optional<String> url202021,
                                                                                      @Valid @RequestParam(value = "2021", required = false) Optional<String> url2021,
                                                                                      @Valid @RequestParam(value = "2021-22", required = false) Optional<String> url202122,
                                                                                      @Valid @RequestParam(value = "2022", required = false) Optional<String> url2022,
                                                                                      @Valid @RequestParam(value = "2022-23", required = false) Optional<String> url202223,
                                                                                      @Valid @RequestParam(value = "2023", required = false) Optional<String> url2023) {

        LinkedHashMap<String, EuroHandicapSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        if (url2016.isPresent()) {
            returnMap.put("2016", insertEuroHandicapBySeason(team, "2016", url2016.get()));
        }
        if (url201617.isPresent()) {
            returnMap.put("2016-17", insertEuroHandicapBySeason(team, "2016-17", url201617.get()));
        }
        if (url2017.isPresent()) {
            returnMap.put("2017", insertEuroHandicapBySeason(team, "2017", url2017.get()));
        }
        if (url201718.isPresent()) {
            returnMap.put("2017-18", insertEuroHandicapBySeason(team, "2017-18", url201718.get()));
        }
        if (url2018.isPresent()) {
            returnMap.put("2018", insertEuroHandicapBySeason(team, "2018", url2018.get()));
        }
        if (url201819.isPresent()) {
            returnMap.put("2018-19", insertEuroHandicapBySeason(team, "2018-19", url201819.get()));
        }
        if (url2019.isPresent()) {
            returnMap.put("2019", insertEuroHandicapBySeason(team, "2019", url2019.get()));
        }
        if (url201920.isPresent()) {
            returnMap.put("2019-20", insertEuroHandicapBySeason(team, "2019-20", url201920.get()));
        }
        if (url2020.isPresent()) {
            returnMap.put("2020", insertEuroHandicapBySeason(team, "2020", url2020.get()));
        }
        if (url202021.isPresent()) {
            returnMap.put("2020-21", insertEuroHandicapBySeason(team, "2020-21", url202021.get()));
        }
        if (url2021.isPresent()) {
            returnMap.put("2021", insertEuroHandicapBySeason(team, "2021", url2021.get()));
        }
        if (url202122.isPresent()) {
            returnMap.put("2021-22", insertEuroHandicapBySeason(team, "2021-22", url202122.get()));
        }
        if (url2022.isPresent()) {
            returnMap.put("2022", insertEuroHandicapBySeason(team, "2022", url2022.get()));
        }
        if (url202223.isPresent()) {
            returnMap.put("2022-23", insertEuroHandicapBySeason(team, "2022-23", url202223.get()));
        }
        if (url2023.isPresent()) {
            returnMap.put("2023", insertEuroHandicapBySeason(team, "2023", url2023.get()));
        }

        return returnMap;
    }

    private EuroHandicapSeasonInfo insertEuroHandicapBySeason (Team team, String season, String url) {
        TeamEHhistoricData teamEHhistoricData = new TeamEHhistoricData();
        LinkedHashMap<String, Object> scrappedInfo = null;
        EuroHandicapSeasonInfo euroHandicapSeasonInfo = new EuroHandicapSeasonInfo();
        try {
            if (url.contains("team_matches")) {
                scrappedInfo = teamEHhistoricData.extractEuroHandicapDataZZ(url);
                euroHandicapSeasonInfo.setNumMatches(Integer.parseInt((String) scrappedInfo.get("totalMatches")));
                euroHandicapSeasonInfo.setNumMarginWins(Integer.parseInt((String) scrappedInfo.get("numMarginWins")));
                euroHandicapSeasonInfo.setNumWins(Integer.parseInt((String) scrappedInfo.get("numWins")));
            } else {
                scrappedInfo = teamEHhistoricData.extractEuroHandicapDataFromFC(url);
                euroHandicapSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));
                euroHandicapSeasonInfo.setNumMarginWins((Integer) scrappedInfo.get("numMarginWins"));
                euroHandicapSeasonInfo.setNumWins((Integer) scrappedInfo.get("numWins"));
            }

        } catch (Exception e) {
            return null;
        }

        euroHandicapSeasonInfo.setTeamId(team);
        euroHandicapSeasonInfo.setSeason(season);
        euroHandicapSeasonInfo.setUrl(url);

        euroHandicapSeasonInfo.setWinsRate((Double) scrappedInfo.get("totalWinsRate"));
        euroHandicapSeasonInfo.setMarginWinsRate((Double) scrappedInfo.get("marginWinsRate"));
        euroHandicapSeasonInfo.setNegativeSequence((String) scrappedInfo.get("noMarginWinsSeq"));
        euroHandicapSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
        euroHandicapSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
        euroHandicapSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
        return (EuroHandicapSeasonInfo) statsBySeasonService.insertStatsBySeasonInfo(euroHandicapSeasonInfo);
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
