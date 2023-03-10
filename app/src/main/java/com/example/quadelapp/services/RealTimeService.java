package com.example.quadelapp.services;

import android.Manifest;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.quadelapp.FavouritesAndAlertFragment;
import com.example.quadelapp.Models.Picture;
import com.example.quadelapp.Models.SystemElement;
import com.example.quadelapp.PicturesFragment;
import com.example.quadelapp.R;
import com.example.quadelapp.SystemElementsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RealTimeService extends Service {
    public RealTimeService() {
    }
    private Timer timer;
    private TimerTask timerTask;
    private ArrayList<String> favouritedPictures;
    private RedisService redisService;
    private static final String CHANNEL_ID = "my_channel";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                sendRetrofitRequest();
            }
        };
        timer.schedule(timerTask, 0, 5000); // run the task every 5 seconds
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
    private void sendRetrofitRequest() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);
        fillDataOnFavouritesAndAlertFrgmentEvery2Secs();
    }
    private void fillDataOnFavouritesAndAlertFrgmentEvery2Secs() {
        if (!FavouritesAndAlertFragment.isQueryActive) {
            favouritedPictures = getAllTrueKeysFromPreferences();
            Call<List<Picture>> call = redisService.getFavoritesAndAlertPictures(favouritedPictures);
            call.enqueue(new Callback<List<Picture>>() {
                List<Picture> picturesList = new ArrayList<Picture>();
                @Override
                public void onResponse(Call<List<Picture>> call, Response<List<Picture>> response) {
                    if (response.isSuccessful()) {
                        picturesList = response.body();
                        for (Picture p : picturesList) {
                            changeFromCodeToWordState(p);
                            p.setImage(getResources().getIdentifier("toplana"+p.getTitle().toLowerCase(Locale.ROOT), "drawable", getPackageName()));
                        }
                        FavouritesAndAlertFragment.fullListPictures.clear();
                        FavouritesAndAlertFragment.fullListPictures.addAll(picturesList);
                        FavouritesAndAlertFragment.adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<List<Picture>> call, Throwable t) {
                    // handle the failure
                }
            });
        }
    }
    private void fillDataOnPicturesFragmentEvery2Secs(){
        if(!FavouritesAndAlertFragment.isQueryActive){
            Call<List<Picture>> call = redisService.getAllPictures();
            call.enqueue(new Callback<List<Picture>>() {
                List<Picture> picturesList = new ArrayList<Picture>();
                @Override
                public void onResponse(Call<List<Picture>> call, Response<List<Picture>> response) {
                    if (response.isSuccessful()) {
                        picturesList = response.body();
                        for(Picture p: picturesList) {
                            changeFromCodeToWordState(p);
                            p.setImage(getResources().getIdentifier("toplanadudara" , "drawable", getPackageName()));
                            //p.setImage(getResources().getIdentifier(p.getTitle(), "drawable", getActivity().getPackageName()));
                            setIfFavouritedFromPreferences(p);
                        }
                        PicturesFragment.pictures.clear();
                        PicturesFragment.pictures.addAll(picturesList);
                        PicturesFragment.adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<List<Picture>> call, Throwable t) {
                    // handle the failure
                }
            });
        }
    }

    private void fillDataOnSystemElementsFrgmentEvery2Secs(){
        if(!FavouritesAndAlertFragment.isQueryActive){
            Call<List<SystemElement>> call = redisService.getAllSystemElements();
            call.enqueue(new Callback<List<SystemElement>>() {
                List<SystemElement> systemElementsList = new ArrayList<SystemElement>();
                @Override
                public void onResponse(Call<List<SystemElement>> call, Response<List<SystemElement>> response) {
                    if (response.isSuccessful()) {
                        systemElementsList = response.body();
                        for(SystemElement e: systemElementsList) {
                            changeFromCodeToWordState(e);
                            e.setElementImage(getResources().getIdentifier("toplanadudara" , "drawable", getPackageName()));
                            //p.setImage(getResources().getIdentifier(p.getTitle(), "drawable", getActivity().getPackageName()));
                        }
                        SystemElementsFragment.elements.clear();
                        SystemElementsFragment.elements.addAll(systemElementsList);
                        SystemElementsFragment.adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<List<SystemElement>> call, Throwable t) {
                    // handle the failure
                }
            });
        }
    }

    private ArrayList<String> getAllTrueKeysFromPreferences(){
        Map<String, ?> favoriteIdsMap;
        SharedPreferences favoritesIds = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        favoriteIdsMap = favoritesIds.getAll();
        ArrayList<String> favoriteIdsList = new ArrayList<String>(favoriteIdsMap.keySet());
        return favoriteIdsList;
    }

    private void changeFromCodeToWordState(Picture pic) {
        if (Objects.equals(pic.getState(), "4")) {
            pic.setState("ALARM");
        } else if (Objects.equals(pic.getState(), "3")) {
            pic.setState("FAULT");
        } else if (Objects.equals(pic.getState(), "2")) {
            pic.setState("OFF");
        } else if (Objects.equals(pic.getState(), "1")) {
            pic.setState("OK");
        }
    }
    private void changeFromCodeToWordState(SystemElement systemElement) {
        if (Objects.equals(systemElement.getState(), "4")) {
            systemElement.setState("ALARM");
        } else if (Objects.equals(systemElement.getState(), "3")) {
            systemElement.setState("FAULT");
        } else if (Objects.equals(systemElement.getState(), "2")) {
            systemElement.setState("OFF");
        } else if (Objects.equals(systemElement.getState(), "1")) {
            systemElement.setState("OK");
        }
    }

    private void setIfFavouritedFromPreferences(Picture pic){
        SharedPreferences favorites = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        pic.setFavourite(favorites.getBoolean(pic.getId(), false));
    }
}

