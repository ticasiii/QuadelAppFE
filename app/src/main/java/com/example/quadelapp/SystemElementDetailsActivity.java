package com.example.quadelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
    private ImageView ivCover, ivState;
    private LineChart ivChart;
    private BarChart barChart;
    private TextView tvDesc, tvDescChart;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolBarLayout;
    private Map<Float, String> valueMap = new HashMap<>();
    private ActivitySystemElementDetailsBinding binding;
    public class StringValueFormatter extends ValueFormatter {
        private Map<Float, String> valueMap;
        public StringValueFormatter(Map<Float, String> valueMap) {
            this.valueMap = valueMap;
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
        barChart = binding.getRoot().findViewById(R.id.bar_chart);
        ivState = binding.getRoot().findViewById(R.id.ivState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setValueMap();
        elementId = getIntent().getStringExtra("elementId");
        elementType = getIntent().getStringExtra("elementType");
        getSystemElementFromRedisAndFillData(elementType, elementId);
        //getDataForChart();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
//    @Override
//    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.system_element_details_menu, menu);
//        stateItem = menu.findItem(R.id.action_state);
//        return true;
//    }
    private void setValueMap(){
        valueMap.put(1.0f, "OK");
        valueMap.put(2.0f, "OFF");
        valueMap.put(3.0f, "FAULT");
        valueMap.put(4.0f, "ALARM");
    }
    private void getSystemElementFromRedisAndFillData(String elementType, String elementId){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);
        if(elementType == "cp") {
            getControlPanelFromRedisAndFillData(redisService, elementId);
        }
        else {
            getElementFromRedisAndFillData(redisService, elementId);
        }
        if(Integer.parseInt(elementId)<27)
            getDataForChart(redisService, elementId + 27);
        else
            getDataForChart(redisService, elementId);
    }
    private void getControlPanelFromRedisAndFillData(RedisService redisService, String controlPanelId){
        Call<ControlPanel> call = redisService.getControlPanelById(controlPanelId);
        call.enqueue(new Callback<ControlPanel>() {
            @Override
            public void onResponse(@NonNull Call<ControlPanel> call, @NonNull Response<ControlPanel> response) {
                if(response.body() != null)
                    getControlPanelDataFromResponse(response, controlPanelId);
                else
                    showToastMessaggeShort("Response body for CONTROLPANEL is NULL!");
            }
            @Override
            public void onFailure(Call<ControlPanel> call, Throwable t) {
                showToastMessaggeShort("Something went wrong with retrieving CONTROLPANEL from DB!");
            }
        });
    }
    private void getControlPanelDataFromResponse(Response<ControlPanel> response, String controlPanelId){
        ControlPanel cp = new ControlPanel(controlPanelId);
        cp.setTitle(response.body().getTitle());
        cp.setDescription(response.body().getDescription());
        cp.setState(response.body().getState());
        changeFromCodeToWordState(cp);
        //cp.setElementImage(getResources().getIdentifier("toplanadudara" , "drawable", SystemElementDetailsActivity.this.getPackageName()));
        cp.setElementImage(getResources().getIdentifier(cp.getTitle().toLowerCase(Locale.ROOT), "drawable", SystemElementDetailsActivity.this.getPackageName()));
        setFields(cp);
    }

    private void setFields(ControlPanel cp){
        toolBarLayout.setTitle(cp.getTitle());
        Drawable nav = toolbar.getNavigationIcon();
        if(nav != null) {
            nav.setTint(getResources().getColor(R.color.secondaryColor));
        }
        toolBarLayout.setExpandedTitleColor(getResources().getColor(R.color.secondaryColor));
        toolBarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.secondaryColor));
        ivCover.setImageResource(cp.getElementImage());
        tvDesc.setText(cp.getDescription());
        tvDescChart.setText("Description of chart");
        setStateIcon(cp.getState());
        //getHarcodedDataForChart();
        //getHarcodedDataForBarChart();

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

    private void getElementFromRedisAndFillData(RedisService redisService, String elementId){
        Call<Element> call = redisService.getElementById(elementId);
        call.enqueue(new Callback<Element>() {
            @Override
            public void onResponse(@NonNull Call<Element> call, @NonNull Response<Element> response) {
                if(response.body() != null)
                    getElementDataFromResponse(response, elementId);
                else
                    showToastMessaggeShort("Response body for ELEMENT is NULL!");
            }
            @Override
            public void onFailure(Call<Element> call, Throwable t) {
                showToastMessaggeShort("Something went wrong with retrieving ELEMENT from DB!");
            }
        });
    }
    private void getElementDataFromResponse(Response<Element> response, String elementId){
        Element e = new Element(elementId);
        e.setTitle(response.body().getTitle());
        e.setDescription(response.body().getDescription());
        e.setState(response.body().getState());
        changeFromCodeToWordState(e);
        //e.setElementImage(getResources().getIdentifier("toplanadudara" , "drawable", SystemElementDetailsActivity.this.getPackageName()));
        e.setElementImage(getResources().getIdentifier(e.getTitle().toLowerCase(Locale.ROOT) , "drawable", SystemElementDetailsActivity.this.getPackageName()));

        setFields(e);
    }

    private void setFields(Element e){
        toolBarLayout.setTitle(e.getTitle());
        Drawable nav = toolbar.getNavigationIcon();
        if(nav != null) {
            nav.setTint(getResources().getColor(R.color.secondaryColor));
        }
        toolBarLayout.setExpandedTitleColor(getResources().getColor(R.color.secondaryColor));
        toolBarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.secondaryColor));
        ivCover.setImageResource(e.getElementImage());
        tvDesc.setText(e.getDescription());
        tvDescChart.setText("Description of chart");
        setStateIcon(e.getState());
        //getHarcodedDataForChart();
        //getHarcodedDataForBarChart();
        //getDataForChart(redisService, elementId);
    }
    private void changeFromCodeToWordState(SystemElement el){

        if(Objects.equals(el.getState(), "4"))
            el.setState("ALARM");
        else if (Objects.equals(el.getState(), "2"))
            el.setState("OFF");
        else if (Objects.equals(el.getState(), "3"))
            el.setState("FAULT");
        else
            el.setState("OK");
    }
    private void getDataForChart(RedisService redisService, String elementId){
        Call<List<TimeSeriesData>> call = redisService.getTimeSeriesDataById(elementId);
        call.enqueue(new Callback<List<TimeSeriesData>>() {
            @Override
            public void onResponse(Call<List<TimeSeriesData>> call, Response<List<TimeSeriesData>> response) {
                if (response.isSuccessful()) {
                    List<TimeSeriesData> data = response.body();
                    drawChart(data);
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

    private void drawChart(List<TimeSeriesData> timeSeriesDataList) {
        boolean hasDataWithValue = false;
        for (TimeSeriesData data : timeSeriesDataList) {
            if (Float.valueOf(data.getValue()) > 0) {
                hasDataWithValue = true;
                break;
            }
        }
        if (hasDataWithValue) {
            // Create Bar Chart
            barChart.setVisibility(View.VISIBLE);
            showDataInBarChart(timeSeriesDataList);
        } else {
            // Create Line Chart with a value of 1
            ivChart.setVisibility(View.VISIBLE);
            createLineChartWithValueOK();
        }
    }

    private void createLineChartWithValueOK() {
        LineChart chart = findViewById(R.id.iv_chart);

        // create a data set with a value of 1
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 1));
        entries.add(new Entry(1, 1));

        LineDataSet dataSet = new LineDataSet(entries, "State have always been OK");

        dataSet.setColor(Color.BLACK);
        dataSet.setLineWidth(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        dataSet.setDrawCircles(false);
        //dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });

        LineData lineData = new LineData(dataSet);

        // customize x-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setAxisLineWidth(3f);
        xAxis.setLabelCount(-2);
        //xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "State have always been OK";
            }
        });

        // customize y-axis
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(true);
        yAxis.setAxisLineColor(Color.BLACK);
        //yAxis.setLabelCount(0);
        yAxis.setAxisLineWidth(3f);
        //yAxis.setCenterAxisLabels(true);

//        yAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                return "1";
//            }
//        });

        chart.setData(lineData);
        chart.setBackgroundColor(Color.LTGRAY);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.invalidate();
    }


    private void getHarcodedDataForBarChart(){

        List<TimeSeriesData>data = new ArrayList<>();
        //make some static data
        TimeSeriesData tsdRecord = new TimeSeriesData(1318948745000L, "2145");
        data.add(tsdRecord);
        //tsdRecord = new TimeSeriesData(Long.parseLong("1643673600000"), "1");
        tsdRecord = new TimeSeriesData(1350571145000L, "4210");
        data.add(tsdRecord);
        tsdRecord = new TimeSeriesData(Long.parseLong("1660833545000"), "2");
        data.add(tsdRecord);
        tsdRecord = new TimeSeriesData(Long.parseLong("1663511945000"), "2");
        data.add(tsdRecord);
        tsdRecord = new TimeSeriesData(Long.parseLong("1666103945000"), "1");
        data.add(tsdRecord);

        showDataInBarChart(data);
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
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    private void showDataInChart(List<TimeSeriesData> data) {
        LineChart chart = findViewById(R.id.iv_chart);
        List<Entry> entries = new ArrayList<>();
        for (TimeSeriesData timeSeriesData : data) {
            entries.add(new Entry(timeSeriesData.getTimestamp(), Float.parseFloat(timeSeriesData.getValue())));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Element Data");
        setDataSet(dataSet);
        setNamesOfAxiss(chart);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    private void setDataSet(LineDataSet dataSet){
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new StringValueFormatter(valueMap));

    }
    private void getHarcodedDataForChart(){

        List<TimeSeriesData>data = new ArrayList<>();
        //make some static data
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
        Toast.makeText(SystemElementDetailsActivity.this, messagge, Toast.LENGTH_SHORT).show();
    }
}
