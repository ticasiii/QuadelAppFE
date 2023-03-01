package com.example.quadelapp.services;


import com.example.quadelapp.Models.ControlPanel;
import com.example.quadelapp.Models.Element;
import com.example.quadelapp.Models.Picture;
import com.example.quadelapp.Models.SystemElement;
import com.example.quadelapp.Models.TimeSeriesData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RedisService {


    @GET("picture/bySearch/{queryText}")
    Call<List<Picture>> getFilteredPictures(@Path("queryText")  String queryText);
    @GET("controlpanels/timeseries/numberOfAlarmsOfElement/{elementId}")
    Call<List<TimeSeriesData>> getTimeSeriesDataById(@Path("elementId") String elementId);
    @GET("picture/allSystemElements")
    Call<List<SystemElement>> getAllSystemElements();
    @GET("controlpanel/{id}")
    Call<ControlPanel> getControlPanelById(@Path("id") String id);

    @GET("element/{id}")
    Call<Element> getElementById(@Path("id") String id);

    @GET("picture/id/{id}")
    Call<Picture> getPictureById(@Path("id") String id);

    @GET("picture/all")
    Call<List<Picture>> getAllPictures();
    @GET("picture/favoritesAndAlerts/{arrayIds}")
    Call<List<Picture>> getFavoritesAndAlertPictures(@Path("arrayIds") ArrayList<String> arrayIds);
    @PUT("element/")
    Call<Element> createElement(@Body Element e);

    @GET("/elements")
    Call<List<Element>> getAllElements();

    @GET("element/{title}")
    Call<Element> getElementByTitle(@Path("title") String title);

    @POST("element/{id}")
    Call<Element> updateElement(@Path("id")  String id, @Body Element e);

    @POST("element/state/{id}")
    Call<Element> updateNodesState(@Path("id")  String id, @Body String state);

    @DELETE("element/{id}")
    Call<Void> deleteElement(@Path("id") String id);

    @POST("isConnectedOn/{id}")
    Call<Element> connectElementToControlPanel(@Path("id")  String id, @Body String controlPanelId, String chainNumber);

    @PUT("element/{id}")
    Call<Element> updateNodeElement(@Path("id")  String id, @Body Element e);
    @POST("controlpanel/")
    Call<ControlPanel> createControlPanel(@Body ControlPanel cp);

    @GET("controlpanels/")
    Call<List<ControlPanel>> getAllControlPanels();

    @DELETE("controlpanel/{id}")
    Call<Void> deleteControlPanel(@Path("id") String id);

    @PUT("controlpanel/{id}")
    Call<ControlPanel> updateControlPanel(@Path("id")  String id, @Body ControlPanel cp);
    @GET("picture/allAlertedSortedByState")
    Call<List<Picture>> getAlertedPictures();
    @GET("picture/fromFavoriteList")
    Call<List<Picture>> getFavoritePictures(@Body List<String> arrayIds);


}