package com.api.BetStrat.util;

import com.api.BetStrat.entity.HistoricMatch;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

public class Utils {

    private double mean = this.mean;

    @SneakyThrows
    public static double beautifyDoubleValue (double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        NumberFormat nf = NumberFormat.getInstance();
        return nf.parse(df.format(value)).doubleValue();
    }

    public static String findMainCompetition (List<HistoricMatch> historicMatches) {
        List<HistoricMatch> copyHistoricMatches = historicMatches;
        return copyHistoricMatches.stream()
                // filter non null competition matches
                .filter(m -> Objects.nonNull(m.getCompetition()))
                // consider all phases of the main league
                .map(m -> {
                    m.setCompetition(m.getCompetition().replaceAll("Promotion", "").replaceAll("Relegation", "")
                        .replaceAll("Playoffs", "").replaceAll("Finals", "").replaceAll("Meistergruppe", "")
                        .replaceAll("Qualifikationsgruppe", "").replaceAll("Playoff", "").replaceAll("Primeira Fase", "")
                        .replaceAll("Segunda Fase", "").replaceAll("Apertura", "").replaceAll("Clausura", "")
                        .replaceAll("Final", "").replaceAll("Intermedio", "").replaceAll("Primera Etapa", "")
                        .replaceAll("Segunda Etapa", "").replaceAll("Play Off", ""));
                    return m;
                })
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
