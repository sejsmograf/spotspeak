package com.example.spotspeak.constants;

public class TraceConstants {
    public static final int TRACE_DISCOVERY_DISTANCE = 50;
    public static final int TRACE_EXPIRATION_HOURS = 24;
    public static final int EXPIRED_TRACE_CLEANUP_INTERVAL_MS = 1000 * 60; // 1 minute

    public static final int EVENT_EPSILON_METERS = 500;
    public static final int EVENT_MIN_POINTS = 5;
    public static final int EVENT_EXPIRATION_HOURS = 12;
}
