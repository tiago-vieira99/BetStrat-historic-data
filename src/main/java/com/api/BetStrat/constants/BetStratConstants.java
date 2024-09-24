package com.api.BetStrat.constants;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BetStratConstants {

    //Scrapping Service
    public static final String SCRAPPER_SERVICE_URL = "http://47.237.107.158:8000/";

    public static final String SOCCERSTATS_BASE_URL = "http://www.soccerstats.com/";
    public static final String FCSTATS_BASE_URL = "http://www.fcstats.com/";
    public static final String ZEROZERO_BASE_URL = "zerozero.pt";
    public static final String FBREF_BASE_URL = "fbref.com";
    public static final String WORLDFOOTBALL_BASE_URL = "worldfootball.net";
    public static final String API_SPORTS_BASE_URL = "api-sports";

    public static final String CURRENT_SEASON = "2024-25";

    public static final Integer DEFAULT_BAD_RUN_TO_NEW_SEQ = 5;

    public static final List<String> SEASONS_LIST = ImmutableList.of("2016","2016-17","2017","2017-18","2018","2018-19","2019","2019-20",
            "2020","2020-21","2021","2021-22","2022","2022-23","2023-24");

    public static final List<String> SUMMER_SEASONS_LIST = ImmutableList.of("2016","2017","2018","2019","2020","2021","2022","2023");

    public static final List<String> WINTER_SEASONS_LIST = ImmutableList.of("2016-17","2017-18","2018-19","2019-20","2020-21","2021-22","2022-23","2023-24");

    public static final List<String> SUMMER_SEASONS_BEGIN_MONTH_LIST = ImmutableList.of("January","February","March","April","May");

    public static final List<String> WINTER_SEASONS_BEGIN_MONTH_LIST = ImmutableList.of("July","August","September","October","November");

    public static final List<String> HOCKEY_SEASONS_LIST = ImmutableList.of("2016-2017","2017-2018","2018-2019","2019-2020",
            "2020-2021","2021-2022","2021-2022");

    public static final Map<String, String> ZEROZERO_SEASON_CODES  = new HashMap<String, String>() {{
        put("2016", "2016");
        put("2016-17", "146");
        put("2017", "2017");
        put("2017-18", "147");
        put("2018", "2018");
        put("2018-19", "148");
        put("2019", "2019");
        put("2019-20", "149");
        put("2020", "2020");
        put("2020-21", "150");
        put("2021", "2021");
        put("2021-22", "151");
        put("2022", "2022");
        put("2022-23", "152");
        put("2023", "2023");
        put("2023-24", "153");
    }};

    public static final List<String> LONG_STREAKS_LEAGUES_LIST = ImmutableList.of("https://www.worldfootball.net/schedule/aut-bundesliga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/bel-eerste-klasse-a-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/bul-parva-liga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/cro-1-hnl-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/cyp-first-division-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/cze-1-fotbalova-liga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/den-superliga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/eng-premier-league-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/fra-ligue-1-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/bundesliga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/gre-super-league-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/hun-nb-i-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/isr-ligat-haal-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/ita-serie-a-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/ned-eredivisie-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/pol-ekstraklasa-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/por-primeira-liga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/rou-liga-1-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/rus-premier-liga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/sco-premiership-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/srb-super-liga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/svk-super-liga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/svn-prvaliga-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/esp-primera-division-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/sui-super-league-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/tur-sueperlig-2024-2025-spieltag/",
            "https://www.worldfootball.net/schedule/ukr-premyer-liga-2024-2025-spieltag/");
}
