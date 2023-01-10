package com.api.BetStrat.constants;

public enum SimpleBetStatusEnum {

    WON(10),
    LOST(20),
    REFUNDED(30),
    ONGOING(40),
    CANCELED(50);

    private final int value;

    SimpleBetStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
