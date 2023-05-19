package com.api.BetStrat.controller;

import com.api.BetStrat.entity.ComebackSeasonInfo;
import com.api.BetStrat.entity.GoalsFestSeasonInfo;
import com.api.BetStrat.entity.ShortBasketWinsSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.service.ComebackSeasonInfoService;
import com.api.BetStrat.service.ShortBasketWinsSeasonInfoService;
import com.api.BetStrat.service.TeamService;
import com.api.BetStrat.util.BasketballScrappingData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

    @PostMapping("/updateTeamScore/{teamName}")
    public Team updateTeamScore (@PathVariable("teamName") String teamName, @Valid @RequestParam  String strategy) {
        return teamService.updateTeamScore(teamName, strategy);
    }

    @ApiOperation(value = "updateAllTeamsScoreBystrategy", notes = "Strate0gy values: hockeyDraw, hockeyWinsMarginAny2, hockeyWinsMargin3, footballDrawHunter, footballMarginWins, footballGoalsFest, footballEuroHandicap, basketComebacks")
    @PostMapping("/updateAllTeamsScoreBystrategy")
    public ResponseEntity<String> updateAllTeamsScoreBystrategy (@Valid @RequestParam  String strategy) {
        List<Team> allTeams = teamRepository.findAll();
        for (int i=0; i< allTeams.size(); i++) {
            try {
                teamService.updateTeamScore(allTeams.get(i).getName(), strategy);
            } catch (NumberFormatException er) {
                log.error(er.toString());
            }
        }
        return ResponseEntity.ok().body("OK");
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


    @ApiOperation(value = "getNBAStatsBySeason", notes = "get NBA stats by season. Testing comebacks strategy")
    @GetMapping("/nba-stats-by-season")
    public LinkedHashMap<String, GoalsFestSeasonInfo> getNBAStatsBySeason(@Valid @RequestParam  String season) {
        LinkedHashMap<String, GoalsFestSeasonInfo> returnMap = new LinkedHashMap<>();
        BasketballScrappingData basketballScrappedData = new BasketballScrappingData();
        LinkedHashMap<String, Object> scrappedInfo = null;


//        for (String month : months) {
//            String url = String.format("https://www.basketball-reference.com/leagues/NBA_%s_games-%s.html", season, month);
            scrappedInfo = basketballScrappedData.extractNBAFromBref(season);
//            returnMap.put(season, insertGoalsFestInfoBySeason(team, season, url));

        return returnMap;
    }

    @ApiOperation(value = "setComebacksStatsByTeamSeason", notes = "set comebacks stats from ESPN in bulk.")
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
