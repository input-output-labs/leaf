package fr.iolabs.leaf.scheduler.model;

/**
 * Enumeration of frequencies.
 * Each frequency represents a time interval at which scheduled tasks should be executed.
 */
public enum Frequency {
    NEVER,
    FIVE_MINUTES,
    THIRTY_MINUTES,
    ONE_HOUR,
    TWO_HOURS,
    SIX_HOURS,
    TWELVE_HOURS,
    DAILY,
    WEEKLY,
    MONTHLY;
}
