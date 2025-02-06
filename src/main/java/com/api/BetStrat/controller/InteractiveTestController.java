package com.api.BetStrat.controller;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.util.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api("for interactive testing...")
@RestController
@RequestMapping("/api/betstrat/interactive-test")
public class InteractiveTestController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;


    @SneakyThrows
    @ApiOperation(value = "testHomeMarginWinsByTeam", notes = "Real simulation with real matches with interactive decisions on which stakes to apply for HomeMarginWins strategy")
    @GetMapping("/home-marginwins-by-team")
    public ResponseEntity<String> testHomeMarginWinsByTeam(@Valid @RequestParam String teamName, @Valid @RequestParam String season) {

        Team team = teamRepository.getTeamByNameAndSport(teamName, "Football");
        List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
        String mainCompetition = Utils.findMainCompetition(teamMatchesBySeason);
        List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(m -> m.getCompetition().equals(mainCompetition) && m.getHomeTeam().equals(teamName))
            .collect(Collectors.toList());
        filteredMatches.sort(HistoricMatch.matchDateComparator);

        double targetProfit = 2.0;
        double ref_odds = 2.1;
        int i = 0;
        double currentBalance = 0;

        String lineSeparator = "|--------------|-----------------------------------------------|------------|------------|------------|------------|\n";
        String tableString = lineSeparator;
        tableString += (String.format("| %-12s | %-45s | %-10s | %-10s | %-10s | %-10s |\n",
            "date", "match", "seq level", "stake", "ft_result", "profit"));
        tableString += (lineSeparator);

        ArrayList<JSONObject> matchesPlayed = new ArrayList<>();

        // Interactive console
        Scanner scanner = new Scanner(System.in);
        while (i < filteredMatches.size()) {
            HistoricMatch match = filteredMatches.get(i);
            JSONObject jsonMatch = new JSONObject();
            jsonMatch.put("date", match.getMatchDate());
            jsonMatch.put("match", String.format(match.getHomeTeam() + " - " + match.getAwayTeam()));

            // identify NEXT seq_level and stake
            if (matchesPlayed.size() == 0) {
                jsonMatch.put("seq_level", 1);
                jsonMatch.put("stake", Utils.beautifyDoubleValue(targetProfit/(ref_odds-1)));
            } else {
                double accumulatedTargetProfit = targetProfit;
                for (int j = matchesPlayed.size() - 1; j >= 0; j--) {
                    if (matchesPlayed.get(j).getDouble("profit") > 0) {
                        break;
                    } else {
                        accumulatedTargetProfit += matchesPlayed.get(j).getDouble("profit") * -1;
                    }
                }
                jsonMatch.put("stake", Utils.beautifyDoubleValue(accumulatedTargetProfit/(ref_odds-1)));
                if (matchesPlayed.get(matchesPlayed.size() - 1).getDouble("profit") > 0) {
                    jsonMatch.put("seq_level", 1);
                } else {
                    jsonMatch.put("seq_level", matchesPlayed.get(matchesPlayed.size() - 1).getInt("seq_level") + 1);
                }
            }

            // get a decision
            System.out.println("\n\n" + tableString);
            System.out.printf("\nNext Match %d/%d :", i+1, filteredMatches.size());
            System.out.println("\nCurrent Balance: " + Utils.beautifyDoubleValue(currentBalance));
            System.out.println(match.getMatchDate() + " | " + match.getHomeTeam() + " - " + match.getAwayTeam() + " | seq_level: " + jsonMatch.getInt("seq_level") + " | stake: " + jsonMatch.getString("stake"));
            System.out.print("\nWhats your decision (0 - 'exit' ; 1 - 'bet' ; 2 - 'change stake') ?? : ");
            int userOption = scanner.nextInt();
            if (userOption == 0) {
                return ResponseEntity.ok().body(matchesPlayed.toString());
            } else if (userOption == 1) {
                // calculate the profit result for this match
                String res = match.getFtResult().split("\\(")[0];
                int homeResult = Integer.parseInt(res.split(":")[0]);
                int awayResult = Integer.parseInt(res.split(":")[1]);
                if (((match.getHomeTeam().equals(teamName) && homeResult>awayResult))
                    && (Math.abs(homeResult - awayResult) == 1 || Math.abs(homeResult - awayResult) == 2)) {
                    jsonMatch.put("profit", Utils.beautifyDoubleValue(jsonMatch.getDouble("stake") * ref_odds - jsonMatch.getDouble("stake")));
                } else {
                    jsonMatch.put("profit", -jsonMatch.getDouble("stake"));
                }
                jsonMatch.put("ft_result", match.getFtResult());

                currentBalance += jsonMatch.getDouble("profit");
                matchesPlayed.add(jsonMatch);
                tableString += (String.format("| %-12s | %-45s | %-10s | %-10s | %-10s | %-10s |\n",
                    jsonMatch.getString("date"), jsonMatch.getString("match"), jsonMatch.getString("seq_level"), jsonMatch.getString("stake"), jsonMatch.getString("ft_result"), jsonMatch.getString("profit")));
            } else if (userOption == 2) {
                System.out.print("Enter new stake : ");
                double newStake = scanner.nextDouble();
                // calculate the profit result for this match
                String res = match.getFtResult().split("\\(")[0];
                int homeResult = Integer.parseInt(res.split(":")[0]);
                int awayResult = Integer.parseInt(res.split(":")[1]);
                if (((match.getHomeTeam().equals(teamName) && homeResult>awayResult))
                    && (Math.abs(homeResult - awayResult) == 1 || Math.abs(homeResult - awayResult) == 2)) {
                    jsonMatch.put("profit", Utils.beautifyDoubleValue(newStake * ref_odds - newStake));
                } else {
                    jsonMatch.put("profit", -newStake);
                }
                jsonMatch.put("ft_result", match.getFtResult());
                jsonMatch.put("stake", Utils.beautifyDoubleValue(newStake));

                currentBalance += jsonMatch.getDouble("profit");
                matchesPlayed.add(jsonMatch);
                tableString += (String.format("| %-12s | %-45s | %-10s | %-10s | %-10s | %-10s |\n",
                    jsonMatch.getString("date"), jsonMatch.getString("match"), jsonMatch.getString("seq_level"), newStake, jsonMatch.getString("ft_result"), jsonMatch.getString("profit")));
            } else {
                continue;
            }

            // print final table after last game
            if (i+1 == filteredMatches.size()) {
                System.out.println("\nFINAL Balance: " + Utils.beautifyDoubleValue(currentBalance));
                System.out.println(tableString);
            }
            i++;
        }

        return ResponseEntity.ok().body(matchesPlayed.toString());
    }
}
