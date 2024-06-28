package com.api.BetStrat.enums;

public enum TeamScoreEnum {

    INSUFFICIENT_DATA("INSUFFICIENT_DATA"),
    INAPT("INAPT"),
    RISKY("RISKY"),
    ACCEPTABLE("ACCEPTABLE"),
    EXCELLENT("EXCELLENT");

    private final String value;

    TeamScoreEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
