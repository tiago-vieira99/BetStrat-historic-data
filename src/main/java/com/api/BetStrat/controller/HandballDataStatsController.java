package com.api.BetStrat.controller;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.repository.football.DrawSeasonInfoRepository;
import com.api.BetStrat.repository.football.GoalsFestSeasonInfoRepository;
import com.api.BetStrat.repository.football.WinsMarginSeasonInfoRepository;
import com.api.BetStrat.service.TeamService;
import com.api.BetStrat.service.football.DrawSeasonInfoService;
import com.api.BetStrat.service.football.EuroHandicapSeasonInfoService;
import com.api.BetStrat.service.football.GoalsFestSeasonInfoService;
import com.api.BetStrat.service.football.WinsMarginSeasonInfoService;
import com.api.BetStrat.service.handball.HandballWinsMargin49SeasonInfoService;
import com.api.BetStrat.service.hockey.HockeyDrawSeasonInfoService;
import com.api.BetStrat.service.hockey.WinsMargin3SeasonInfoService;
import com.api.BetStrat.service.hockey.WinsMarginAny2SeasonInfoService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
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
@Api("Historical Data Analysis for Handball")
@RestController
@CrossOrigin
@RequestMapping("/api/bhd/handball")
public class HandballDataStatsController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private DrawSeasonInfoService drawSeasonInfoService;

    @Autowired
    private GoalsFestSeasonInfoService goalsFestSeasonInfoService;

    @Autowired
    private HockeyDrawSeasonInfoService hockeyDrawSeasonInfoService;

    @Autowired
    private WinsMarginSeasonInfoService winsMarginSeasonInfoService;

    @Autowired
    private WinsMarginAny2SeasonInfoService winsMarginAny2SeasonInfoService;

    @Autowired
    private WinsMargin3SeasonInfoService winsMargin3SeasonInfoService;

    @Autowired
    private EuroHandicapSeasonInfoService euroHandicapSeasonInfoService;

    @Autowired
    private HandballWinsMargin49SeasonInfoService handballWinsMargin49SeasonInfoService;

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
        return ResponseEntity.ok().body(allTeams.stream().filter(t -> t.getSport().equals("Handball")).collect(Collectors.toList()));
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
        team.setSport("Handball");
        Team newTeam = teamService.insertTeam(team);
        insertHistoricalMatches(newTeam.getId());
        return newTeam;
    }

    @SneakyThrows
    @PostMapping("/historicalMatchesResults")
    public void insertHistoricalMatches(@Valid @RequestParam Long teamId) {

        Team team = teamRepository.getOne(teamId);

        List<String> seasonsList = null;

        if (SUMMER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = SUMMER_SEASONS_LIST;
        } else if (WINTER_SEASONS_BEGIN_MONTH_LIST.contains(team.getBeginSeason())) {
            seasonsList = WINTER_SEASONS_LIST;
        }

        for (String season : seasonsList) {
            LOGGER.info(team.getName() + " - " + season);
            String teamUrl = team.getUrl();
            JSONArray scrappingData = null;
            String newSeasonUrl = "";

            if (teamUrl == null) {
                continue;
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
                    historicMatch.setCompetition(match.getString("competition"));
                    historicMatch.setSport(team.getSport());
                    historicMatch.setSeason(season);
                    try {
                        historicMatchRepository.save(historicMatch);
                    } catch (Exception e) {
                        log.info("match already inserted:  " + historicMatch.toString());
                    }
                }
            }
        }
    }

    @SneakyThrows
    @PostMapping("/historicalStatsData")
    public void updateHistoricalStatsData(@Valid @RequestParam Long teamId) {
        Team team = teamRepository.getOne(teamId);

        handballWinsMargin49SeasonInfoService.updateStatsDataInfo(team);

    }

    @GetMapping("/getHistoricMatches")
    public List<HistoricMatch> getHistoricMatches(@Valid @RequestParam  Long teamId, @Valid @RequestParam  String season) {
        return historicMatchRepository.getTeamMatchesBySeason(teamRepository.getOne(teamId), season);
    }

}
