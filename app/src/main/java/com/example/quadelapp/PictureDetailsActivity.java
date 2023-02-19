package com.example.quadelapp;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.example.quadelapp.Models.Picture;
import com.example.quadelapp.Models.TimeSeriesData;
import com.example.quadelapp.services.RedisService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private ImageView ivCover, ivState;
    private LineChart ivChart;
    private BarChart barChart;
    private TextView tvDesc, tvDescChart;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolBarLayout;
    private Map<Float, String> valueMap = new HashMap<>();
    private ActivityPictureDetailsBinding binding;
    public class StringValueFormatter extends ValueFormatter {
        private Map<Float, String> valueMap;
        public StringValueFormatter(Map<Float, String> valueMap) {
            this.valueMap = valueMap;
        }
    }
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
        barChart = binding.getRoot().findViewById(R.id.iv_chartBar);
        ivState = binding.getRoot().findViewById(R.id.ivState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        pictureId = getIntent().getStringExtra("pictureId");
        activityId = getIntent().getStringExtra("activityId");
        getPictureFromRedisAndFillData(pictureId);
    }
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_picture_details, menu);
        MenuItem favouritesItem = menu.findItem(R.id.action_favourites);
        if(isFavouritedInPreferences(pictureId))
            favouritesItem.setIcon(R.drawable.icon_favourited40);
        else
            favouritesItem.setIcon(R.drawable.icon_add_to_favourites40);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favourites) {
            clickOnMenuFavourites(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
//    }

    private void getPictureFromRedisAndFillData(String pictureId){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);
        getPictureFromRedisByIDAndFillData(redisService, pictureId);
        getHarcodedDataForChart();
        //getAndShowDataInChart(redisService, pictureId);
    }
    private void setStateIcon(String state){
        if(Objects.equals(state, "ALARM")){
            ivState.setImageResource(R.drawable.ic_red_circle);
        }
        else if(Objects.equals(state, "FAULT")){
            ivState.setImageResource(R.drawable.ic_yellow_circle);
        }
        else if(Objects.equals(state, "OFF")){
            ivState.setImageResource(R.drawable.ic_grey_circle);
        }
        else if(Objects.equals(state, "OK")){
            ivState.setImageResource(R.drawable.ic_green_circle);
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
    private void clickOnStateIV(){showToastMessaggeShort("This is the state of this room");}

    private boolean isFavouritedInPreferences(String pictureId){
        SharedPreferences favorites = PictureDetailsActivity.this.getSharedPreferences("favorites",MODE_PRIVATE);
        return favorites.getBoolean(pictureId, false);
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

    private void getPictureFromRedisByIDAndFillData(RedisService redisService, String pictureId){
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

    private void getAndShowDataInChart(RedisService redisService, String elementId){
        Call<List<TimeSeriesData>> call = redisService.getTimeSeriesDataById(elementId);
        call.enqueue(new Callback<List<TimeSeriesData>>() {
            @Override
            public void onResponse(Call<List<TimeSeriesData>> call, Response<List<TimeSeriesData>> response) {
                if (response.isSuccessful()) {
                    List<TimeSeriesData> data = response.body();
                    showDataInBarChart(data);
                } else {
                    showToastMessaggeShort("Response body for TimeSeriesData is NULL!");
                }
            }
            @Override
            public void onFailure(Call<List<TimeSeriesData>> call, Throwable t) {
                showToastMessaggeShort("Something went wrong with retrieving TimeSeriesData from DB!");
            }
        });
    }
    private void setFields(Picture picture){
        toolBarLayout.setTitle(picture.getTitle());
        Drawable nav = toolbar.getNavigationIcon();
        if(nav != null) {
            nav.setTint(getResources().getColor(R.color.secondaryColor));
        }
        toolBarLayout.setExpandedTitleColor(getResources().getColor(R.color.secondaryColor));
        toolBarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.secondaryColor));
        setStateIcon(picture.getState());
        ivCover.setImageResource(picture.getImage());
        tvDesc.setText(picture.getDescription());
        tvDescChart.setText("Description of chart");
        getHarcodedDataForChart();
    }
    private void setIfFavouritedToPreferences(Picture pic){
        SharedPreferences favorites = getSharedPreferences("favorites",Context.MODE_PRIVATE);
        pic.setFavourite(favorites.getBoolean(pic.getId(), false));
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
    private void getHarcodedDataForChart(){

        List<TimeSeriesData>data = new ArrayList<>();
        TimeSeriesData tsdRecord = new TimeSeriesData(1640995200000L, "1");
        data.add(tsdRecord);
        //tsdRecord = new TimeSeriesData(Long.parseLong("1643673600000"), "1");
        tsdRecord = new TimeSeriesData(1643673600000L, "1");
        data.add(tsdRecord);
        tsdRecord = new TimeSeriesData(Long.parseLong("1646092800000"), "2");
        data.add(tsdRecord);
        tsdRecord = new TimeSeriesData(Long.parseLong("1648771200000"), "2");
        data.add(tsdRecord);
        tsdRecord = new TimeSeriesData(Long.parseLong("1651363200000"), "1");
        data.add(tsdRecord);
        data.add(tsdRecord);

        showDataInBarChart(data);
    }

    private String getFormattedDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return "TIME: "+dateFormat.format(new Date(timestamp * 1000 * 3600));
    }
    private String ConvertFloatValueToStringValue(float value){
        if(value==1) {
            return "OK";
        }
        else if (value==2) {
            return "OFF";
        }
        else if (value==3) {
            return "FAULT";
        }
        else
            return "ALARM";
    }

     private void showToastMessaggeShort(String messagge){
        Toast.makeText(PictureDetailsActivity.this, messagge, Toast.LENGTH_SHORT).show();
    }

    private void showDataInBarChart(List<TimeSeriesData> timeSeriesDataList){
        //BarChart chart = findViewById(R.id.bar_chart);


        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < timeSeriesDataList.size(); i++) {
            TimeSeriesData data = timeSeriesDataList.get(i);
            float value = Float.parseFloat(data.getValue());
            entries.add(new BarEntry(i, value));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly number of ALARMS");
        //dataSet.setColor(Color.BLUE);
        dataSet.setDrawValues(true);
        dataSet.setBarBorderWidth(1f);
        dataSet.setBarBorderColor(Color.BLACK);
        dataSet.setColor(Color.RED);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });

        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return String.format(Locale.getDefault(), "%.0f", barEntry.getY());
            }
        });

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.setDrawGridBackground(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(timeSeriesDataList.size());
        barChart.getXAxis().setDrawLabels(true);
        barChart.setBackgroundColor(Color.LTGRAY);


        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < timeSeriesDataList.size()) {
                    long timestamp = timeSeriesDataList.get(index).getTimestamp();
                    return getMonthYearFromUnixTimestamp(timestamp);
                } else {
                    return "";
                }
            }
        });
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(true);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawAxisLine(true);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisRight().setDrawAxisLine(true);
        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.invalidate();
    }

    public static String getMonthYearFromUnixTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        Date date = new Date(timestamp*1000*3600);
        return sdf.format(date);
    }
}