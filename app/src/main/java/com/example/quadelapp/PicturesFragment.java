package com.example.quadelapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PicturesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PicturesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private RedisService redisService;
    public  static PicturesAdapter adapter;
    private FavouritesAndAlertAdapter adapterFavs;
    public  static List<Picture> pictures;
    private static final String TAG = MainActivity.class.getSimpleName();

    public PicturesFragment() {
        // Required empty public constructor
    }
    public static PicturesFragment newInstance(String param1, String param2) {
        PicturesFragment fragment = new PicturesFragment();
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
    public void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        pictures = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview_id);
        adapter = new PicturesAdapter(this, pictures);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        getPicturesFromRedis();
        return view;
    }
    private void preparePicturesFromRedis(){
        for (Picture pic : pictures) {
            pic.setImage(getResources().getIdentifier(pic.getTitle(), "drawable", getActivity().getPackageName()));
        }
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
    private void setIfFavouritedFromPreferences(Picture pic){
        SharedPreferences favorites = getActivity().getSharedPreferences("favorites", getContext().MODE_PRIVATE);
        pic.setFavourite(favorites.getBoolean(pic.getId(), false));
    }
    private void getPicturesFromRedis(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);
        Call<List<Picture>> call = redisService.getAllPictures();
        call.enqueue(new Callback<List<Picture>>() {
            @Override
            public void onResponse(@NonNull Call<List<Picture>> call, @NonNull Response<List<Picture>> response) {
                if (response.body() != null) {
                    for(Picture p: response.body()) {
                        //p.setImage(getResources().getIdentifier(p.getTitle(), "drawable", getActivity().getPackageName()));
                        setIfFavouritedFromPreferences(p);
                        changeFromCodeToWordState(p);
                        //p.setImage(getResources().getIdentifier("toplanadudara" , "drawable", getActivity().getPackageName()));
                        p.setImage(getResources().getIdentifier("toplana"+p.getTitle().toLowerCase(Locale.ROOT), "drawable", getActivity().getPackageName()));
                        pictures.add(p);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(@NonNull Call<List<Picture>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Something went wrong with retrieving PICTURES FROM DB from DB!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}