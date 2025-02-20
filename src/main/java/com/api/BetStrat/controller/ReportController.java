package com.api.BetStrat.controller;

import com.api.BetStrat.entity.Report;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.repository.ReportRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.service.StrategySeasonStatsService;
import com.api.BetStrat.service.TeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.*;

@Slf4j
@Api("Historical Data Analysis for Football")
@RestController
@CrossOrigin
@RequestMapping("/api/bhd/report")
public class ReportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private StrategySeasonStatsService strategySeasonStatsService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ReportRepository reportRepository;


    @ApiOperation(value = "get simulation scores by Strategy and Season")
    @GetMapping("/simulate-by-strategy/score-by-season")
    public ResponseEntity<HashMap> simulateScoreStrategyBySeason (@Valid @RequestParam  String strategy, @Valid @RequestParam String season) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());
        HashMap<String, String> returnMap = new HashMap<>();

        for (Team team : allTeams) {
            //simulate score for desired season
            String scoreBySeason = strategySeasonStatsService.calculateScoreBySeason(team, season, strategy.concat("SeasonStats"));

            returnMap.put(team.getName(), scoreBySeason);
        }

        return ResponseEntity.ok().body(returnMap);
    }

    @ApiOperation(value = "get simulation Team by Strategy and Season")
    @GetMapping("/simulate-by-strategy/")
    public ResponseEntity<HashMap> simulateStrategyBySeason (@Valid @RequestParam  String strategy, @Valid @RequestParam  String teamName, @Valid @RequestParam String season) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (teamByName == null) {
            throw new NotFoundException();
        }

        //simulate score for desired season
        String scoreBySeason = strategySeasonStatsService.calculateScoreBySeason(teamByName, season, strategy.concat("SeasonStats"));


        HashMap list = strategySeasonStatsService.getSimulatedMatchesByStrategyAndSeason(season, teamByName, strategy.concat("SeasonStats"));
        list.put("scoreBySeason", scoreBySeason);

        return ResponseEntity.ok().body(list);
    }

    @ApiOperation(value = "get simulation matches of All Teams by Strategy and Season")
    @GetMapping("/simulate-all-teams-by-strategy/")
    public ResponseEntity<HashMap> simulateAllTeamsStrategyBySeason (@Valid @RequestParam  String strategy, @Valid @RequestParam String season) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());
        HashMap<String, Map> returnMap = new HashMap<>();

        for (Team team : allTeams) {
            //simulate score for desired season
            String scoreBySeason = strategySeasonStatsService.calculateScoreBySeason(team, season, strategy.concat("SeasonStats"));

            if (!scoreBySeason.contains("EXCE")) {
                continue;
            }

            HashMap list = strategySeasonStatsService.getSimulatedMatchesByStrategyAndSeason(season, team, strategy.concat("SeasonStats"));
            list.put("scoreBySeason", scoreBySeason);
            returnMap.put(team.getName(), list);
        }

        return ResponseEntity.ok().body(returnMap);
    }

    @ApiOperation(value = "insert and save reports of all teams for all strategies for Season", notes = "Strategy values:\n\"Draw\",\"GoalsFest\",\"WinsMargin\",\"Btts\", \"CleanSheet\", \n" +
        "            \"ConcedeBothHalves\", \"EuroHandicap\", \"NoBtts\", \"NoGoalsFest\", \"NoWins\", \"ScoreBothHalves\", \n" +
        "            \"SecondHalfBigger\", \"WinAndGoals\", \"WinBothHalves\", \"Wins\"")
    @PostMapping("/{season}/{strategy}")
    public ResponseEntity<Object> insertReportsBySeason (@PathVariable("season") String season, @PathVariable("strategy") String strat) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());

        HashMap<String, Map> reportMap = new HashMap<>();

        String strategy = strat.concat("SeasonStats");
        log.info("handling " + strategy);
        reportMap.put(strategy, new HashMap<String, List>());

        for (Team team : allTeams) {
            //simulate score for desired season
            String scoreBySeason = strategySeasonStatsService.calculateScoreBySeason(team, season, strategy);

            if (scoreBySeason.contains("INAPT") || scoreBySeason.equals(TeamScoreEnum.INSUFFICIENT_DATA.getValue()) ||
                Double.parseDouble(scoreBySeason.substring(scoreBySeason.indexOf('(')+1, scoreBySeason.lastIndexOf(')'))) < 80 ) {
                continue;
            }

            HashMap<String, Object> simulatedInfoForSeasonMap = strategySeasonStatsService.getSimulatedMatchesByStrategyAndSeason(season, team, strategy);
            simulatedInfoForSeasonMap.put("scoreBySeason", scoreBySeason);
            if (simulatedInfoForSeasonMap != null && !simulatedInfoForSeasonMap.isEmpty()) {
                HashMap<String, Map> teamReportMap = new HashMap<>();
                teamReportMap.put(team.getName(), simulatedInfoForSeasonMap);
                reportMap.get(strategy).putAll(teamReportMap);
            }
        }

        Report report = new Report();
        report.setSeason(season);
        report.setStrategy(strat);
        report.setReportMap(reportMap.get(strategy));
        reportRepository.save(report);

        return ResponseEntity.ok().body(reportMap);
    }

    @ApiOperation(value = "get report for strategy and Season", notes = "Strategy values:\n\"Draw\",\"GoalsFest\",\"WinsMargin\",\"Btts\", \"CleanSheet\", \n" +
            "            \"ConcedeBothHalves\", \"EuroHandicap\", \"NoBtts\", \"NoGoalsFest\", \"NoWins\", \"ScoreBothHalves\", \n" +
            "            \"SecondHalfBigger\", \"WinAndGoals\", \"WinBothHalves\", \"Wins\"")
    @GetMapping
    public ResponseEntity<Object> getReportBySeasonAndStrategy (@Valid @RequestParam("season") String season,
                                                                @Valid @RequestParam("strategy") String strategy) {
        List<Report> reportBySeasonAndStrategy = reportRepository.getReportsBySeasonAndStrategy(season, strategy);
        return ResponseEntity.ok().body(reportBySeasonAndStrategy);
    }

}
