package com.api.BetStrat.controller;

import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.entity.WinsMarginSeasonInfo;
import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.service.DrawSeasonInfoService;
import com.api.BetStrat.service.TeamService;
import com.api.BetStrat.service.WinsMarginSeasonInfoService;
import com.api.BetStrat.util.TeamDFhistoricData;
import com.api.BetStrat.util.TeamEHhistoricData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Api("Historical Data Analysis")
@RestController
@CrossOrigin
@RequestMapping("/api/bhd")
public class DataAnalysisController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private DrawSeasonInfoService drawSeasonInfoService;

    @Autowired
    private WinsMarginSeasonInfoService winsMarginSeasonInfoService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TeamRepository teamRepository;

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
    public Team updateTeamScore (@PathVariable("teamName") String teamName) {
        return teamService.updateTeamScore(teamName);
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
//                winsMarginSeasonInfo.setDrawRate(Double.parseDouble((String) scrappedInfo.get("drawRate")));
//                winsMarginSeasonInfo.setNumDraws(Integer.parseInt((String) scrappedInfo.get("totalDraws")));
                winsMarginSeasonInfo.setNumMatches(Integer.parseInt((String) scrappedInfo.get("totalMatches")));
            } else {
//                scrappedInfo = teamDFhistoricData.extractDFDataFromFC(url);
//                winsMarginSeasonInfo.setDrawRate((Double) scrappedInfo.get("drawRate"));
//                winsMarginSeasonInfo.setNumDraws((Integer) scrappedInfo.get("totalDraws"));
//                winsMarginSeasonInfo.setNumMatches((Integer) scrappedInfo.get("totalMatches"));
            }

        } catch (Exception e) {
            return null;
        }

        winsMarginSeasonInfo.setTeamId(team);
        winsMarginSeasonInfo.setSeason(season);
        winsMarginSeasonInfo.setUrl(url);

        winsMarginSeasonInfo.setWinsRate((Double) scrappedInfo.get("totalWinsRate"));
        winsMarginSeasonInfo.setNumMarginWins(Integer.parseInt((String) scrappedInfo.get("numMarginWins")));
        winsMarginSeasonInfo.setNumWins(Integer.parseInt((String) scrappedInfo.get("numWins")));
        winsMarginSeasonInfo.setMarginWinsRate((Double) scrappedInfo.get("marginWinsRate"));
        winsMarginSeasonInfo.setNoMarginWinsSequence((String) scrappedInfo.get("noMarginWinsSeq"));
        winsMarginSeasonInfo.setStdDeviation((Double) scrappedInfo.get("standardDeviation"));
        winsMarginSeasonInfo.setCoefDeviation((Double) scrappedInfo.get("coefficientVariation"));
        winsMarginSeasonInfo.setCompetition((String) scrappedInfo.get("competition"));
        return winsMarginSeasonInfoService.insertWinsMarginInfo(winsMarginSeasonInfo);
    }

//    @GetMapping("/draw-stats-by-team-season")
//    public LinkedHashMap<String, LinkedHashMap> getDrawStatsByTeamSeason(@Valid @RequestParam  Optional<String> url1, @Valid @RequestParam  Optional<String> url2
//            , @Valid @RequestParam  Optional<String> url3, @Valid @RequestParam  Optional<String> url4, @Valid @RequestParam  Optional<String> url5) {
//        TeamDFhistoricDataZZ teamDFhistoricDataZZ = new TeamDFhistoricDataZZ();
//        LinkedHashMap<String, LinkedHashMap> returnMap = new LinkedHashMap<>();
//
//        if(url1.isPresent()) {returnMap.put("url1",teamDFhistoricDataZZ.extractDFData(url1.get())); }
//        if(url2.isPresent()) {returnMap.put("url2",teamDFhistoricDataZZ.extractDFData(url2.get())); }
//        if(url3.isPresent()) {returnMap.put("url3",teamDFhistoricDataZZ.extractDFData(url3.get())); }
//        if(url4.isPresent()) {returnMap.put("url4",teamDFhistoricDataZZ.extractDFData(url4.get())); }
//        if(url5.isPresent()) {returnMap.put("url5",teamDFhistoricDataZZ.extractDFData(url5.get())); }
//
//        return returnMap;
//    }

//    @GetMapping("/euro-handicap-stats-by-team-season")
//    public LinkedHashMap<String, LinkedHashMap> getEuroHandicapStatsByTeamSeason(@Valid @RequestParam  Optional<String> url1, @Valid @RequestParam  Optional<String> url2
//            , @Valid @RequestParam  Optional<String> url3, @Valid @RequestParam  Optional<String> url4, @Valid @RequestParam  Optional<String> url5) {
//        TeamEHhistoricDataZZ teamEHhistoricDataZZ = new TeamEHhistoricDataZZ();
//        LinkedHashMap<String, LinkedHashMap> returnMap = new LinkedHashMap<>();
//
//        if(url1.isPresent()) {returnMap.put("url1",teamEHhistoricDataZZ.extractEHData(url1.get())); }
//        if(url2.isPresent()) {returnMap.put("url2",teamEHhistoricDataZZ.extractEHData(url2.get())); }
//        if(url3.isPresent()) {returnMap.put("url3",teamEHhistoricDataZZ.extractEHData(url3.get())); }
//        if(url4.isPresent()) {returnMap.put("url4",teamEHhistoricDataZZ.extractEHData(url4.get())); }
//        if(url5.isPresent()) {returnMap.put("url5",teamEHhistoricDataZZ.extractEHData(url5.get())); }
//
//        return returnMap;
//    }
//
//    @GetMapping("/12margin-goal-stats-by-team-season")
//    public LinkedHashMap<String, LinkedHashMap> get12MarginGoalStatsByTeamSeason(@Valid @RequestParam  Optional<String> url1, @Valid @RequestParam  Optional<String> url2
//        , @Valid @RequestParam  Optional<String> url3, @Valid @RequestParam  Optional<String> url4, @Valid @RequestParam Optional<String> url5) {
//        TeamEHhistoricDataZZ teamEHhistoricDataZZ = new TeamEHhistoricDataZZ();
//        LinkedHashMap<String, LinkedHashMap> returnMap = new LinkedHashMap<>();
//
//        if(url1.isPresent()) {returnMap.put("url1",teamEHhistoricDataZZ.extract12MarginGoalsData(url1.get())); }
//        if(url2.isPresent()) {returnMap.put("url2",teamEHhistoricDataZZ.extract12MarginGoalsData(url2.get())); }
//        if(url3.isPresent()) {returnMap.put("url3",teamEHhistoricDataZZ.extract12MarginGoalsData(url3.get())); }
//        if(url4.isPresent()) {returnMap.put("url4",teamEHhistoricDataZZ.extract12MarginGoalsData(url4.get())); }
//        if(url5.isPresent()) {returnMap.put("url5",teamEHhistoricDataZZ.extract12MarginGoalsData(url5.get())); }
//
//        return returnMap;
//    }

//    @GetMapping("/sequence")
//    public LinkedHashMap<String, String> getSequenceAnalysis(@Valid @RequestParam String sequence) {
//        TeamDFhistoricDataZZ teamDFhistoricDataZZ = new TeamDFhistoricDataZZ();
//        return teamDFhistoricDataZZ.sequenceAnalysis(sequence);
//    }
}
