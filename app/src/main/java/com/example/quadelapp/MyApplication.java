package com.example.quadelapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.example.quadelapp.services.RealTimeService;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, RealTimeService.class);
        //startService(intent);
    }
}