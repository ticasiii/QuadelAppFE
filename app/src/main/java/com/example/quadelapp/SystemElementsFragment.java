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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SystemElementsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SystemElementsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private RedisService redisService;
    public static SystemElementsAdapter adapter;

//    private ArrayList  elements;
    public static List<SystemElement> elements;
    private static final String TAG = MainActivity.class.getSimpleName();


    public SystemElementsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ElementsFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_system_elements, container, false);
        elements = new ArrayList<>();


        /**initCollapsingToolbar(view);*/

        recyclerView = view.findViewById(R.id.recyclerview_id);
        adapter = new SystemElementsAdapter(this, elements);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        //prepareElements();
        //preparePicturesFromBackend();

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
                //spremanje adaptera
                elements.addAll(elementsFromResponse);

//                adapter = new FavouritesAndAlertAdapter(FavouritesAndAlertFragment.this, pictures);
//                recyclerView .setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<List<SystemElement>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Something went wrong with retrieving PICTURES FROM DB from DB!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeFromCodeToWordState(SystemElement systemElement){

        switch (systemElement.getState()){
            case "1":
                systemElement.setState("OK");
            case "2":
                systemElement.setState("OFF");
            case "3":
                systemElement.setState("ERROR");
            case "4":
                systemElement.setState("ALARM");
            default:
                systemElement.setState("OK");
        }

    }

    private void prepareElements(){

        ControlPanel cp = new ControlPanel("0","CP1", "desc", "type", "ok");
        elements.add(cp);

        cp = new ControlPanel("1", "CP2","desc", "type", "ok");
        elements.add(cp);

        cp = new ControlPanel("2","CP3","desc", "type", "ok");
        elements.add(cp);

        cp = new ControlPanel("3","CP4","desc", "type", "ok");
        elements.add(cp);

        cp = new ControlPanel("4","CP5","desc", "type", "ok");
        elements.add(cp);

        Element d = new Element("29","D1", "detector is in Toplana Dudara","ok","1","temperature" );
        elements.add(d);

        d = new Element("30","D2", "detector is in Toplana Dudara","ok", "2", "temperature");
        elements.add(d);

        d = new Element("31","D3", "detector is in Toplana Dudara","ok", "1", "temperature");
        elements.add(d);

        d = new Element("32","D4", "detector is in Toplana Dudara","ok","2", "temperature");
        elements.add(d);

        d = new Element("33","D5", "detector is in Toplana Dudara","ok", "1", "temperature");
        elements.add(d);

        Log.i(TAG, "Values of talkList: " + elements);
    }

}