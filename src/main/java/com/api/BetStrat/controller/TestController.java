package com.api.BetStrat.controller;

import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.util.ScrappingUtil;
import com.api.BetStrat.util.TeamDrawFiboStatsByLeague;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api("for testing...")
@RestController
@RequestMapping("/api/betstrat/test")
public class TestController {

    @ApiOperation(value = "for testing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/league-draws-stats/{league}")
    public String testEndpoint(@PathVariable("league") String league) {
            ScrappingUtil scraping = new ScrappingUtil();
            return scraping.testDrawLeagues(league);
    }

    @GetMapping("/team-draws-stats/{league}&{team}")
    public String testTeamDraw(@PathVariable("league") String league,@PathVariable("team") String team) {
        ScrappingUtil scraping = new ScrappingUtil();
        return scraping.testDrawTeams(league,team);
    }

    @GetMapping("/find-first-draws-by-league")
    public HashMap<String, Map> getDrawFiboStatsByLeague(@Valid @RequestParam String leagueURL) {
        TeamDrawFiboStatsByLeague teamDrawFiboStatsByLeague = new TeamDrawFiboStatsByLeague();
        return teamDrawFiboStatsByLeague.findFirstDrawsByLeague(leagueURL);
    }

    @GetMapping("/find-first-draws-by-league2")
    public HashMap<String, Map> getDrawFiboStatsByLeague2(@Valid @RequestParam String leagueURL) {
        TeamDrawFiboStatsByLeague teamDrawFiboStatsByLeague = new TeamDrawFiboStatsByLeague();
        return teamDrawFiboStatsByLeague.findFirstDrawsByLeague2(leagueURL);
    }

}
