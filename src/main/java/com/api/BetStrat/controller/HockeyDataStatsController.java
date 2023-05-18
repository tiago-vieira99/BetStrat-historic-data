package com.api.BetStrat.controller;

import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.HockeyDrawSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.WinsMarginSeasonInfo;
import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.service.DrawSeasonInfoService;
import com.api.BetStrat.service.HockeyDrawSeasonInfoService;
import com.api.BetStrat.service.TeamService;
import com.api.BetStrat.service.WinsMargin3SeasonInfoService;
import com.api.BetStrat.service.WinsMarginAny2SeasonInfoService;
import com.api.BetStrat.service.WinsMarginSeasonInfoService;
import com.api.BetStrat.util.HockeyEurohockeyScrappingData;
import com.api.BetStrat.util.TeamDFhistoricData;
import com.api.BetStrat.util.TeamEHhistoricData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.HOCKEY_SEASONS_LIST;

@Slf4j
@Api("Historical Data Analysis for Hockey")
@RestController
@CrossOrigin
@RequestMapping("/api/hockey")
public class HockeyDataStatsController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private DrawSeasonInfoService drawSeasonInfoService;

    @Autowired
    private HockeyDrawSeasonInfoService hockeyDrawSeasonInfoService;

    @Autowired
    private WinsMarginSeasonInfoService winsMarginSeasonInfoService;

    @Autowired
    private WinsMarginAny2SeasonInfoService winsMarginAny2SeasonInfoService;

    @Autowired
    private WinsMargin3SeasonInfoService winsMargin3SeasonInfoService;

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

    @ApiOperation(value = "get Hockey Team Draw Stats")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ArrayList.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/team-hockey-draw-stats/{teamName}")
    public ResponseEntity<List<HockeyDrawSeasonInfo>> getHockeyTeamStats(@PathVariable("teamName") String teamName) {
        List<HockeyDrawSeasonInfo> teamStats = teamService.getHockeyTeamDrawStats(teamName);
        return ResponseEntity.ok().body(teamStats);
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

            drawSeasonInfo.setNoDrawsSequence((String) scrappedInfo.get("noDrawsSeq"));
            drawSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
            drawSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
            drawSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
            drawSeasonInfoService.insertDrawInfo(drawSeasonInfo);
            returnMap.put(entry.getKey(), drawSeasonInfo);
        }

        return returnMap;
    }

    @PostMapping("/hockey-stats-bulk-by-league-from-eurohockey")
    public LinkedHashMap<String, Object> setHockeyStatsBulkByLeagueSeasonFromEurohockey(@Valid @RequestParam  String leagueName, @Valid @RequestParam  String country,
                                                                                            @Valid @RequestParam  String season,
                                                                                            @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                                            @Valid @RequestParam(value = "end-season", required = false) String endSeason,
                                                                                            @Valid @RequestParam String leagueURL) throws UnsupportedEncodingException {
        LinkedHashMap<String, Object> returnMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> matchesURLsMap = new LinkedHashMap<>();

        HttpPost httppost = new HttpPost("http://34.125.116.128:8880/api/league/new");
        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair("country", country));
        params.add(new BasicNameValuePair("name", leagueName));
        params.add(new BasicNameValuePair("season", season));
        params.add(new BasicNameValuePair("sport", "Hockey"));
        params.add(new BasicNameValuePair("url", leagueURL));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        try (CloseableHttpResponse response = httpClient.execute(httppost)) {
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    // do something useful
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        HockeyEurohockeyScrappingData hockeyEurohockeyScrappingData = new HockeyEurohockeyScrappingData();
        for (String hockeySeason : HOCKEY_SEASONS_LIST) {
            String leagueUrlBySeason = leagueURL.replace(leagueURL.substring(leagueURL.indexOf("=")+1), hockeySeason.substring(5));
            LinkedHashMap<String, Object> allMatchesURLsBySeason = hockeyEurohockeyScrappingData.extractTeamsURLBySeason(leagueUrlBySeason);
            matchesURLsMap.put(hockeySeason, allMatchesURLsBySeason);
        }

        for (Map.Entry<String,Object> entry : matchesURLsMap.entrySet()) {
            String seasonn = entry.getKey();
            LinkedHashMap<String, Object> teamMatchesURLMap = (LinkedHashMap<String, Object>) entry.getValue();

            for (Map.Entry<String,Object> teamEntry : teamMatchesURLMap.entrySet()) {
                String teamName = teamEntry.getKey();
                log.info(seasonn + " -> " + teamName);
                Team team = teamRepository.getTeamByName(teamName);
                if (team == null) {
                    team = new Team();
                    team.setName(teamName);
                    team.setSport("Hockey");
                    team.setBeginSeason(beginSeason);
                    team.setEndSeason(endSeason);
                    teamService.insertTeam(team);
                }

                //draws hunter stats
                LinkedHashMap<String, Object> scrappedInfo = hockeyEurohockeyScrappingData.buildDrawStatsMap((List<Node>) teamEntry.getValue());
                HockeyDrawSeasonInfo hockeyDrawSeasonInfo = new HockeyDrawSeasonInfo();
                try {
                    hockeyDrawSeasonInfo.setDrawRate((Double) scrappedInfo.get("drawRate"));
                    hockeyDrawSeasonInfo.setNumDraws((Integer) scrappedInfo.get("totalDraws"));
                    hockeyDrawSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));

                } catch (Exception e) {
                    return null;
                }

                hockeyDrawSeasonInfo.setTeamId(team);
                hockeyDrawSeasonInfo.setSeason(seasonn);
//                    hockeyDrawSeasonInfo.setUrl(url);

                hockeyDrawSeasonInfo.setNoDrawsSequence((String) scrappedInfo.get("noDrawsSeq"));
                hockeyDrawSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
                hockeyDrawSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
                hockeyDrawSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
                try {
                    hockeyDrawSeasonInfoService.insertDrawInfo(hockeyDrawSeasonInfo);
                } catch (DataIntegrityViolationException er) {
                    log.error(er.toString());
                }

//                ///////////margin wins 2-3
//                LinkedHashMap<String, Object> MW23scrappedInfo = hockeyEurohockeyScrappingData.build23MarginWinStatsMap((List<Node>) teamEntry.getValue(), teamName);
//                WinsMarginSeasonInfo winsMarginSeasonInfo = new WinsMarginSeasonInfo();
//
//                winsMarginSeasonInfo.setNumMatches((Integer) MW23scrappedInfo.get("totalMatches"));
//                winsMarginSeasonInfo.setNumMarginWins((Integer) MW23scrappedInfo.get("numMarginWins"));
//                winsMarginSeasonInfo.setNumWins((Integer) MW23scrappedInfo.get("numWins"));
//
//                winsMarginSeasonInfo.setTeamId(team);
//                winsMarginSeasonInfo.setSeason(entry.getKey());
////                winsMarginSeasonInfo.setUrl(url);
//
//                winsMarginSeasonInfo.setWinsRate((Double) MW23scrappedInfo.get("totalWinsRate"));
//                winsMarginSeasonInfo.setMarginWinsRate((Double) MW23scrappedInfo.get("marginWinsRate"));
//                winsMarginSeasonInfo.setNoMarginWinsSequence((String) MW23scrappedInfo.get("noMarginWinsSeq"));
//                winsMarginSeasonInfo.setStdDeviation((Double) MW23scrappedInfo.get("standardDeviation"));
//                winsMarginSeasonInfo.setCoefDeviation((Double) MW23scrappedInfo.get("coefficientVariation"));
//                winsMarginSeasonInfo.setCompetition((String) MW23scrappedInfo.get("competition"));
//                try {
//                    winsMarginSeasonInfoService.insertWinsMarginInfo(winsMarginSeasonInfo);
//                } catch (DataIntegrityViolationException er) {
//                    log.error(er.toString());
//                }
//
//                //////margin wins any 2
//                LinkedHashMap<String, Object> MWAny2scrappedInfo = hockeyEurohockeyScrappingData.buildAny2MarginWinStatsMap((List<Node>) teamEntry.getValue(), teamName);
//                WinsMarginAny2SeasonInfo winsMarginAny2SeasonInfo = new WinsMarginAny2SeasonInfo();
//
//                winsMarginAny2SeasonInfo.setNumMatches((Integer) MWAny2scrappedInfo.get("totalMatches"));
//                winsMarginAny2SeasonInfo.setNumMarginWins((Integer) MWAny2scrappedInfo.get("numMarginWins"));
//                winsMarginAny2SeasonInfo.setNumWins((Integer) MWAny2scrappedInfo.get("numWins"));
//
//                winsMarginAny2SeasonInfo.setTeamId(team);
//                winsMarginAny2SeasonInfo.setSeason(entry.getKey());
////                winsMarginSeasonInfo.setUrl(url);
//
//                winsMarginAny2SeasonInfo.setWinsRate((Double) MWAny2scrappedInfo.get("totalWinsRate"));
//                winsMarginAny2SeasonInfo.setMarginWinsRate((Double) MWAny2scrappedInfo.get("marginWinsRate"));
//                winsMarginAny2SeasonInfo.setNoMarginWinsSequence((String) MWAny2scrappedInfo.get("noMarginWinsSeq"));
//                winsMarginAny2SeasonInfo.setStdDeviation((Double) MWAny2scrappedInfo.get("standardDeviation"));
//                winsMarginAny2SeasonInfo.setCoefDeviation((Double) MWAny2scrappedInfo.get("coefficientVariation"));
//                winsMarginAny2SeasonInfo.setCompetition((String) MWAny2scrappedInfo.get("competition"));
//                try {
//                    winsMarginAny2SeasonInfoService.insertWinsMarginInfo(winsMarginAny2SeasonInfo);
//                } catch (DataIntegrityViolationException er) {
//                    log.error(er.toString());
//                }
//
//                //////margin wins by 3
//                LinkedHashMap<String, Object> MW3scrappedInfo = hockeyEurohockeyScrappingData.buildMargin3WinStatsMap((List<Node>) teamEntry.getValue(), teamName);
//                WinsMargin3SeasonInfo winsMargin3SeasonInfo = new WinsMargin3SeasonInfo();
//
//                winsMargin3SeasonInfo.setNumMatches((Integer) MW3scrappedInfo.get("totalMatches"));
//                winsMargin3SeasonInfo.setNumMarginWins((Integer) MW3scrappedInfo.get("numMarginWins"));
//                winsMargin3SeasonInfo.setNumWins((Integer) MW3scrappedInfo.get("numWins"));
//
//                winsMargin3SeasonInfo.setTeamId(team);
//                winsMargin3SeasonInfo.setSeason(entry.getKey());
////                winsMarginSeasonInfo.setUrl(url);
//
//                winsMargin3SeasonInfo.setWinsRate((Double) MW3scrappedInfo.get("totalWinsRate"));
//                winsMargin3SeasonInfo.setMarginWinsRate((Double) MW3scrappedInfo.get("marginWinsRate"));
//                winsMargin3SeasonInfo.setNoMarginWinsSequence((String) MW3scrappedInfo.get("noMarginWinsSeq"));
//                winsMargin3SeasonInfo.setStdDeviation((Double) MW3scrappedInfo.get("standardDeviation"));
//                winsMargin3SeasonInfo.setCoefDeviation((Double) MW3scrappedInfo.get("coefficientVariation"));
//                winsMargin3SeasonInfo.setCompetition((String) MW3scrappedInfo.get("competition"));
//                try {
//                    winsMargin3SeasonInfoService.insertWinsMarginInfo(winsMargin3SeasonInfo);
//                } catch (DataIntegrityViolationException er) {
//                    log.error(er.toString());
//                }
            }

        }

        return returnMap;
    }

    @PostMapping("/hockey-draw-stats-bulk-by-league")
    public LinkedHashMap<String, HockeyDrawSeasonInfo> setHockeyDrawStatsBulkByLeagueSeason(@Valid @RequestParam  String leagueName, @Valid @RequestParam  String country,
                                                                                            @Valid @RequestParam  String season,
                                                                                      @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                                      @Valid @RequestParam(value = "end-season", required = false) String endSeason,
                                                                                      @Valid @RequestParam String url) throws UnsupportedEncodingException {
        LinkedHashMap<String, HockeyDrawSeasonInfo> returnMap = new LinkedHashMap<>();

        HttpPost httppost = new HttpPost("http://betstrat-coreapp:8080/api/league/new");
        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair("country", country));
        params.add(new BasicNameValuePair("name", leagueName));
        params.add(new BasicNameValuePair("season", season));
        params.add(new BasicNameValuePair("sport", "Hockey"));
        params.add(new BasicNameValuePair("url", url));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        try (CloseableHttpResponse response = httpClient.execute(httppost)) {
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    // do something useful
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Document originalDocument = null;

        try {
            originalDocument = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("erro ao tentar conectar com Jsoup -> {}", e.getMessage());
            log.error(e.toString());
        }

        List<Node> standingsTableTeams = originalDocument.getElementsByAttributeValueContaining("class", "standings").get(0).childNode(3).childNodes().stream().filter(c -> c.childNodes().size() > 3).collect(Collectors.toList());

        for (Node team : standingsTableTeams) {
            String teamUrl = team.childNode(3).childNode(1).attr("href");
            String teamName = team.childNode(3).childNode(1).childNode(0).toString().trim();

            setHockeyDrawStatsByTeamSeason(teamName, beginSeason, endSeason, teamUrl);
        }

        return returnMap;
    }

    @PostMapping("/hockey-draw-stats-by-team")
    public LinkedHashMap<String, HockeyDrawSeasonInfo> setHockeyDrawStatsByTeamSeason(@Valid @RequestParam  String teamName,
                                                                            @Valid @RequestParam(value = "begin_season", required = false) String beginSeason,
                                                                            @Valid @RequestParam(value = "end-season", required = false) String endSeason,
                                                                            @Valid @RequestParam(value = "url", required = false) String url) {
        LinkedHashMap<String, HockeyDrawSeasonInfo> returnMap = new LinkedHashMap<>();

        Team team = teamRepository.getTeamByName(teamName);
        if (team == null) {
            team = new Team();
            team.setName(teamName);
            team.setSport("Hockey");
            team.setBeginSeason(beginSeason);
            team.setEndSeason(endSeason);
            teamService.insertTeam(team);
        }

        TeamDFhistoricData teamDFhistoricData = new TeamDFhistoricData();
        LinkedHashMap<String, Object> scrappedInfoMap = teamDFhistoricData.extractHockeyDFDataFromLastSeasons(url);

        for (Map.Entry<String,Object> entry : scrappedInfoMap.entrySet()){
            LinkedHashMap<String, Object> scrappedInfo = (LinkedHashMap<String, Object>) entry.getValue();
            HockeyDrawSeasonInfo hockeyDrawSeasonInfo = new HockeyDrawSeasonInfo();
            try {
                hockeyDrawSeasonInfo.setDrawRate((Double) scrappedInfo.get("drawRate"));
                hockeyDrawSeasonInfo.setNumDraws((Integer) scrappedInfo.get("totalDraws"));
                hockeyDrawSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));

            } catch (Exception e) {
                return null;
            }

            hockeyDrawSeasonInfo.setTeamId(team);
            hockeyDrawSeasonInfo.setSeason(entry.getKey());
            hockeyDrawSeasonInfo.setUrl(url);

            hockeyDrawSeasonInfo.setNoDrawsSequence((String) scrappedInfo.get("noDrawsSeq"));
            hockeyDrawSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
            hockeyDrawSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
            hockeyDrawSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
            hockeyDrawSeasonInfoService.insertDrawInfo(hockeyDrawSeasonInfo);
            returnMap.put(entry.getKey(), hockeyDrawSeasonInfo);
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

        drawSeasonInfo.setNoDrawsSequence((String) scrappedInfo.get("noDrawsSeq"));
        drawSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
        drawSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
        drawSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
        return drawSeasonInfoService.insertDrawInfo(drawSeasonInfo);
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
            winsMarginSeasonInfo.setNoMarginWinsSequence((String) scrappedInfo.get("noMarginWinsSeq"));
            winsMarginSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
            winsMarginSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
            winsMarginSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
            winsMarginSeasonInfoService.insertWinsMarginInfo(winsMarginSeasonInfo);
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
        winsMarginSeasonInfo.setNoMarginWinsSequence((String) scrappedInfo.get("noMarginWinsSeq"));
        winsMarginSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
        winsMarginSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
        winsMarginSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
        return winsMarginSeasonInfoService.insertWinsMarginInfo(winsMarginSeasonInfo);
    }
}
