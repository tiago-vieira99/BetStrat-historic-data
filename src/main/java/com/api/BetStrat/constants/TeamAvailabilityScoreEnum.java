package com.api.BetStrat.constants;

public enum TeamAvailabilityScoreEnum {

    INSUFFICIENT_DATA("INSUFFICIENT_DATA"),
    INAPT("INAPT"),
    RISKY("RISKY"),
    ACCEPTABLE("ACCEPTABLE"),
    EXCELLENT("EXCELLENT");

    private final String value;

    TeamAvailabilityScoreEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
