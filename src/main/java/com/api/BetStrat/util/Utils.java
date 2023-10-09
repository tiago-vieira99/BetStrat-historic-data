package com.api.BetStrat.util;

import com.api.BetStrat.entity.HistoricMatch;
import lombok.SneakyThrows;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Utils {

    @SneakyThrows
    public static double beautifyDoubleValue (double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        NumberFormat nf = NumberFormat.getInstance();
        return nf.parse(df.format(value)).doubleValue();
    }

    public static String findMainCompetition (List<HistoricMatch> historicMatches) {
        return historicMatches.stream()
                // filter non null competition matches
                .filter(m -> Objects.nonNull(m.getCompetition()))
                // summarize competitions
                .collect(Collectors.groupingBy(HistoricMatch::getCompetition, Collectors.counting()))
                // fetch the max entry
                .entrySet().stream().max(Map.Entry.comparingByValue())
                // map to tag
                .map(Map.Entry::getKey).orElse(null);
    }
}
