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

    public static class MatchesByDateSorter implements Comparator<HistoricMatch> {
        private SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("dd/MM/yyyy"),
                new SimpleDateFormat("yyyy-MM-dd")
        };

        @Override
        public int compare(HistoricMatch obj1, HistoricMatch obj2) {
            Date date1 = parseDate(obj1.getMatchDate());
            Date date2 = parseDate(obj2.getMatchDate());

            if (date1 != null && date2 != null) {
                return date1.compareTo(date2);
            }

            // Handle cases where parsing fails by treating them as greater
            return 1;
        }

        private Date parseDate(String dateString) {
            for (SimpleDateFormat dateFormat : dateFormats) {
                try {
                    return dateFormat.parse(dateString);
                } catch (ParseException e) {
                    // Parsing failed, try the next format
                }
            }
            return null; // Parsing failed for all formats
        }
    }
}
