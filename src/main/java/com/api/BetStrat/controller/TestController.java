package com.api.BetStrat.controller;

import com.api.BetStrat.entity.HistoricMatch;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.StandardError;
import com.api.BetStrat.repository.HistoricMatchRepository;
import com.api.BetStrat.repository.TeamRepository;
import com.api.BetStrat.service.football.WinsMarginStrategySeasonStatsService;
import com.api.BetStrat.util.ScrappingUtil;
import com.api.BetStrat.util.TeamDrawFiboStatsByLeague;
import com.api.BetStrat.util.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.api.BetStrat.constants.BetStratConstants.SEASONS_LIST;

@Api("for testing...")
@RestController
@RequestMapping("/api/betstrat/test")
public class TestController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private HistoricMatchRepository historicMatchRepository;

    public boolean matchFollowStrategyRules(HistoricMatch historicMatch, String teamName, String strategyName) {
        String res = historicMatch.getFtResult().split("\\(")[0];
        int homeResult = Integer.parseInt(res.split(":")[0]);
        int awayResult = Integer.parseInt(res.split(":")[1]);
        if (((historicMatch.getHomeTeam().equals(teamName) && homeResult>awayResult) || (historicMatch.getAwayTeam().equals(teamName) && homeResult<awayResult))
            && (Math.abs(homeResult - awayResult) == 1 || Math.abs(homeResult - awayResult) == 2)) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Integer> testStrategySeasonStats(Team team, String season) {

        List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);
        String mainCompetition = Utils.findMainCompetition(teamMatchesBySeason);
        List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(t -> t.getCompetition().equals(mainCompetition) && t.getHomeTeam().equals(t.getTeamId().getName())).collect(Collectors.toList());
        filteredMatches.sort(HistoricMatch.matchDateComparator);


        ArrayList<Integer> noMarginWinsSequence = new ArrayList<>();
        int count = 0;
        for (HistoricMatch historicMatch : filteredMatches) {
            String res = historicMatch.getFtResult().split("\\(")[0];
            count++;
            int homeResult = Integer.parseInt(res.split(":")[0]);
            int awayResult = Integer.parseInt(res.split(":")[1]);
            if ((historicMatch.getHomeTeam().equals(team.getName()) && homeResult>awayResult)) {
                if (matchFollowStrategyRules(historicMatch, team.getName(), null)) {
                    noMarginWinsSequence.add(count);
                    count = 0;
                }
            }
        }

        noMarginWinsSequence.add(count);
        HistoricMatch lastMatch = filteredMatches.get(filteredMatches.size() - 1);
        if (!matchFollowStrategyRules(lastMatch, team.getName(), null)) {
            noMarginWinsSequence.add(-1);
        }

        return noMarginWinsSequence;
    }

    @ApiOperation(value = "test fractioned Kelly for GF")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
        @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
        @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/test-kelly-for-gf-v2/{season}")
    public String testKellyForGoalsFestv2(@PathVariable("season") String season, @Valid @RequestParam Double initial) {
        String matches = new String();
        ObjectMapper objectMapper = new ObjectMapper();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDate currentDate = null;
        double initialBankroll = 100 + initial;
        double currentBankroll = initialBankroll;

        try {
            // Replace "data.json" with the actual path to your JSON file
            File jsonFile = new File("/Users/vie0002t/Desktop/over25backtest/" + season + ".json");

            // Read JSON from the file and parse it as a JsonNode
            JsonNode data = objectMapper.readTree(jsonFile);

            // Iterate over the JSON array
            for (JsonNode match : data) {
                String date = String.valueOf(match.get("datetime")).replaceAll("\"", "");
                String competition = String.valueOf(match.get("competition")).replaceAll("\"", "");
                //                String match = record.get("match");
                //                String ftResult = record.get("FT result");
                Boolean toBet = Boolean.valueOf(String.valueOf(match.get("filter by comp")));
                Boolean isBetGreen = Boolean.valueOf(String.valueOf(match.get("over 25")).replaceAll("\"","").toLowerCase());//betOutcomeForBttsOneHalf(match);//Boolean.valueOf(record.get("goalsFest"));

                LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
                LocalDate dateObj = dateTime.toLocalDate();
                // Check if we have moved to a new date
                if (currentDate == null || !currentDate.equals(dateObj)) {
//                        if (currentDate != null && dateObj.getMonthValue() != currentDate.getMonthValue()) {
//                            // Reset bankroll at the beginning of each month
//                            initialBankroll = 100;
//                        }
                    currentDate = dateObj;
                    currentBankroll = initialBankroll;
                }

                if (toBet) {
                    double odd = Double.parseDouble(String.valueOf(match.get("over25_odd")).replaceAll("\"","").replaceAll(",","."));//1.97;//getRandomOdd() * 0.95;
                    //                    if (odd < 1.8) {
                    //                        continue;
                    //                    }
                    String betResult = isBetGreen ? "GREEN" : "RED";
                    double stakePercentage = calculateFinalFraction(odd);
                    double stake = Utils.beautifyDoubleValue(stakePercentage * currentBankroll);

                    if (isBetGreen) {
                        initialBankroll += stake * (odd - 1);
                    } else {
                        initialBankroll -= stake;
                    }

                    matches = matches.concat(String.format(date.toString().substring(0,11) + ";" + competition + ";" + match.get("match") + ";Over/Under Goals;" + String.valueOf(odd).replaceAll("\\.", ",") +
                        ";" + String.valueOf(stake).replaceAll("\\.", ",") + ";" + String.valueOf(stakePercentage).replaceAll("\\.", ",") + ";" + betResult + "\n"));
//                        matches = matches.concat(String.format(
//                            date + ";" + String.valueOf(odd).replaceAll("\\.", ",") + ";" + String.valueOf(stake).replaceAll("\\.", ",") + ";" + String.valueOf(stakePercentage)
//                                .replaceAll("\\.", ",") + ";" + betResult + ";" + String.valueOf(currentBankroll).replaceAll("\\.", ",") + "\n"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(currentBankroll);
        return matches;
    }

    private boolean betOutcomeForBttsOneHalf(JsonNode match) {
        if (String.valueOf(match.get("outcome")).replace("\"","").equals("GREEN")) {
            return true;
        }
        return false;
    }

    @ApiOperation(value = "test fractioned Kelly for GF")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
        @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
        @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/test-kelly-for-gf/{season}")
    public String testKellyForGoalsFest(@PathVariable("season") String season) {
        String matches = new String();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");// DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDate currentDate = null;
        double initialBankroll = 100;
        double currentBankroll = initialBankroll;

        try (InputStream inputStream = TestController.class.getClassLoader().getResourceAsStream(season + ".csv");
            Reader reader = new InputStreamReader(inputStream);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withFirstRecordAsHeader())) {

            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: ");
            }

            for (CSVRecord record : csvParser) {
                String date = record.get("date").substring(0, 19);
//                String competition = record.get("competition");
//                String match = record.get("match");
//                String ftResult = record.get("FT result");
                Boolean toBet = true;//Boolean.valueOf(record.get("to bet"));
                Boolean goalsFest = record.get("outcome").equals("GREEN") ? true : false;//Boolean.valueOf(record.get("goalsFest"));

                LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
                LocalDate dateObj = dateTime.toLocalDate();
                // Check if we have moved to a new date
                if (currentDate == null || !currentDate.equals(dateObj)) {
                    currentDate = dateObj;
                    currentBankroll = initialBankroll;
                }

                if (toBet) {
                    double odd = Double.parseDouble(record.get("odd"));//getRandomOdd() * 0.95;
//                    if (odd < 1.8) {
//                        continue;
//                    }
                    String betResult = goalsFest ? "GREEN" : "RED";
                    double stakePercentage = calculateFinalFraction(odd);
                    double stake = Utils.beautifyDoubleValue(stakePercentage * currentBankroll);

                    if (goalsFest) {
                        initialBankroll += stake * (odd - 1);
                    } else {
                        initialBankroll -= stake;
                    }

//                    matches = matches.concat(String.format(date + ";" + competition + ";" + match + ";" + ftResult + ";" + String.valueOf(odd).replaceAll("\\.", ",") +
//                        ";" + String.valueOf(stake).replaceAll("\\.", ",") + ";" + String.valueOf(stakePercentage).replaceAll("\\.", ",") + ";" + betResult + ";" +
//                        String.valueOf(currentBankroll).replaceAll("\\.", ",") + "\n"));
                    matches = matches.concat(String.format(date + ";" + String.valueOf(odd).replaceAll("\\.", ",") +
                        ";" + String.valueOf(stake).replaceAll("\\.", ",") + ";" + String.valueOf(stakePercentage).replaceAll("\\.", ",") + ";" + betResult + ";" +
                        String.valueOf(currentBankroll).replaceAll("\\.", ",") + "\n"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return matches;
    }

    private static double calculateFinalFraction(double odd) {
        double p = 0.53;     // 55%  (estimated probability of winning)
        int minStakePercentage = 3; // 5%
        int maxStakePercentage = 8; // 10%
        double kellyFraction = (p * (odd - 1) - (1 - p)) / (odd - 1);
        double adjustedFraction = 0.5 * kellyFraction;     //fração fixa do valor do Kelly calculado para retirar algum 'otimismo' que o metodo de Kelly tem
        double finalFraction = Math.min(Math.max(adjustedFraction, minStakePercentage / 100.0), maxStakePercentage / 100.0);

        return finalFraction;
    }

    public static double getRandomOdd() {
        // Create a Random object
        Random random = new Random();

        // Generate a random number between 0 and 10000 (inclusive)
        int rand = random.nextInt(10001); // 0 to 10000

        // Map the random number to the corresponding value based on the distribution
        if (rand < 1364) { // 0-1363 (13.64%)
            return 1.75;
        } else if (rand < 2273) { // 1364-2272 (9.09%)
            return 1.90;
        } else if (rand < 3182) { // 2273-3181 (9.09%)
            return 2.00;
        } else if (rand < 4091) { // 3182-4090 (9.09%)
            return 2.20;
        } else if (rand < 5000) { // 4091-4999 (9.09%)
            return 2.40;
        } else if (rand < 5909) { // 5000-5908 (9.09%)
            return 1.70;
        } else if (rand < 6364) { // 5909-6363 (4.55%)
            return 1.55;
        } else if (rand < 7728) { // 6364-7727 (13.64%)
            return 1.85;
        } else if (rand < 8183) { // 7728-8182 (4.55%)
            return 1.80;
        } else if (rand < 9092) { // 8183-9091 (9.09%)
            return 2.10;
        } else if (rand < 9547) { // 9092-9546 (4.55%)
            return 1.72;
        } else { // 9547-10000 (4.55%)
            return 2.25;
        }
    }

    @ApiOperation(value = "test-wins-margin-v2")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
        @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
        @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/test-wins-margin-v2")
    public HashMap<String, String> testWinsMargin20() {
        HashMap<String, String> dataMap = new HashMap<>();

        List<Team> footballTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());

        List<Integer> teamIds = Arrays.asList(189,275,215,262,194,230,278,267,220,257,206,195,236,237,221,199,684,208,265,163,235,211,222,268,201,164,190,255,224,240,239,234,67,274,225,209,198,196,263,192);

        for (Team team : footballTeams) {
            try {

                if (teamIds.contains(Integer.parseInt(String.valueOf(team.getId())))) {
                    ArrayList<Integer> negativeSequence = testStrategySeasonStats(team, "2024-25");
                    dataMap.put(team.getName(), negativeSequence.toString().replaceAll("\\[","").replaceAll("\\]",""));
                }

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        return dataMap;
    }

    @ApiOperation(value = "get historic stats for over/under 2.5 goals")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request", response = StandardError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = StandardError.class),
            @ApiResponse(code = 403, message = "Forbidden", response = StandardError.class),
            @ApiResponse(code = 404, message = "Not Found", response = StandardError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = StandardError.class),
    })
    @GetMapping("/overunders-stats")
    public HashMap<String, HashMap> overUnder25stats() {
        HashMap<String, HashMap> dataMap = new HashMap<>();

        List<Team> footballTeams = teamRepository.findAll().stream().filter(t -> t.getSport().equals("Football")).collect(Collectors.toList());


        for (Team team : footballTeams) {
            try {
                HashMap<String, String> seasonStatsMap = new HashMap<>();
                for (String season : SEASONS_LIST) {
                    List<HistoricMatch> teamMatchesBySeason = historicMatchRepository.getTeamMatchesBySeason(team, season);

                    if (!teamMatchesBySeason.isEmpty()) {
                        String mainCompetition = Utils.findMainCompetition(teamMatchesBySeason);
                        List<HistoricMatch> filteredMatches = teamMatchesBySeason.stream().filter(t -> t.getCompetition().equals(mainCompetition)).collect(Collectors.toList());
//                        long numOvers = filteredMatches.stream().filter(m -> Integer.parseInt(m.getFtResult().split(":")[0]) > 0 && Integer.parseInt(m.getFtResult().split(":")[1]) > 0).count();
//                        double oversRate = 100*numOvers/filteredMatches.size();
                        long count = teamMatchesBySeason.stream().filter(m ->
                                (Integer.parseInt(m.getFtResult().split(":")[0]) + Integer.parseInt(m.getFtResult().split(":")[1]) -
                                        Integer.parseInt(m.getHtResult().split(":")[0]) - Integer.parseInt(m.getHtResult().split(":")[1])) <=
                                        (Integer.parseInt(m.getHtResult().split(":")[0]) + Integer.parseInt(m.getHtResult().split(":")[1]))).count();
                        double oversRate = 100*count/teamMatchesBySeason.size();
                        seasonStatsMap.put(season, String.valueOf(oversRate));
                    } else {
                        System.out.println("no matches for " + season + " for " + team.getName());
                    }
                }
                dataMap.put(team.getName(), seasonStatsMap);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        return dataMap;
    }

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

    @SneakyThrows
    @ApiOperation(value = "testMarginWinsBankrollManagement", notes = "Real simulation with real sequences and odd avg for testing StacksBankrollManagement system for MarginWins strategy")
    @GetMapping("/test-marginwins-bankroll-management")
    public double testNewMarginWinsBankrollManagement(@Valid @RequestParam String teamSequence, @Valid @RequestParam double oddAvg, @Valid @RequestParam double targetProfit) {
        List<String> sequences = Arrays.asList(teamSequence.split(","));

        double totalBalance= 0.0;

        Stack<Double> stakesPlayed = new Stack<>();
        Stack<Double> stakesToRecover = new Stack<>();
        Stack<Double> sums = new Stack<>();
        double laststake = 0.0;
        boolean recoveryMode = false;
        double stakesDivisor = 9 * targetProfit;

        for (int k = 0; k< sequences.size()-1;k++) {
            int numNoWins = Integer.parseInt(sequences.get(k).trim());
            double sumStakesPlayed = 0.0;

            for (int i = 0; i<numNoWins; i++) {
                if (sumStakesPlayed >= stakesDivisor) {
                    totalBalance = Utils.beautifyDoubleValue(totalBalance - sumStakesPlayed);
                    sums.push(-sumStakesPlayed);
                    stakesToRecover.push(Utils.beautifyDoubleValue(sumStakesPlayed/3));
                    stakesToRecover.push(Utils.beautifyDoubleValue(sumStakesPlayed/3));
                    stakesToRecover.push(Utils.beautifyDoubleValue(sumStakesPlayed/3));
                    sumStakesPlayed = 0;
                    recoveryMode = true;
                }
                double stakeToPlay = 0;
                if (!recoveryMode) {
                    stakeToPlay = Utils.beautifyDoubleValue((targetProfit + sumStakesPlayed)/(oddAvg-1));
                } else if (recoveryMode && sumStakesPlayed == 0) {
                    stakeToPlay = Utils.beautifyDoubleValue((stakesToRecover.peek())/(oddAvg-1));
                } else if (recoveryMode && sumStakesPlayed > 0) {
                    stakeToPlay = Utils.beautifyDoubleValue((stakesToRecover.peek() + sumStakesPlayed)/(oddAvg-1));
                }
                stakesPlayed.push(stakeToPlay);
                sumStakesPlayed = Utils.beautifyDoubleValue(sumStakesPlayed + stakeToPlay);
            }

            laststake = stakesPlayed.peek();
            if (Integer.parseInt(sequences.get(k+1).trim()) == -1) {
                totalBalance = totalBalance - laststake;
                break;
            } else if (Integer.parseInt(sequences.get(k+1).trim()) == 0) {
                totalBalance += laststake;
                break;
            }

            //add to balance last balance
            if (recoveryMode) {
                totalBalance = Utils.beautifyDoubleValue(totalBalance + stakesToRecover.peek());
                sums.push(stakesToRecover.peek());
            } else {
                totalBalance = Utils.beautifyDoubleValue(totalBalance + targetProfit);
                sums.push(targetProfit);
            }

            if (stakesToRecover.size() > 0) {
                stakesToRecover.pop();
            }

            if (stakesToRecover.size() == 0) {
                recoveryMode = false;
            }
        }

        String stakesString = stakesPlayed.toString();

        return totalBalance;
    }


    @SneakyThrows
    @ApiOperation(value = "testOver25BankrollManagement", notes = "Real simulation with real data of StacksBankrollManagement system for over 2.5 strategy")
    @GetMapping("/test-over25-bankroll-management")
    public List<LinkedHashMap> testOver25BankrollManagement() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Create the Map to hold the grouped data
        Map<String, List<JsonNode>> groupedData = new LinkedHashMap<>();

        try {
            // Replace "data.json" with the actual path to your JSON file
            File jsonFile = new File("/Users/vie0002t/Desktop/belgium23-24.json");

            // Create a SimpleDateFormat to parse the date string
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");

            // Read JSON from the file and parse it as a JsonNode
            JsonNode data = objectMapper.readTree(jsonFile);

            // Iterate over the JSON array
            for (JsonNode jsonObject : data) {
                // Get the date string from the JSON object
                String dateString = jsonObject.get("01. date").asText();

                // Parse the date string into a Date object
                Date date = dateFormat.parse(dateString);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String dayDate = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);

                // Check if the date already exists in the map
                if (groupedData.containsKey(dayDate)) {
                    // If it exists, add the JSON object to the list
                    groupedData.get(dayDate).add(jsonObject);
                } else {
                    // If it doesn't exist, create a new list and add the JSON object to it
                    List<JsonNode> dateList = new ArrayList<>();
                    dateList.add(jsonObject);
                    groupedData.put(dayDate, dateList);
                }
            }

            // Now you have the grouped data in the Map, where the key is the date and the value is a list of JSON objects
//            for (Map.Entry<Date, List<JsonNode>> entry : groupedData.entrySet()) {
//                Date date = entry.getKey();
//                List<JsonNode> dateObjects = entry.getValue();
//
//                System.out.println("Date: " + date);
//                for (JsonNode object : dateObjects) {
//                    System.out.println(object.toString());
//                }
//                //System.out.println("----------------------");
//            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

//        Stack<LinkedHashMap<Double, JsonNode>> stack = new Stack<>();
        List<Stack<LinkedHashMap<Double, JsonNode>>> stakesToRecover = new ArrayList<>();
        double targetProfit = 2;

        double accumulatedBalance = 0.0;
        List<LinkedHashMap> allSimulatedData = new ArrayList<>();
        List<Double> balances = new ArrayList<>();

        int prevMonth = 0;
        for (Map.Entry<String, List<JsonNode>> entry : groupedData.entrySet()) {
            String date = entry.getKey();
            List<JsonNode> dateObjects = entry.getValue();
            //reset the stakesToRecover at the beggining of each month
//            if (date.getMonth() > prevMonth) {
//                stakesToRecover.clear();
//            }
//            prevMonth = date.getMonth();
//
//            //start over when get 50€ and save the money
//            if (accumulatedBalance >= 50) {
//                balances.add(accumulatedBalance);
//                accumulatedBalance = 0;
//                stakesToRecover.clear();
//            }

            /***first place the bet giving the odd and returning the stake***/
            for (JsonNode object : dateObjects) {
                String currentDate = String.valueOf(object.get("01. date"));
                double overOdd = Double.parseDouble(String.valueOf(object.get("20, overOdd")).replaceAll(",",".").replaceAll("\"",""));
                overOdd = overOdd * 1.15;
                double stakeToBet = 0;

                // Filtering the 'stakesToRecover' list and adding the highest key element to the stack
                List<Integer> filteredStacksIdx = new ArrayList<>();
                for (int i=0; i<stakesToRecover.size(); i++) {
                    if (!existStackForCurrentDate(stakesToRecover.get(i), currentDate.substring(0,11))) {
                        filteredStacksIdx.add(i);
                    }
                }
                if (filteredStacksIdx.size() > 0) {
                    //get the stack with higher capacity and stakeToBet is the poped element
                    Stack<LinkedHashMap<Double, JsonNode>> stackWithHighestKey = findStackWithHighestKey(stakesToRecover, currentDate.substring(0,11));

                    // Adding a new element to the stack with the highest key
                    if (stackWithHighestKey != null) {
                        double newKey = Utils.beautifyDoubleValue((calculateSumOfKeys(stackWithHighestKey) + targetProfit)/(overOdd-1));
                        stackWithHighestKey.push(new LinkedHashMap<>(Collections.singletonMap(newKey, object)));
                    }
                } else {
                    stakeToBet = Utils.beautifyDoubleValue(targetProfit/(overOdd-1));
                    Stack<LinkedHashMap<Double, JsonNode>> newStack = new Stack<>();
                    LinkedHashMap<Double, JsonNode> betMap = new LinkedHashMap<>();
                    betMap.put(stakeToBet, object);
                    newStack.push(betMap);
                    stakesToRecover.add(newStack);
                }
            }

            String daySummary = "\n\nDate: " + date.toString() + "\n## bets placed:";
            for (int i = 0; i<stakesToRecover.size(); i++) {
                String stackLine = "\n" + String.valueOf(i+1) + "\t:\t\t";
                for (int k=0; k<stakesToRecover.get(i).size(); k++) {
                    stackLine = stackLine + stakesToRecover.get(i).get(k).keySet() + "\t|\t";
                }
                daySummary = daySummary + stackLine;
            }
            System.out.println(daySummary);

            /***then, set the bet result adding/removing stake in data structure***/
            for (JsonNode object : dateObjects) {
                String result = String.valueOf(object.get("result")).replaceAll("\"","");
                double overOdd = Double.parseDouble(String.valueOf(object.get("20, overOdd")).replaceAll(",",".").replaceAll("\"",""));
                overOdd = overOdd * 1.15;

                Stack<LinkedHashMap<Double, JsonNode>> stackByJsonNode = findStackByJsonNode(stakesToRecover, object);
                double firstStake = (double) stackByJsonNode.peek().keySet().toArray()[0];
                if (result.contains("GREEN")) {
                    accumulatedBalance += Utils.beautifyDoubleValue((firstStake*overOdd) - firstStake);
                    stakesToRecover.remove(stackByJsonNode);
                } else {
                    //penso que daqui para baixo se deva usar calculateSumOfKeys(stackByJsonNode) em vez de firstStake
                    if (stackByJsonNode.size() > 4 || firstStake >= 20) {
                        int numSplittedStakes = 3;
//                        if (firstStake >= 10) {
//                            numSplittedStakes = 3;
//                        }
//                        int numSplittedStakes = 5;

                        double splittedStake = Utils.beautifyDoubleValue(firstStake/numSplittedStakes);
                        //split it into X new stacks
                        for (int i=0;i<numSplittedStakes;i++) {
                            Stack<LinkedHashMap<Double, JsonNode>> newStack = new Stack<>();
                            LinkedHashMap<Double, JsonNode> betMap = new LinkedHashMap<>();
                            betMap.put(splittedStake, object);
                            newStack.push(betMap);
                            stakesToRecover.add(newStack);
                        }
                        stakesToRecover.remove(stackByJsonNode);
                    }
                    accumulatedBalance -= firstStake;
                }
                LinkedHashMap<String, Object> betInfoMap = new LinkedHashMap<>();
                betInfoMap.put("date", object.get("01. date").toString().replaceAll("\"","").substring(0,11));
                betInfoMap.put("odd", String.valueOf(overOdd).replaceAll("\\.", ","));
                betInfoMap.put("stake", String.valueOf(firstStake).replaceAll("\\.",","));
                betInfoMap.put("accuBalance", String.valueOf(accumulatedBalance).replaceAll("\\.",","));
                betInfoMap.put("result", result);
                allSimulatedData.add(betInfoMap);
            }

            /*** end of the day, print the summary : ***/
            daySummary = "\n\n## day summary result";
            for (int i = 0; i<stakesToRecover.size(); i++) {
                String stackLine = "\n" + String.valueOf(i+1) + "\t:\t\t";
                for (int k=0; k<stakesToRecover.get(i).size(); k++) {
                    stackLine = stackLine + stakesToRecover.get(i).get(k).keySet() + "\t|\t";
                }
                daySummary = daySummary + stackLine;
            }
            System.out.println(daySummary);
        }

        List<LinkedHashMap<String, Object>> sumStakesByDate = sumStakesByDate(allSimulatedData);
        for (LinkedHashMap<String, Object> m : sumStakesByDate) {
            Double stake = (Double) m.get("sumStake");
            m.put("sumStake", String.valueOf(stake).replaceAll("\\.", ","));
        }
        String s = JSONArray.toJSONString(sumStakesByDate);

        System.out.println(s);

        return allSimulatedData;
    }

    @SneakyThrows
    @ApiOperation(value = "testHomeSuperFavBankrollManagement", notes = "Real simulation with real data of StacksBankrollManagement system for home super favourites strategy")
    @GetMapping("/test-homefav-bankroll-management")
    public List<LinkedHashMap> testHomeSuperFavBankrollManagement() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Create the Map to hold the grouped data
        Map<Date, List<JsonNode>> groupedData = new LinkedHashMap<>();

        try {
            // Replace "data.json" with the actual path to your JSON file
            File jsonFile = new File("C:\\Users\\tiago.vieira\\Desktop\\homefav.json");

            // Create a SimpleDateFormat to parse the date string
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // Read JSON from the file and parse it as a JsonNode
            JsonNode data = objectMapper.readTree(jsonFile);

            // Iterate over the JSON array
            for (JsonNode jsonObject : data) {
                // Get the date string from the JSON object
                String dateString = jsonObject.get("01. date").asText();

                // Parse the date string into a Date object
                Date date = dateFormat.parse(dateString);

                // Check if the date already exists in the map
                if (groupedData.containsKey(date)) {
                    // If it exists, add the JSON object to the list
                    groupedData.get(date).add(jsonObject);
                } else {
                    // If it doesn't exist, create a new list and add the JSON object to it
                    List<JsonNode> dateList = new ArrayList<>();
                    dateList.add(jsonObject);
                    groupedData.put(date, dateList);
                }
            }

            // Now you have the grouped data in the Map, where the key is the date and the value is a list of JSON objects
//            for (Map.Entry<Date, List<JsonNode>> entry : groupedData.entrySet()) {
//                Date date = entry.getKey();
//                List<JsonNode> dateObjects = entry.getValue();
//
//                System.out.println("Date: " + date);
//                for (JsonNode object : dateObjects) {
//                    System.out.println(object.toString());
//                }
//                //System.out.println("----------------------");
//            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

//        Stack<LinkedHashMap<Double, JsonNode>> stack = new Stack<>();
        List<Stack<LinkedHashMap<Double, JsonNode>>> stakesToRecover = new ArrayList<>();
        int targetProfit = 1;

        double accumulatedBalance = 0.0;
        List<LinkedHashMap> allSimulatedData = new ArrayList<>();

        for (Map.Entry<Date, List<JsonNode>> entry : groupedData.entrySet()) {
            Date date = entry.getKey();
            List<JsonNode> dateObjects = entry.getValue();

//            System.out.println("Date: " + date);
            /***first place the bet giving the odd and returning the stake***/
            for (JsonNode object : dateObjects) {
                String currentDate = String.valueOf(object.get("01. date"));
                double overOdd = Double.parseDouble(String.valueOf(object.get("24, v1Odd")).replaceAll(",",".").replaceAll("\"",""));
                double stakeToBet = 0;

                // Filtering the 'stakesToRecover' list and adding the highest key element to the stack
                List<Integer> filteredStacksIdx = new ArrayList<>();
                for (int i=0; i<stakesToRecover.size(); i++) {
                    if (!existStackForCurrentDate(stakesToRecover.get(i), currentDate.substring(0,11))) {
                        filteredStacksIdx.add(i);
                    }
                }
                if (filteredStacksIdx.size() > 0) {
                    //get the stack with higher capacity and stakeToBet is the poped element
                    Stack<LinkedHashMap<Double, JsonNode>> stackWithHighestKey = findStackWithHighestKey(stakesToRecover, currentDate.substring(0,11));

                    // Adding a new element to the stack with the highest key
                    if (stackWithHighestKey != null) {
                        double newKey = (calculateSumOfKeys(stackWithHighestKey) + targetProfit)/(overOdd-1);
                        stackWithHighestKey.push(new LinkedHashMap<>(Collections.singletonMap(newKey, object)));
                    }
                } else {
                    stakeToBet = targetProfit/(overOdd-1);
                    Stack<LinkedHashMap<Double, JsonNode>> newStack = new Stack<>();
                    LinkedHashMap<Double, JsonNode> betMap = new LinkedHashMap<>();
                    betMap.put(stakeToBet, object);
                    newStack.push(betMap);
                    stakesToRecover.add(newStack);
                }
//                System.out.println();
            }

            String daySummary = "\n\nDate: " + date.toString() + "\n## bets placed:";
            for (int i = 0; i<stakesToRecover.size(); i++) {
                String stackLine = "\n" + String.valueOf(i+1) + "\t:\t\t";
                for (int k=0; k<stakesToRecover.get(i).size(); k++) {
                    stackLine = stackLine + stakesToRecover.get(i).get(k).keySet() + "\t|\t";
                }
                daySummary = daySummary + stackLine;
            }
            System.out.println(daySummary);

            /***then, set the bet result adding/removing stake in data structure***/
            for (JsonNode object : dateObjects) {
                String result = String.valueOf(object.get("result")).replaceAll("\"","");
                double overOdd = Double.parseDouble(String.valueOf(object.get("24, v1Odd")).replaceAll(",",".").replaceAll("\"",""));

                Stack<LinkedHashMap<Double, JsonNode>> stackByJsonNode = findStackByJsonNode(stakesToRecover, object);
                double firstStake = (double) stackByJsonNode.peek().keySet().toArray()[0];
                if (result.contains("true")) {
                    accumulatedBalance += (firstStake*overOdd) - firstStake;
                    stakesToRecover.remove(stackByJsonNode);
                } else {
                    if (stackByJsonNode.size() > 1 || firstStake >= 10) {
                        int numSplittedStakes = 2;
                        if (firstStake >= 10) {
                            numSplittedStakes = 3;
                        }
                        double splittedStake = firstStake/numSplittedStakes;
                        //split it into X new stacks
                        for (int i=0;i<numSplittedStakes;i++) {
                            Stack<LinkedHashMap<Double, JsonNode>> newStack = new Stack<>();
                            LinkedHashMap<Double, JsonNode> betMap = new LinkedHashMap<>();
                            betMap.put(splittedStake, object);
                            newStack.push(betMap);
                            stakesToRecover.add(newStack);
                        }
                        stakesToRecover.remove(stackByJsonNode);
                    }
                    accumulatedBalance -= firstStake;
                }
                LinkedHashMap<String, Object> betInfoMap = new LinkedHashMap<>();
                betInfoMap.put("date", object.get("01. date").toString().replaceAll("\"","").substring(0,11));
                betInfoMap.put("odd", object.get("24, v1Odd").toString().replaceAll("\"",""));
                betInfoMap.put("stake", String.valueOf(firstStake).replaceAll("\\.",","));
                betInfoMap.put("result", result);
                betInfoMap.put("accuBalance", String.valueOf(accumulatedBalance).replaceAll("\\.",","));
                allSimulatedData.add(betInfoMap);
            }

            /*** end of the day, print the summary : ***/
            daySummary = "\n\n## day summary result";
            for (int i = 0; i<stakesToRecover.size(); i++) {
                String stackLine = "\n" + String.valueOf(i+1) + "\t:\t\t";
                for (int k=0; k<stakesToRecover.get(i).size(); k++) {
                    stackLine = stackLine + stakesToRecover.get(i).get(k).keySet() + "\t|\t";
                }
                daySummary = daySummary + stackLine;
            }

            System.out.println(daySummary);
        }

        List<LinkedHashMap<String, Object>> sumStakesByDate = sumStakesByDate(allSimulatedData);
        for (LinkedHashMap<String, Object> m : sumStakesByDate) {
            Double stake = (Double) m.get("sumStake");
            m.put("sumStake", String.valueOf(stake).replaceAll("\\.", ","));
        }
        String s = JSONArray.toJSONString(sumStakesByDate);

        System.out.println(s);

        return allSimulatedData;
    }

    @SneakyThrows
    @ApiOperation(value = "testMarginWinBankrollManagement", notes = "Real simulation with real data of StacksBankrollManagement system for margin wins strategy")
    @GetMapping("/test-marginwin-bankroll-management")
    public List<LinkedHashMap> testMarginWinBankrollManagement() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Create the Map to hold the grouped data
        Map<String, List<JsonNode>> groupedData = new LinkedHashMap<>();

        try {
            // Replace "data.json" with the actual path to your JSON file
            File jsonFile = new File("C:\\Users\\tiago.vieira\\Desktop\\marginwins.json");

            // Create a SimpleDateFormat to parse the date string
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // Read JSON from the file and parse it as a JsonNode
            JsonNode data = objectMapper.readTree(jsonFile);

            // Iterate over the JSON array
            for (JsonNode jsonObject : data) {
                // Get the date string from the JSON object
                String dateString = jsonObject.get("date").asText();

                // Parse the date string into a Date object
                Date date = dateFormat.parse(dateString);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String dayDate = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);

                // Check if the date already exists in the map
                if (groupedData.containsKey(dayDate)) {
                    // If it exists, add the JSON object to the list
                    groupedData.get(dayDate).add(jsonObject);
                } else {
                    // If it doesn't exist, create a new list and add the JSON object to it
                    List<JsonNode> dateList = new ArrayList<>();
                    dateList.add(jsonObject);
                    groupedData.put(dayDate, dateList);
                }
            }

            // Now you have the grouped data in the Map, where the key is the date and the value is a list of JSON objects
//            for (Map.Entry<Date, List<JsonNode>> entry : groupedData.entrySet()) {
//                Date date = entry.getKey();
//                List<JsonNode> dateObjects = entry.getValue();
//
//                System.out.println("Date: " + date);
//                for (JsonNode object : dateObjects) {
//                    System.out.println(object.toString());
//                }
//                //System.out.println("----------------------");
//            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

//        Stack<LinkedHashMap<Double, JsonNode>> stack = new Stack<>();
        List<Stack<LinkedHashMap<Double, JsonNode>>> stakesToRecover = new ArrayList<>();
        int targetProfit = 1;

        double accumulatedBalance = 0.0;
        List<LinkedHashMap> allSimulatedData = new ArrayList<>();

        for (Map.Entry<String, List<JsonNode>> entry : groupedData.entrySet()) {
            String date = entry.getKey();
            List<JsonNode> dateObjects = entry.getValue();

//            System.out.println("Date: " + date);
            /***first place the bet giving the odd and returning the stake***/
            for (JsonNode object : dateObjects) {
                String currentDate = String.valueOf(object.get("date"));
                double overOdd = Double.parseDouble(String.valueOf(object.get("odd")).replaceAll(",",".").replaceAll("\"",""));
                double stakeToBet = 0;

                // Filtering the 'stakesToRecover' list and adding the highest key element to the stack
                List<Integer> filteredStacksIdx = new ArrayList<>();
                for (int i=0; i<stakesToRecover.size(); i++) {
                    if (!existStackForCurrentDate(stakesToRecover.get(i), currentDate.substring(0,11))) {
                        filteredStacksIdx.add(i);
                    }
                }
                if (filteredStacksIdx.size() > 0) {
                    //get the stack with higher capacity and stakeToBet is the poped element
                    Stack<LinkedHashMap<Double, JsonNode>> stackWithHighestKey = findStackWithHighestKey(stakesToRecover, currentDate.substring(0,11));

                    // Adding a new element to the stack with the highest key
                    if (stackWithHighestKey != null) {
                        double newKey = Utils.beautifyDoubleValue((calculateSumOfKeys(stackWithHighestKey) + targetProfit)/(overOdd-1));
                        stackWithHighestKey.push(new LinkedHashMap<>(Collections.singletonMap(newKey, object)));
                    }
                } else {
                    stakeToBet = Utils.beautifyDoubleValue(targetProfit/(overOdd-1));
                    Stack<LinkedHashMap<Double, JsonNode>> newStack = new Stack<>();
                    LinkedHashMap<Double, JsonNode> betMap = new LinkedHashMap<>();
                    betMap.put(stakeToBet, object);
                    newStack.push(betMap);
                    stakesToRecover.add(newStack);
                }
//                System.out.println();
            }

            String daySummary = "\n\nDate: " + date.toString() + "\n## bets placed:";
            for (int i = 0; i<stakesToRecover.size(); i++) {
                String stackLine = "\n" + String.valueOf(i+1) + "\t:\t\t";
                for (int k=0; k<stakesToRecover.get(i).size(); k++) {
                    stackLine = stackLine + stakesToRecover.get(i).get(k).keySet() + "\t|\t";
                }
                daySummary = daySummary + stackLine;
            }
            System.out.println(daySummary);

            /***then, set the bet result adding/removing stake in data structure***/
            for (JsonNode object : dateObjects) {
                double result = Double.parseDouble(String.valueOf(object.get("balance")).replaceAll(",",".").replaceAll("\"",""));
                double overOdd = Double.parseDouble(String.valueOf(object.get("odd")).replaceAll(",",".").replaceAll("\"",""));

                Stack<LinkedHashMap<Double, JsonNode>> stackByJsonNode = findStackByJsonNode(stakesToRecover, object);
                double firstStake = (double) stackByJsonNode.peek().keySet().toArray()[0];
                if (result > 0) {
                    accumulatedBalance += Utils.beautifyDoubleValue((firstStake*overOdd) - firstStake);
                    stakesToRecover.remove(stackByJsonNode);
                } else {
                    if (stackByJsonNode.size() > 2 || firstStake >= 15) {
                        int numSplittedStakes = 2;
                        if (firstStake >= 15) {
                            numSplittedStakes = 3;
                        }
                        double splittedStake = Utils.beautifyDoubleValue(firstStake/numSplittedStakes);
                        //split it into X new stacks
                        for (int i=0;i<numSplittedStakes;i++) {
                            Stack<LinkedHashMap<Double, JsonNode>> newStack = new Stack<>();
                            LinkedHashMap<Double, JsonNode> betMap = new LinkedHashMap<>();
                            betMap.put(splittedStake, object);
                            newStack.push(betMap);
                            stakesToRecover.add(newStack);
                        }
                        stakesToRecover.remove(stackByJsonNode);
                    }
                    accumulatedBalance -= firstStake;
                }
                LinkedHashMap<String, Object> betInfoMap = new LinkedHashMap<>();
                betInfoMap.put("date", object.get("date").toString().replaceAll("\"",""));
                betInfoMap.put("odd", object.get("odd").toString().replaceAll("\"",""));
                betInfoMap.put("stake", String.valueOf(firstStake).replaceAll("\\.",","));
                betInfoMap.put("result", result);
                betInfoMap.put("accuBalance", String.valueOf(accumulatedBalance).replaceAll("\\.",","));
                allSimulatedData.add(betInfoMap);
            }

            /*** end of the day, print the summary : ***/
            daySummary = "\n\n## day summary result";
            for (int i = 0; i<stakesToRecover.size(); i++) {
                String stackLine = "\n" + String.valueOf(i+1) + "\t:\t\t";
                for (int k=0; k<stakesToRecover.get(i).size(); k++) {
                    stackLine = stackLine + stakesToRecover.get(i).get(k).keySet() + "\t|\t";
                }
                daySummary = daySummary + stackLine;
            }

            System.out.println(daySummary);
        }

        List<LinkedHashMap<String, Object>> sumStakesByDate = sumStakesByDate(allSimulatedData);
        for (LinkedHashMap<String, Object> m : sumStakesByDate) {
            Double stake = (Double) m.get("sumStake");
            m.put("sumStake", String.valueOf(stake).replaceAll("\\.", ","));
        }
        String s = JSONArray.toJSONString(sumStakesByDate);

        System.out.println(s);

        return allSimulatedData;
    }

    public static boolean existStackForCurrentDate(Stack<LinkedHashMap<Double, JsonNode>> stack, String currentDate) {

        for (LinkedHashMap<Double, JsonNode> element : stack) {
            JsonNode jsonNode = (JsonNode) element.values().toArray()[0];
            if (jsonNode.get("01. date").toString().replaceAll("\"","").contains(currentDate.replaceAll("\"",""))) {
                return true;
            }
        }
        return false;
    }

    public static Stack<LinkedHashMap<Double, JsonNode>> findStackWithHighestKey(List<Stack<LinkedHashMap<Double, JsonNode>>> stacks, String currentDate) {
        // Finding the stack with the highest key (if any)
        double highestKey = Double.MIN_VALUE;
        Stack<LinkedHashMap<Double, JsonNode>> stackWithHighestKey = null;
        for (Stack<LinkedHashMap<Double, JsonNode>> currentStack : stacks) {
            if (existStackForCurrentDate(currentStack, currentDate.substring(0,11))) {
                continue;
            }
            double currentKey = findHighestKey(currentStack);
            if (currentKey > highestKey) {
                highestKey = currentKey;
                stackWithHighestKey = currentStack;
            }
        }
        return stackWithHighestKey;
    }

    public static double findHighestKey(Stack<LinkedHashMap<Double, JsonNode>> stack) {
        // Finding the highest key in the given stack
        double highestKey = Double.MIN_VALUE;
        for (LinkedHashMap<Double, JsonNode> element : stack) {
            double key = element.keySet().iterator().next();
            if (key > highestKey) {
                highestKey = key;
            }
        }
        return highestKey;
    }

    public static double calculateSumOfKeys(Stack<LinkedHashMap<Double, JsonNode>> stack) {
        double sum = 0.0;
        for (LinkedHashMap<Double, JsonNode> element : stack) {
            for (Double key : element.keySet()) {
                sum += key;
            }
        }
        return sum;
    }

    public static Stack<LinkedHashMap<Double, JsonNode>> findStackByJsonNode(List<Stack<LinkedHashMap<Double, JsonNode>>> stacks, JsonNode targetJsonNode) {
        for (Stack<LinkedHashMap<Double, JsonNode>> currentStack : stacks) {
            for (LinkedHashMap<Double, JsonNode> element : currentStack) {
                for (JsonNode jsonNode : element.values()) {
                    if (jsonNode.equals(targetJsonNode)) {
                        return currentStack; // Return the original stack without removing it.
                    }
                }
            }
        }
        return null; // Return null if the targetJsonNode is not found in any of the stacks.
    }

    public static List<LinkedHashMap<String, Object>> sumStakesByDate(List<LinkedHashMap> originalList) {
        // Create a map to store the sum of stakes for each date
        LinkedHashMap<String, Double> dateStakeSumMap = new LinkedHashMap<>();

        // Iterate through the original list and calculate the sum of stakes for each date
        for (Map<String, Object> entry : originalList) {
            String date = (String) entry.get("date");
            double stake = Double.parseDouble(String.valueOf(entry.get("stake")).replaceAll(",","."));
            dateStakeSumMap.put(date, dateStakeSumMap.getOrDefault(date, 0.0) + stake);
        }

        // Create a new list of maps with the sum of stakes grouped by date
        List<LinkedHashMap<String, Object>> newList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : dateStakeSumMap.entrySet()) {
            LinkedHashMap<String, Object> newEntry = new LinkedHashMap<>();
            newEntry.put("date", entry.getKey());
            newEntry.put("sumStake", entry.getValue());
            newList.add(newEntry);
        }

        return newList;
    }

}
