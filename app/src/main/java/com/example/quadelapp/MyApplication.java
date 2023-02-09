package com.example.quadelapp;

import android.app.Application;
import android.content.Intent;

import com.example.quadelapp.services.RealTimeService;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, RealTimeService.class);
        startService(intent);
    }
}