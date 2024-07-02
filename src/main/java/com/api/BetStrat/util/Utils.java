package com.api.BetStrat.util;

import com.api.BetStrat.entity.HistoricMatch;
import lombok.SneakyThrows;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Utils {

    private double mean = this.mean;

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

    public static double calculateSD(ArrayList<Integer> sequence) {
        List<Integer> sequence2 = new ArrayList<>(sequence.subList(0, sequence.size()-1));
        if (sequence.get(sequence.size()-1) == 0) {
            sequence2.add(1);
        }

        //mean
        double mean = sequence2.stream().mapToInt(Integer::intValue).average().getAsDouble();

        //squared deviation
        List<Double> ssList = new ArrayList<>();

        for (int i : sequence2) {
            ssList.add(Math.pow(Math.abs(mean-i),2));
        }

        //SS value
        double ssValue = ssList.stream().collect(Collectors.summingDouble(i -> i));

        //s^2
        double s2 = ssValue / (sequence2.size() - 1);

        //standard deviation
        double stdDev = Math.sqrt(s2);

        return stdDev;
    }

    public static double calculateCoeffVariation(double stdDev, ArrayList<Integer> sequence) {
        List<Integer> sequence2 = new ArrayList<>(sequence.subList(0, sequence.size()-1));
        if (sequence.get(sequence.size()-1) == 0) {
            sequence2.add(1);
        }

        //mean
        double mean = sequence2.stream().mapToInt(Integer::intValue).average().getAsDouble();
        return (stdDev/mean)*100;
    }

}
