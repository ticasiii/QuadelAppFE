package com.example.quadelapp.Models;

public class TimeSeriesData {
    private long timestamp;
    private String value;
    public TimeSeriesData(long timestamp, String value) {
        this.timestamp = timestamp / 1000 / 3600;
        this.value = value;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public String getValue() {
        return value;
    }
}