package com.api.BetStrat.constants;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class BetStratConstants {

    //Scrapping Service
    public static final String SOCCERSTATS_BASE_URL = "http://www.soccerstats.com/";
    public static final String FCSTATS_BASE_URL = "http://www.fcstats.com/";
    public static final String ZEROZERO_BASE_URL = "http://www.zerozero.pt/";

    public static final List<String> SEASONS_LIST = ImmutableList.of("2016","2016-17","2017","2017-18","2018","2018-19","2019","2019-20",
            "2020","2020-21","2021","2021-22","2022");

    public static final List<String> HOCKEY_SEASONS_LIST = ImmutableList.of("2016","2016-2017","2017","2017-2018","2018","2018-2019","2019","2019-2020",
            "2020","2020-2021","2021","2021-2022","2022");

}
