package com.example.quadelapp;

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

import com.example.quadelapp.Models.ControlPanel;
import com.example.quadelapp.Models.SystemElement;
import com.example.quadelapp.Models.Element;
import com.example.quadelapp.Models.Picture;
import com.example.quadelapp.services.RedisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SystemElementsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private RedisService redisService;
    public static SystemElementsAdapter adapter;
    public static List<SystemElement> elements;
    private static final String TAG = MainActivity.class.getSimpleName();
    public SystemElementsFragment() {
        // Required empty public constructor
    }
    public static SystemElementsFragment newInstance(String param1, String param2) {
        SystemElementsFragment fragment = new SystemElementsFragment();
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
        View view = inflater.inflate(R.layout.fragment_system_elements, container, false);
        elements = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerview_id);
        adapter = new SystemElementsAdapter(this, elements);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        redisService = retrofit.create(RedisService.class);
        getSystemElementsFromRedis();
        return view;
    }

    @Override
    public void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
    }
    private void getSystemElementsFromRedis(){
        Call<List<SystemElement>> call = redisService.getAllSystemElements();
        ArrayList<SystemElement> elementsFromResponse = new ArrayList<SystemElement>();
        call.enqueue(new Callback<List<SystemElement>>() {
            @Override
            public void onResponse(@NonNull Call<List<SystemElement>> call, @NonNull Response<List<SystemElement>> response) {
                elementsFromResponse.clear();
                if (response.body() != null) {
                    for(SystemElement se: response.body()) {
                        changeFromCodeToWordState(se);
                        se.setElementImage(getResources().getIdentifier("toplanadudara" , "drawable", getActivity().getPackageName()));
                        //p.setImage(getResources().getIdentifier(p.getTitle(), "drawable", getActivity().getPackageName()));
                        elementsFromResponse.add(se);
                    }
                }
                elements.addAll(elementsFromResponse);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(@NonNull Call<List<SystemElement>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Something went wrong with retrieving PICTURES FROM DB from DB!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void changeFromCodeToWordState(SystemElement se) {
        if (Objects.equals(se.getState(), "4")) {
            se.setState("ALARM");
        } else if (Objects.equals(se.getState(), "3")) {
            se.setState("FAULT");
        } else if (Objects.equals(se.getState(), "2")) {
            se.setState("OFF");
        } else if (Objects.equals(se.getState(), "1")) {
            se.setState("OK");
        }
    }
}