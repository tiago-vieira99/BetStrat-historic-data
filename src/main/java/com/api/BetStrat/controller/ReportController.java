package com.api.BetStrat.controller;

import com.api.BetStrat.dto.SimulatedMatchDto;
import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Report;
import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.ReportRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.service.StrategySeasonStatsService;
import com.api.BetStrat.service.TeamService;
import com.api.BetStrat.tasks.GetLastPlayedMatchTask;
import com.api.BetStrat.util.ScrappingUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
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


    @ApiOperation(value = "simulate Team by Strategy and Season")
    @PostMapping("/simulate-by-strategy/")
    public ResponseEntity<List<SimulatedMatchDto>> simulateStrategyBySeason (@Valid @RequestParam  String strategy, @Valid @RequestParam  String teamName, @Valid @RequestParam String season) {
        Team teamByName = teamRepository.getTeamByNameAndSport(teamName, "Football");
        if (teamByName == null) {
            throw new NotFoundException();
        }

        //simulate score for desired season
        String scoreBySeason = strategySeasonStatsService.calculateScoreBySeason(teamByName, season, strategy.concat("SeasonStats"));

        if (scoreBySeason.contains("INAPT") || scoreBySeason.equals(TeamScoreEnum.INSUFFICIENT_DATA.getValue()) ||
                Double.parseDouble(scoreBySeason.substring(scoreBySeason.indexOf('(')+1, scoreBySeason.lastIndexOf(')'))) < 70 ) {
            return ResponseEntity.ok().body(null);
        }

        List<SimulatedMatchDto> list = strategySeasonStatsService.simulateStrategyBySeason(season, teamByName, strategy.concat("SeasonStats"));

        return ResponseEntity.ok().body(list);
    }

    @ApiOperation(value = "insert reports of all teams for all strategies for Season")
    @PostMapping("/{season}")
    public ResponseEntity<Object> insertReportsBySeason (@PathVariable("season") String season) {
        List<Team> allTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());

        HashMap<String, Map> reportMap = new HashMap<>();

        for (String strategy : FOOTBALL_STRATEGIES_LIST) {
            log.info("handling " + strategy);
            reportMap.put(strategy, new HashMap<String, List>());

            for (Team team : allTeams) {
                //simulate score for desired season
                String scoreBySeason = strategySeasonStatsService.calculateScoreBySeason(team, season, strategy.concat("SeasonStats"));

                if (scoreBySeason.contains("INAPT") || scoreBySeason.equals(TeamScoreEnum.INSUFFICIENT_DATA.getValue()) ||
                        Double.parseDouble(scoreBySeason.substring(scoreBySeason.indexOf('(')+1, scoreBySeason.lastIndexOf(')'))) < 80 ) {
                    continue;
                }

                List<SimulatedMatchDto> simulatedMatchDtoList = strategySeasonStatsService.simulateStrategyBySeason(season, team, strategy.concat("SeasonStats"));
                if (simulatedMatchDtoList != null && !simulatedMatchDtoList.isEmpty()) {
                    HashMap<String, List> teamReportMap = new HashMap<>();
                    teamReportMap.put(team.getName(), simulatedMatchDtoList);
                    reportMap.get(strategy).putAll(teamReportMap);
                }
            }

            Report report = new Report();
            report.setSeason(season);
            report.setStrategy(strategy);
            report.setReportMap(reportMap.get(strategy));
            reportRepository.save(report);
        }

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
