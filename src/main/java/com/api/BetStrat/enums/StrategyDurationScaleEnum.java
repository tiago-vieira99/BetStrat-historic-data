package com.api.BetStrat.enums;

public enum StrategyDurationScaleEnum {

    // a sequence following strategy X must be green after Y games max. After that, the stake grows very exponentially

    SHORT(12),                  // Y : 1, 2
    MEDIUM_SHORT(34),           // Y : 3, 4
    MEDIUM(56),                 // Y : 5, 6
    MEDIUM_LONG(78),            // Y : 7, 8
    LONG(80);                   // Y : 8, 8+

    private final int value;

    StrategyDurationScaleEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
