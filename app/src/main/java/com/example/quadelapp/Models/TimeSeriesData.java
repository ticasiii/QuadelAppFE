package com.example.quadelapp.Models;

public class TimeSeriesData {
    private long timestamp;
    private float value;

    public TimeSeriesData(long timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getValue() {
        return value;
    }
}