package com.api.BetStrat.util;

import lombok.SneakyThrows;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Utils {

    @SneakyThrows
    public static double beautifyDoubleValue (double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        NumberFormat nf = NumberFormat.getInstance();
        return nf.parse(df.format(value)).doubleValue();
    }
}
