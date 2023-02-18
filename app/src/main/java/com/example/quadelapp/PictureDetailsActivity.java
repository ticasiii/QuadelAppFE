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
    private BarChart ivChartBar;
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
        ivChartBar = binding.getRoot().findViewById(R.id.iv_chartBar);
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

        showDataInChart(data);
    }
    private void setNamesOfAxiss(BarChart chart) {
        // Set the X-axis name
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Format the X-axis label as desired
                //return "Time: " +value;
                return getFormattedDate((long)value);
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1
        xAxis.setLabelRotationAngle(-45);

        // Set the Y-axis name
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Format the Y-axis label as desired
                return "State: " + ConvertFloatValueToStringValue(value);
            }
        });
        yAxis.setDrawGridLines(false);
        yAxis.setGranularity(1f);
        //
        chart.invalidate();
    }
    private void setNamesOfAxiss(LineChart chart) {
        // Set the X-axis name
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Format the X-axis label as desired
                //return "Time: " +value;
                return getFormattedDate((long)value);
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1
        xAxis.setLabelRotationAngle(-45);

        // Set the Y-axis name
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Format the Y-axis label as desired
                return "State: " + ConvertFloatValueToStringValue(value);
            }
        });
        yAxis.setDrawGridLines(false);
        yAxis.setGranularity(1f);
        //
        chart.invalidate();
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

    private void showDataInChart(List<TimeSeriesData> data) {
        List<Entry> entries = new ArrayList<>();
        for (TimeSeriesData timeSeriesData : data) {
            entries.add(new Entry(timeSeriesData.getTimestamp(), Float.parseFloat(timeSeriesData.getValue())));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Element Data");
        setDataSet(dataSet);
        setNamesOfAxiss(ivChart);
        LineData lineData = new LineData(dataSet);
        ivChart.setData(lineData);
        ivChart.invalidate();
    }

    private void showDataInBarChart(List<TimeSeriesData> data) {
        List<BarEntry> entries = new ArrayList<>();
        for (TimeSeriesData timeSeriesData : data) {
            entries.add(new BarEntry(timeSeriesData.getTimestamp(), Float.parseFloat(timeSeriesData.getValue())));
        }
        BarDataSet dataSet = new BarDataSet(entries, "Element data");
        //setDataSet(dataSet);
        //setNamesOfAxiss(chart);
        BarData barData = new BarData(dataSet);
        ivChartBar.setData(barData);
        ivChartBar.invalidate();
    }

    private void setDataSet(BarDataSet dataSet){
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new PictureDetailsActivity.StringValueFormatter(valueMap));
    }
    private void setDataSet(LineDataSet dataSet){
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new PictureDetailsActivity.StringValueFormatter(valueMap));
    }

    private void showToastMessaggeShort(String messagge){
        Toast.makeText(PictureDetailsActivity.this, messagge, Toast.LENGTH_SHORT).show();
    }
}