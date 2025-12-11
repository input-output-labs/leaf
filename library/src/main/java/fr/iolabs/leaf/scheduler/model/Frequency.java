package fr.iolabs.leaf.scheduler.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of healthcheck frequencies.
 * Each frequency represents a time interval at which healthchecks should be executed.
 */
public enum Frequency {
    NEVER("Never"),
    FIVE_MINUTES("5minutes"),
    THIRTY_MINUTES("30minutes"),
    ONE_HOUR("1h"),
    TWO_HOURS("2h"),
    SIX_HOURS("6h"),
    TWELVE_HOURS("12h"),
    DAILY("1 day"),
    WEEKLY("1 week"),
    MONTHLY("1 month");

    private final String value;

    Frequency(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Frequency fromString(String value) {
        for (Frequency frequency : Frequency.values()) {
            if (frequency.value.equals(value)) {
                return frequency;
            }
        }
        throw new IllegalArgumentException("Unknown frequency: " + value);
    }
}
