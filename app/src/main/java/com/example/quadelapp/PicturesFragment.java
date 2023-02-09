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
import java.util.Map;

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
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PicturesFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        pictures = new ArrayList<>();
        /**initCollapsingToolbar(view);*/

        recyclerView = view.findViewById(R.id.recyclerview_id);
        adapter = new PicturesAdapter(this, pictures);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        getPicturesFromRedis();
        //preparePictures();
        //preparePicturesFromBackend();

        return view;
    }


    private void preparePicturesFromRedis(){
        for (Picture pic : pictures) {
            pic.setImage(getResources().getIdentifier(pic.getTitle(), "drawable", getActivity().getPackageName()));
        }
    }

    private void preparePictures() {
        int covers[] = new int[11];

        for (int index = 0; index < 11; index++) {
            int resourceID = getResources().getIdentifier("album" + index, "drawable", getActivity().getPackageName());
            covers[index] = resourceID;

        }


        Picture p = new Picture("0","Title1", "desc1", covers[0],"OK");
        pictures.add(p);

         p = new Picture("1","Title2", "desc2", covers[1],"ALARM");
        pictures.add(p);

        p = new Picture("2","Title3", "desc3", covers[2],"OFF");
        pictures.add(p);

        p = new Picture("3","Title4", "desc4", covers[3],"OK");
        pictures.add(p);

        p = new Picture("4","Title5", "desc5", covers[10],"ERROR");
        pictures.add(p);

        Log.i(TAG, "Values of talkList: " + pictures);

        adapter.notifyDataSetChanged();
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
                        p.setImage(getResources().getIdentifier("toplanadudara" , "drawable", getActivity().getPackageName()));
                        //p.setImage(getResources().getIdentifier(p.getTitle(), "drawable", getActivity().getPackageName()));

                        pictures.add(p);
                    }
                }
                //spremanje adaptera

                //adapter = new PicturesAdapter(PicturesFragment.this, pictures);
                //recyclerView .setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<List<Picture>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Something went wrong with retrieving PICTURES FROM DB from DB!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshAdapter(){
        if(pictures!=null){
            adapter.pictures = pictures;
            adapter.notifyDataSetChanged();
        }
    }
}