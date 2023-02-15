package com.example.quadelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quadelapp.Models.ControlPanel;
import com.example.quadelapp.Models.Element;
import com.example.quadelapp.Models.Picture;
import com.example.quadelapp.Models.SystemElement;
import com.example.quadelapp.Models.TimeSeriesData;
import com.example.quadelapp.databinding.ActivitySystemElementDetailsBinding;
import com.example.quadelapp.services.RedisService;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import com.github.mikephil.charting.charts.LineChart;

public class SystemElementDetailsActivity extends AppCompatActivity {

    private String elementId, elementType;
    private RedisService redisService;

    private Map<String, Picture> pictures;

    private ImageView ivCover;
    private LineChart ivChart;
    private TextView tvDesc, tvDescChart;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolBarLayout;
    private String ts1String;
    private long ts1Long;
    private Map<Float, String> valueMap = new HashMap<>();
    private List<String> xLabels = new ArrayList<>();



    private ActivitySystemElementDetailsBinding binding;


    public class StringValueFormatter extends ValueFormatter {
        private Map<Float, String> valueMap;

        public StringValueFormatter(Map<Float, String> valueMap) {
            this.valueMap = valueMap;
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return valueMap.get(value);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySystemElementDetailsBinding.inflate(getLayoutInflater());
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

        elementId = getIntent().getStringExtra("elementId");
        elementType = getIntent().getStringExtra("elementType");

        if(elementType == "cp"){
            getControlPanelFromRedisAndFillData(elementId);
        }
        else{
            getElementFromRedisAndFillData(elementId);
        }

        valueMap.put(1.0f, "OK");
        valueMap.put(2.0f, "OFF");
        valueMap.put(3.0f, "FAULT");
        valueMap.put(4.0f, "ALARM");

        //getDataForChart();


    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setToolbarColorBasedOnState(CollapsingToolbarLayout toolBarLayout, String state){
        int color;
        if(Objects.equals(state, "ALARM")){
            color = Color.RED;
        }
        else if(Objects.equals(state, "ERROR"))
        {
            color = Color.YELLOW;
        } else if (Objects.equals(state, "OFF")) {
            color = Color.GRAY;
        }
        else {
            color = Color.GREEN;
        }
        toolBarLayout.setContentScrimColor(color);
        // adding the color to be shown
        ObjectAnimator animator1 = ObjectAnimator.ofInt(toolBarLayout, "backgroundColor", color, Color.WHITE, color);
        setAnimator(animator1);

        ObjectAnimator animator2 = ObjectAnimator.ofInt(toolBarLayout, "contentScrimColor", color, Color.WHITE, color);
        setAnimator(animator2);

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
    private void getControlPanelFromRedisAndFillData(String controlPanelId){


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);


        Call<ControlPanel> call = redisService.getControlPanelById(controlPanelId);

        call.enqueue(new Callback<ControlPanel>() {
            @Override
            public void onResponse(@NonNull Call<ControlPanel> call, @NonNull Response<ControlPanel> response) {

                if(response.body() != null){
                    ControlPanel cp = new ControlPanel(controlPanelId);

                    cp.setTitle(response.body().getTitle());
                    cp.setDescription(response.body().getDescription());
                    cp.setState(response.body().getState());
                    changeFromCodeToWordState(cp);
                    cp.setElementImage(getResources().getIdentifier("toplanadudara" , "drawable", SystemElementDetailsActivity.this.getPackageName()));
                    //cp.setElementImage(getResources().getIdentifier(cp.getTitle(), "drawable", ControlPanelDetailsActivity.this.getPackageName()));



                    setFields(cp);
                }
                else{
                    showToastMessaggeShort("Response body for CONTROLPANEL is NULL!");
                }
            }
            @Override
            public void onFailure(Call<ControlPanel> call, Throwable t) {
                showToastMessaggeShort("Something went wrong with retrieving CONTROLPANEL from DB!");
            }
        });
    }

    private void setFields(ControlPanel cp){
        toolBarLayout.setTitle(cp.getTitle());
        Drawable nav = toolbar.getNavigationIcon();
        if(nav != null) {
            nav.setTint(getResources().getColor(R.color.black));
        }        toolBarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.black));
        toolBarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.black));

        setToolbarColorBasedOnState(toolBarLayout, cp.getState());

        ivCover.setImageResource(cp.getElementImage());
        tvDesc.setText(cp.getDescription());
        tvDescChart.setText("Description of chart");
        getHarcodedDataForChart();

        //ivChart.setImageResource(cp.getElementImage());

    }

    private void getElementFromRedisAndFillData(String elementId){


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);


        Call<Element> call = redisService.getElementById(elementId);

        call.enqueue(new Callback<Element>() {
            @Override
            public void onResponse(@NonNull Call<Element> call, @NonNull Response<Element> response) {

                if(response.body() != null){
                    Element e = new Element(elementId);

                    e.setTitle(response.body().getTitle());
                    e.setDescription(response.body().getDescription());
                    e.setState(response.body().getState());
                    changeFromCodeToWordState(e);
                    e.setElementImage(getResources().getIdentifier("toplanadudara" , "drawable", SystemElementDetailsActivity.this.getPackageName()));
                    //cp.setElementImage(getResources().getIdentifier(cp.getTitle(), "drawable", ControlPanelDetailsActivity.this.getPackageName()));


                    setFields(e);
                }
                else{
                    showToastMessaggeShort("Response body for ELEMENT is NULL!");
                }
            }
            @Override
            public void onFailure(Call<Element> call, Throwable t) {
                showToastMessaggeShort("Something went wrong with retrieving ELEMENT from DB!");
            }
        });
    }

    private void setFields(Element e){
        toolBarLayout.setTitle(e.getTitle());
        Drawable nav = toolbar.getNavigationIcon();
        if(nav != null) {
            nav.setTint(getResources().getColor(R.color.black));
        }        toolBarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.black));
        toolBarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.black));

        setToolbarColorBasedOnState(toolBarLayout, e.getState());

        ivCover.setImageResource(e.getElementImage());
        tvDesc.setText(e.getDescription());
        tvDescChart.setText("Description of chart");
        getHarcodedDataForChart();

        //ivChart.setImageResource(e.getElementImage());

    }

    private void changeFromCodeToWordState(SystemElement el){

        switch (el.getState()){
            case "1":
                el.setState("OK");
            case "2":
                el.setState("OFF");
            case "3":
                el.setState("FAULT");
            case "4":
                el.setState("ALARM");
            default:
                el.setState("OK");
        }

    }

    private void getDataForChart(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);

        Call<List<TimeSeriesData>> call = redisService.getTimeSeriesDataById(elementId);

        call.enqueue(new Callback<List<TimeSeriesData>>() {
            @Override
            public void onResponse(Call<List<TimeSeriesData>> call, Response<List<TimeSeriesData>> response) {
                if (response.isSuccessful()) {
                    List<TimeSeriesData> data = response.body();
                    // Do something with the data, for example, show it in a chart
                    showDataInChart(data);
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

    private void showDataInChart(List<TimeSeriesData> data) {
        LineChart chart = findViewById(R.id.iv_chart);

        List<Entry> entries = new ArrayList<>();
        for (TimeSeriesData timeSeriesData : data) {
            entries.add(new Entry(timeSeriesData.getTimestamp(), Float.parseFloat(timeSeriesData.getValue())));
        }
        xLabels.add(String.valueOf(entries.get(0).getX()));

        xLabels.add(String.valueOf(entries.get(entries.size()-1).getX()));


        LineDataSet dataSet = new LineDataSet(entries, "Element Data");
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);

        dataSet.setValueFormatter(new StringValueFormatter(valueMap));
        //dataSet.value
        setNamesOfAxiss(chart);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();

    }



    private void getHarcodedDataForChart(){

        List<TimeSeriesData>data = new ArrayList<>();
        //make some static data
        TimeSeriesData tsd = new TimeSeriesData(Long.parseLong("1640995200000"), "1");
        data.add(tsd);
        tsd = new TimeSeriesData(Long.parseLong("1643673600000"), "1");
        data.add(tsd);
        tsd = new TimeSeriesData(Long.parseLong("1646092800000"), "2");
        data.add(tsd);
        tsd = new TimeSeriesData(Long.parseLong("1648771200000"), "2");
        data.add(tsd);
        tsd = new TimeSeriesData(Long.parseLong("1651363200000"), "1");
        data.add(tsd);
//        tsd = new TimeSeriesData(1643065200, "4");
//        data.add(tsd);
//        tsd = new TimeSeriesData(1643151600, "4");
//        data.add(tsd);
//        tsd = new TimeSeriesData(1643238000, "1");
        data.add(tsd);
//        tsd = new TimeSeriesData(1643151600, "1");
//        data.add(tsd);

        showDataInChart(data);
    }
    private void setNamesOfAxiss(LineChart chart) {
        //LineChart chart = findViewById(R.id.iv_chart);

// Set the X-axis name
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
             @Override
             public String getAxisLabel(float value, AxisBase axis) {
                 // Format the X-axis label as desired
                 //return "Time: " +value;
                 return getFormattedDate((long)value);
             }
//            public String getFormattedValue(float value) {
//                // Convert the X-axis value to a date string
//                return getFormattedDate((long)value);
//                 //return String.valueOf(value);
//
//            }
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

        chart.invalidate();
    }

    private String getFormattedDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return "TIME: "+dateFormat.format(new Date(timestamp));
    }

    private String ConvertFloatValueToStringValue(float value){
        if(value==1)
            return "OK";
        else if (value==2) {
            return "OFF";
        } else if (value==3) {
            return "FAULT";
        }
        else
            return "ALARM";
    }

    private void showToastMessaggeShort(String messagge){
        Toast.makeText(SystemElementDetailsActivity.this, messagge, Toast.LENGTH_SHORT).show();
    }
}
