package com.api.BetStrat.controller;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.basketball.ComebackSeasonInfo;
import com.api.BetStrat.entity.football.GoalsFestSeasonInfo;
import com.api.BetStrat.entity.basketball.ShortBasketWinsSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.handball.Handball712WinsMarginSeasonInfo;
import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.service.basketball.ComebackSeasonInfoService;
import com.api.BetStrat.service.basketball.ShortBasketWinsSeasonInfoService;
import com.api.BetStrat.service.TeamService;
import com.api.BetStrat.util.BasketballScrappingData;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.FBREF_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.SUMMER_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_BEGIN_MONTH_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WINTER_SEASONS_LIST;
import static com.api.BetStrat.constants.BetStratConstants.WORLDFOOTBALL_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.ZEROZERO_BASE_URL;
import static com.api.BetStrat.constants.BetStratConstants.ZEROZERO_SEASON_CODES;
import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

@Slf4j
@Api("Historical Data Analysis for Basketball")
@RestController
@CrossOrigin
@RequestMapping("/api/bhd/basket")
public class BasketballDataStatsController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private ComebackSeasonInfoService comebackSeasonInfoService;

    @Autowired
    private ShortBasketWinsSeasonInfoService shortBasketWinsSeasonInfoService;

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

    @ApiOperation(value = "get Team Comeback Wins Stats Info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/team-comebackwins-stats/{teamName}")
    public ResponseEntity<List<ComebackSeasonInfo>> getComebackWinsTeamStats(@PathVariable("teamName") String teamName) {
        List<ComebackSeasonInfo> teamStats = teamService.getTeamComebackWinsStats(teamName);
        return ResponseEntity.ok().body(teamStats);
    }

    @ApiOperation(value = "get Team Short Wins Stats Info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/team-shortwins-stats/{teamName}")
    public ResponseEntity<List<ShortBasketWinsSeasonInfo>> getShortWinsTeamStats(@PathVariable("teamName") String teamName) {
        List<ShortBasketWinsSeasonInfo> teamStats = teamService.getTeamShortWinsStats(teamName);
        return ResponseEntity.ok().body(teamStats);
    }

    @ApiOperation(value = "get Team Margin 7-12 Wins Stats Info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/team-margin712wins-stats/{teamName}")
    public ResponseEntity<List<Handball712WinsMarginSeasonInfo>> getMargin712WinsTeamStats(@PathVariable("teamName") String teamName) {
        List<Handball712WinsMarginSeasonInfo> teamStats = teamService.getTeamMargin712WinsStats(teamName);
        return ResponseEntity.ok().body(teamStats);
    }

    @PostMapping("/updateTeamScore/{teamName}")
    public Team updateTeamScore (@PathVariable("teamName") String teamName, @Valid @RequestParam  String strategy) {
        return teamService.updateTeamScore(teamName, strategy, "Basketball");
    }

    @SneakyThrows
    @PostMapping("/saveHistoricMatches")
    public void saveHistoricMatches(@Valid @RequestParam Long teamId) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Basketball")).collect(Collectors.toList());

//        for (int j = 25; j < allTeams.size(); j++) {
            insertHistoricalMatches(teamId);
//        }
    }

    @ApiOperation(value = "scrape all historic matches results for every seasons for the team", notes = "")
    @SneakyThrows
    @PostMapping("/historicalMatchesResults")
    public void insertHistoricalMatches(@Valid @RequestParam Long teamId) {

        Team team = teamRepository.getOne(teamId);

        List<String> seasonsList = Arrays.asList("2020-21");

//        if (SUMMER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
//            seasonsList = SUMMER_SEASONS_LIST;
//        } else if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
//            seasonsList = WINTER_SEASONS_LIST;
//        }

        for (String season : seasonsList) {
            LOGGER.info(team.getName() + " - " + season);
            String teamUrl = team.getUrl();
            JSONArray scrappingData = null;
            String newSeasonUrl = String.format("http://www.espn.com/nba/team/schedule/_/name/%s/season/%s/seasontype/2", teamUrl, "20" + season.split("-")[1]);

            if (teamUrl == null) {
                continue;
            }

            scrappingData = ScrappingUtil.getScrappingData(team.getName(), "20" + season.split("-")[1], newSeasonUrl, true);

            if (scrappingData != null) {
                for (int i = 0; i < scrappingData.length(); i++) {
                    JSONObject match = (JSONObject) scrappingData.get(i);
                    HistoricMatch historicMatch = new HistoricMatch();
                    historicMatch.setTeamId(team);
                    historicMatch.setMatchDate(match.getString("date"));
                    historicMatch.setHomeTeam(match.getString("homeTeam"));
                    historicMatch.setAwayTeam(match.getString("awayTeam"));
                    historicMatch.setHtResult(match.getString("ftResult").replaceAll("-",":").split(";")[0]);
                    historicMatch.setFtResult(match.getString("ftResult").replaceAll("-",":").split(";")[1]);
                    historicMatch.setCompetition(match.getString("competition"));
                    historicMatch.setSport(team.getSport());
                    historicMatch.setSeason(season);
                    try {
                        historicMatchRepository.save(historicMatch);
                    } catch (Exception e) {
                        log.info("\nERROR:  " + e.toString());
                    }
                }
            }
        }
    }

    @ApiOperation(value = "updateAllTeamsScoreBystrategy", notes = "Strategy values: hockeyDraw, hockeyWinsMarginAny2, hockeyWinsMargin3, footballDrawHunter, footballMarginWins, footballGoalsFest, footballEuroHandicap, basketComebacks, basketShortWins")
    @PostMapping("/updateAllTeamsScoreBystrategy")
    public ResponseEntity<String> updateAllTeamsScoreBystrategy (@Valid @RequestParam  String strategy) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Basketball")).collect(Collectors.toList());
        for (int i=0; i< allTeams.size(); i++) {
            try {
                teamService.updateTeamScore(allTeams.get(i).getName(), strategy, "Basketball");
            } catch (NumberFormatException er) {
                log.error(er.toString());
            }
        }
        return ResponseEntity.ok().body("OK");
    }

    @SneakyThrows
    @PostMapping("/historicalStatsData")
    public void updateHistoricalStatsData(@Valid @RequestParam Long teamId) {
//        Team team = teamRepository.getOne(teamId);

        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Basketball")).collect(Collectors.toList());
        for (Team team : allTeams) {
            comebackSeasonInfoService.updateStatsDataInfo(team);
            shortBasketWinsSeasonInfoService.updateStatsDataInfo(team);
        }
    }

    @PostMapping("/fullAnalysis")
    public ResponseEntity<String> fullAnalysis () {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Basketball")).collect(Collectors.toList());

        for (int i=0; i< allTeams.size(); i++) {
            log.info("handling " + allTeams.get(i).getName());
            try {
//                if (allTeams.get(i).getMarginWinsScore() == null || allTeams.get(i).getMarginWinsScore().contains("DATA")) {
//                    setMarginWinsStatsByTeamSeasonFC(allTeams.get(i).getName(), allTeams.get(i).getBeginSeason(), allTeams.get(i).getEndSeason(), allTeams.get(i).getUrl());
//                }
//                if (allTeams.get(i).getDrawsHunterScore() == null || allTeams.get(i).getDrawsHunterScore().contains("DATA")) {
//                    setDrawStatsByTeamSeasonFC(allTeams.get(i).getName(), allTeams.get(i).getBeginSeason(), allTeams.get(i).getEndSeason(), allTeams.get(i).getUrl());
//                }
//                setEuroHandicapStatsByTeamSeasonFC(allTeams.get(i).getName(), allTeams.get(i).getBeginSeason(), allTeams.get(i).getEndSeason(), allTeams.get(i).getUrl());
            } catch (Exception er) {
                log.error(er.toString());
            }
        }
        return ResponseEntity.ok().body("OK");
    }


    @ApiOperation(value = "setComebacksStatsByTeamSeason", notes = "set comebacks stats")
    @PostMapping("/comebacks-stats-by-team-season")
    public LinkedHashMap<String, ComebackSeasonInfo> setGoalsFestStatsByTeamSeason(@Valid @RequestParam  String teamName,
                                                                                    @Valid @RequestParam  String teamPath,
                                                                                    @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                                    @Valid @RequestParam(value = "end-season", required = false) String endSeason) {

        LinkedHashMap<String, ComebackSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setSport("Basketball");
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        List<String> summerSeasonss = Arrays.asList("2016", "2017", "2018", "2019", "2020", "2021", "2022");
        for (String season : summerSeasonss) {
            String url = String.format("https://www.espn.com/nba/team/schedule/_/name/%s/season/%s/seasontype/2", teamPath, season);
            returnMap.put(season, insertComebacksInfoBySeason(team, season, url));
        }

        return returnMap;
    }

    private ComebackSeasonInfo insertComebacksInfoBySeason (Team team, String season, String url) {
        BasketballScrappingData basketballScrappingData = new BasketballScrappingData();
        LinkedHashMap<String, Object> scrappedInfo = null;
        ComebackSeasonInfo comebackSeasonInfo = new ComebackSeasonInfo();
        try {
            scrappedInfo = basketballScrappingData.extractNBAComebacksFromESPN(url);
            comebackSeasonInfo.setComebacksRate((Double) scrappedInfo.get("comebacksRate"));
            comebackSeasonInfo.setNumComebacks((Integer) scrappedInfo.get("totalComebacks"));
            comebackSeasonInfo.setWinsRate((Double) scrappedInfo.get("winsRate"));
            comebackSeasonInfo.setNumWins((Integer) scrappedInfo.get("totalWins"));
            comebackSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));
            comebackSeasonInfo.setTeamId(team);
            comebackSeasonInfo.setSeason(season);
            comebackSeasonInfo.setUrl(url);

            comebackSeasonInfo.setNoComebacksSequence((String) scrappedInfo.get("noComebacksSequence"));
            comebackSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
            comebackSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
            comebackSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));

        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
        ComebackSeasonInfo insertedData = null;
        try {
            insertedData = comebackSeasonInfoService.insertComebackInfo(comebackSeasonInfo);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return insertedData;
    }

    @ApiOperation(value = "setShortWinsStatsByTeamSeason", notes = "set short wins (<6pts) stats from ESPN in bulk.")
    @PostMapping("/shortwins-stats-by-team-season")
    public LinkedHashMap<String, ShortBasketWinsSeasonInfo> setShortWinsStatsByTeamSeason(@Valid @RequestParam  String teamName,
                                                                                   @Valid @RequestParam  String teamPath,
                                                                                   @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                                   @Valid @RequestParam(value = "end-season", required = false) String endSeason) {

        LinkedHashMap<String, ShortBasketWinsSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setSport("Basketball");
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        List<String> summerSeasonss = Arrays.asList("2016", "2017", "2018", "2019", "2020", "2021", "2022");
        for (String season : summerSeasonss) {
            String url = String.format("https://www.espn.com/nba/team/schedule/_/name/%s/season/%s/seasontype/2", teamPath, season);
            returnMap.put(season, insertShortWinsInfoBySeason(team, season, url));
        }

        return returnMap;
    }

    private ShortBasketWinsSeasonInfo insertShortWinsInfoBySeason (Team team, String season, String url) {
        BasketballScrappingData basketballScrappingData = new BasketballScrappingData();
        LinkedHashMap<String, Object> scrappedInfo = null;
        ShortBasketWinsSeasonInfo shortBasketWinsSeasonInfo = new ShortBasketWinsSeasonInfo();
        try {
            scrappedInfo = basketballScrappingData.extractNBAShortWinsFromESPN(url);
            shortBasketWinsSeasonInfo.setShortWinsRate((Double) scrappedInfo.get("shortWinsRate"));
            shortBasketWinsSeasonInfo.setNumShortWins((Integer) scrappedInfo.get("totalShortWins"));
            shortBasketWinsSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));
            shortBasketWinsSeasonInfo.setTeamId(team);
            shortBasketWinsSeasonInfo.setSeason(season);
            shortBasketWinsSeasonInfo.setUrl(url);

            shortBasketWinsSeasonInfo.setNoShortWinsSequence((String) scrappedInfo.get("noShortWinsSequence"));
            shortBasketWinsSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
            shortBasketWinsSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
            shortBasketWinsSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));

        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
        ShortBasketWinsSeasonInfo insertedData = null;
        try {
            insertedData = shortBasketWinsSeasonInfoService.insertShortWinsInfo(shortBasketWinsSeasonInfo);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return insertedData;
    }
}
