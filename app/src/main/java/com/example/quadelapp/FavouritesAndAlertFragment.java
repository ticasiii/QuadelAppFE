package com.example.quadelapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.quadelapp.Models.Picture;
import com.example.quadelapp.services.RedisService;

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

public class FavouritesAndAlertFragment extends Fragment{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private RedisService redisService;
    public static FavouritesAndAlertAdapter adapter;
    public static  List<Picture> fullListPictures;
    public  List<Picture> filteredListPictures;
    public  List<Picture> queryListPictures;
    public static boolean isQueryActive;
    private static final String TAG = MainActivity.class.getSimpleName();

    public FavouritesAndAlertFragment() {
        // Required empty public constructor
    }
    public static FavouritesAndAlertFragment newInstance(String param1, String param2) {
        FavouritesAndAlertFragment fragment = new FavouritesAndAlertFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favourites_and_alert, container, false);

        fullListPictures = new ArrayList<>();
        filteredListPictures = new ArrayList<>();
        queryListPictures = new ArrayList<>();
        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.recyclerview_id);
        adapter = new FavouritesAndAlertAdapter(this, fullListPictures);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);
        return view;
    }
    @Override
    public void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        //searchView.setOnQueryTextListener(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()>2){
                    getFilteredDataFromRedis(newText);
                }
                else {
                    fullListPictures.clear();
                    fullListPictures.addAll(queryListPictures);
                }
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                isQueryActive = false;
                adapter.notifyDataSetChanged();
                return true; // Return true to collapse action view
            }
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isQueryActive = true;
                adapter.notifyDataSetChanged();
                // Do something when expanded
                return true; // Return true to expand action view
            }
        });
    }

    private void getFilteredDataFromRedis(String queryText){
        Call<List<Picture>> call = redisService.getFilteredPictures(queryText);
        ArrayList<Picture> pictures = new ArrayList<Picture>();

        call.enqueue(new Callback<List<Picture>>() {
            @Override
            public void onResponse(@NonNull Call<List<Picture>> call, @NonNull Response<List<Picture>> response) {
                fullListPictures.clear();
                if (response.body() != null) {
                    for(Picture p: response.body()) {
                        changeFromCodeToWordState(p);
                        //p.setImage(getResources().getIdentifier("toplanadudara" , "drawable", getActivity().getPackageName()));
                        p.setImage(getResources().getIdentifier("toplana"+p.getTitle().toLowerCase(Locale.ROOT), "drawable", getActivity().getPackageName()));
                        pictures.add(p);
                    }
                }
                //spremanje adaptera
                fullListPictures.addAll(pictures);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(@NonNull Call<List<Picture>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Something went wrong with retrieving PICTURES based on FULLTEXT SEARCH FROM DB!", Toast.LENGTH_SHORT).show();
            }
        });
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
}