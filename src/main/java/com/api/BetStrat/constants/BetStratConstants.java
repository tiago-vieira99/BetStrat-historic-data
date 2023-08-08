package com.api.BetStrat.constants;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BetStratConstants {

    //Scrapping Service
    public static final String SCRAPPER_SERVICE_URL = "http://localhost:8000/";

    public static final String SOCCERSTATS_BASE_URL = "http://www.soccerstats.com/";
    public static final String FCSTATS_BASE_URL = "http://www.fcstats.com/";
    public static final String ZEROZERO_BASE_URL = "zerozero.pt";
    public static final String FBREF_BASE_URL = "fbref.com";
    public static final String WORLDFOOTBALL_BASE_URL = "worldfootball.net";

    public static final List<String> FOOTBALL_SEASONS_LIST = ImmutableList.of("2016","2016-17","2017","2017-18","2018","2018-19","2019","2019-20",
            "2020","2020-21","2021","2021-22","2022","2022-23");

    public static final List<String> FOOTBALL_SUMMER_SEASONS_LIST = ImmutableList.of("2016","2017","2018","2019","2020","2021","2022");

    public static final List<String> FOOTBALL_WINTER_SEASONS_LIST = ImmutableList.of("2016-17","2017-18","2018-19","2019-20","2020-21","2021-22","2022-23");

    public static final List<String> FOOTBALL_SUMMER_SEASONS_BEGIN_MONTH_LIST = ImmutableList.of("January","February","March","April","May");

    public static final List<String> FOOTBALL_WINTER_SEASONS_BEGIN_MONTH_LIST = ImmutableList.of("July","August","September","October","November");

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
    }};
}
