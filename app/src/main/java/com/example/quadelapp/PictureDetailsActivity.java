package com.example.quadelapp;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.example.quadelapp.Models.Picture;
import com.example.quadelapp.services.RedisService;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quadelapp.databinding.ActivityPictureDetailsBinding;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PictureDetailsActivity extends AppCompatActivity {

    //public Picture pic;
    private String pictureId, activityId;
    private RedisService redisService;

    private Map<String, Picture> pictures;

    private ImageView ivCover;
    private LineChart ivChart;
    private TextView tvDesc, tvDescChart;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolBarLayout;
    private MenuItem stateItem;


    private ActivityPictureDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPictureDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolBarLayout = binding.toolbarLayout;
        ivCover = binding.getRoot().findViewById(R.id.imageview_cover);
        tvDesc = binding.getRoot().findViewById(R.id.tv_description);
        tvDescChart = binding.getRoot().findViewById(R.id.tv_chart_description);
        ivChart = binding.getRoot().findViewById(R.id.iv_chart);
        //ImageView fab = binding.fab;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pictureId = getIntent().getStringExtra("pictureId");
        activityId = getIntent().getStringExtra("activityId");

        getPictureFromRedisAndFillData(pictureId);

/*        if(picture.isFavourite()){
            fab.setImageResource(R.drawable.icon_favourited80);
        }
        else{
            fab.setImageResource(R.drawable.icon_add_to_favourites80);
        }

        //FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                if(!picture.isFavourite()){
                    addPictureToFavourites();
                    picture.setFavourite(true);
                    fab.setImageResource(R.drawable.icon_favourited80);
                }
                else{
                    removePictureFromFavourites();
                    picture.setFavourite(false);
                    fab.setImageResource(R.drawable.icon_add_to_favourites80);
                }
            }
        });*/
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

//    private void setToolbarColorBasedOnState(CollapsingToolbarLayout toolBarLayout, String state){
//        int color;
//        if(Objects.equals(state, "ALARM")){
//            color = Color.RED;
//        }
//        else if(Objects.equals(state, "ERROR"))
//        {
//            color = Color.YELLOW;
//        } else if (Objects.equals(state, "OFF")) {
//            color = Color.GRAY;
//        }
//        else {
//            color = Color.GREEN;
//        }
//        toolBarLayout.setContentScrimColor(color);
//            // adding the color to be shown
//            ObjectAnimator animator1 = ObjectAnimator.ofInt(toolBarLayout, "backgroundColor", color, Color.WHITE, color);
//            setAnimator(animator1);
//
//            ObjectAnimator animator2 = ObjectAnimator.ofInt(toolBarLayout, "contentScrimColor", color, Color.WHITE, color);
//            setAnimator(animator2);
//
//    }

    private void setMenuIconBasedOnState(MenuItem menuItem, String state){
        if(Objects.equals(state, "ALARM")){
            menuItem.setIcon(R.drawable.ic_red_circle);
        }
        else if(Objects.equals(state, "FAULT")){
            menuItem.setIcon(R.drawable.ic_yellow_circle);
        }
        else if(Objects.equals(state, "OFF")){
            menuItem.setIcon(R.drawable.ic_grey_circle);
        }
        else if(Objects.equals(state, "OK")){
            menuItem.setIcon(R.drawable.ic_green_circle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_picture_details, menu);
        MenuItem favouritesItem = menu.findItem(R.id.action_favourites);
        stateItem = menu.findItem(R.id.action_state);

        if(isFavouritedInPreferences(pictureId)){
            favouritesItem.setIcon(R.drawable.icon_favourited40);
        }
        else{
            favouritesItem.setIcon(R.drawable.icon_add_to_favourites40);
        }


        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_favourites:
                clickOnMenuFavourites(item);
                return true;
            case R.id.action_state:
                clickOnMenuState();
/*            case R.id.help:
                showHelp();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clickOnMenuFavourites(MenuItem item){
        if(!isFavouritedInPreferences(pictureId)){
            addPictureToFavourites(pictureId);
            item.setIcon(R.drawable.icon_favourited40);
        }
        else{
            removePictureFromFavourites(pictureId);
            item.setIcon(R.drawable.icon_add_to_favourites40);
        }
    }
    private void clickOnMenuState(){
        showToastMessaggeShort("This is the state of this room");
    }

    private boolean isFavouritedInPreferences(String pictureId){
        SharedPreferences favorites = PictureDetailsActivity.this.getSharedPreferences("favorites",MODE_PRIVATE);
        return favorites.getBoolean(pictureId, false);
    }


    private void setAnimator(ObjectAnimator animator){

        // duration of one color
        animator.setDuration(800);
        animator.setEvaluator(new ArgbEvaluator());

        // color will be show in reverse manner
        animator.setRepeatCount(Animation.REVERSE);

        // It will be repeated up to infinite time
        animator.setRepeatCount(Animation.INFINITE);
        animator.start();
    }
    private void addPictureToFavourites(){
        showToastMessaggeShort("Picture is added to favourites");
    }

    private void removePictureFromFavourites(){
        showToastMessaggeShort("Picture is removed from favourites");
    }


    private void addPictureToFavourites(String pictureId){
        savePictureFavouriteStateToPreferences(pictureId);
        showToastMessaggeShort("Picture is added to favourites");
    }

    private void savePictureFavouriteStateToPreferences(String pictureId){
        SharedPreferences favorites = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = favorites.edit();
        editor.putBoolean(pictureId, true);
        editor.apply();
    }

    private void removePictureFromFavourites(String pictureId){
        removePictureFavouriteStateFromPreferences(pictureId);
        showToastMessaggeShort("Picture is removed from favourites");
    }

    private void removePictureFavouriteStateFromPreferences(String pictureId){
        SharedPreferences favorites = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = favorites.edit();
        editor.remove(pictureId);
        editor.commit();
    }

    private void switchFavoriteState(Picture picture){
        if(picture.isFavourite()) {
            picture.setFavourite(false);
        }
        else picture.setFavourite(true);
    }

    private void getPictureFromRedisAndFillData(String pictureId){


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);


        Call<Picture> call = redisService.getPictureById(pictureId);

        call.enqueue(new Callback<Picture>() {
            @Override
            public void onResponse(@NonNull Call<Picture> call, @NonNull Response<Picture> response) {

                if(response.body() != null){
                    Picture picture = new Picture(pictureId);

                    picture.setTitle(response.body().getTitle());
                    picture.setDescription(response.body().getDescription());
                    picture.setState(response.body().getState());
                    setIfFavouritedToPreferences(picture);
                    changeFromCodeToWordState(picture);
                    picture.setImage(getResources().getIdentifier("toplanadudara" , "drawable", PictureDetailsActivity.this.getPackageName()));
                    //picture.setImage(getResources().getIdentifier(picture.getTitle(), "drawable", getActivity().getPackageName()));


                    setFields(picture);
                }
                else{
                    showToastMessaggeShort("Response body is NULL!");
                }
            }
            @Override
            public void onFailure(Call<Picture> call, Throwable t) {
                showToastMessaggeShort("Something went wrong with retrieving PICTURE from DB!");
            }
        });
    }

    private void setFields(Picture picture){
        toolBarLayout.setTitle(picture.getTitle());
        Drawable nav = toolbar.getNavigationIcon();
        if(nav != null) {
            nav.setTint(getResources().getColor(R.color.black));
        }        toolBarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.black));
        toolBarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.black));

        //setToolbarColorBasedOnState(toolBarLayout, picture.getState());
        setMenuIconBasedOnState(stateItem, picture.getState());

        ivCover.setImageResource(picture.getImage());
        tvDesc.setText(picture.getDescription());
        tvDescChart.setText("Description of chart");
        //ivChart.setImageResource(picture.getImage());

    }

    private void setIfFavouritedToPreferences(Picture pic){
        SharedPreferences favorites = getSharedPreferences("favorites",Context.MODE_PRIVATE);
        pic.setFavourite(favorites.getBoolean(pic.getId(), false));
    }

     private void changeFromCodeToWordState(Picture pic){

        switch (pic.getState()){
            case "1":
                pic.setState("OK");
            case "2":
                pic.setState("OFF");
            case "3":
                pic.setState("ERROR");
            case "4":
                pic.setState("ALARM");
            default:
                pic.setState("OK");
        }

    }

    private void showToastMessaggeShort(String messagge){
        Toast.makeText(PictureDetailsActivity.this, messagge, Toast.LENGTH_SHORT).show();
    }
}