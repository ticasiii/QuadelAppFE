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
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavouritesAndAlertFragment#newInstance} factory method to
 * create an instance of this fragment. implements SearchView.OnQueryTextListener 
 */
public class FavouritesAndAlertFragment extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private RedisService redisService;
    public static FavouritesAndAlertAdapter adapter;
    //private Map<String, Object> speakersList;
    public static  List<Picture> fullListPictures;
    public  List<Picture> filteredListPictures;
    public  List<Picture> queryListPictures;


    private List<String> picturesListIds;
    public static boolean isQueryActive;


    private static final String TAG = MainActivity.class.getSimpleName();


    public FavouritesAndAlertFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavouritesAndAlertFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        //TS.CREATE detectorState:elementId:elementType

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

        //getAllPicturesFromRedis();
//        getFavoritedPicturesFromRedis();
//        getAlertedPictures();

        //getFavoritedAndAlertedPictures();


        //preparePictures();
        //getFavouritedPictures();
        //preparePicturesFromBackend();

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




                //adapter.filterInAndroid(query);
                //adapter.filterData(query);
                //getFilteredPicturesFromRedis(query);
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


    private List<Picture> filter(List<Picture> pics, String query) {
        query = query.toLowerCase().trim();

        List<Picture> filteredModelList = new ArrayList<>();
        for (Picture pic : pics) {
            String text = pic.getTitle().toLowerCase().trim();
            if (text.contains(query)) {
                filteredModelList.add(pic);
            }
        }
        return filteredModelList;
    }

    private void preparePicturesFromBackend(){
        //get FAVOURITE PICTURES FROM BACKEND by default
        //1. filtered = favourited
        //2. filtered = based on query
    }
    //getFavoritesAndAlertPictures
    private void getFavoritedAndAlertedPictures(){
        ArrayList<String>arrayIds = getAllTrueKeysFromPreferences();
        Call<List<Picture>> call = redisService.getFavoritesAndAlertPictures(arrayIds);
        ArrayList<Picture> pictures = new ArrayList<Picture>();

        call.enqueue(new Callback<List<Picture>>() {
            @Override
            public void onResponse(@NonNull Call<List<Picture>> call, @NonNull Response<List<Picture>> response) {
                queryListPictures.addAll(fullListPictures);
                fullListPictures.clear();


                if (response.body() != null) {
                    for(Picture p: response.body()) {
                        changeFromCodeToWordState(p);
                        p.setImage(getResources().getIdentifier("toplanadudara" , "drawable", getActivity().getPackageName()));
                        //p.setImage(getResources().getIdentifier(p.getTitle(), "drawable", getActivity().getPackageName()));
                        pictures.add(p);

                    }
                }
                //spremanje adaptera
                fullListPictures.addAll(pictures);

//                adapter = new FavouritesAndAlertAdapter(FavouritesAndAlertFragment.this, pictures);
//                recyclerView .setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<List<Picture>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Something went wrong with retrieving PICTURES FROM DB from DB!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private ArrayList<String> getAllTrueKeysFromPreferences(){
        Map<String, ?> favoriteIdsMap;
        SharedPreferences favoritesIds = getActivity().getSharedPreferences("favorites",Context.MODE_PRIVATE);
        favoriteIdsMap = favoritesIds.getAll();
        ArrayList<String> favoriteIdsList = new ArrayList<String>(favoriteIdsMap.keySet());

        return favoriteIdsList;
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
                        p.setImage(getResources().getIdentifier("toplanadudara" , "drawable", getActivity().getPackageName()));
                        //p.setImage(getResources().getIdentifier(p.getTitle(), "drawable", getActivity().getPackageName()));
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

    private void removePicture(Picture pic){
        fullListPictures.remove(pic);
    }


//    private void getAlertedPictures(){
//        Call<List<Picture>> call = redisService.getAlertedPictures();
//        ArrayList<Picture> pictures = new ArrayList<Picture>();
//
//        call.enqueue(new Callback<List<Picture>>() {
//            @Override
//            public void onResponse(@NonNull Call<List<Picture>> call, @NonNull Response<List<Picture>> response) {
//
//                if (response.body() != null) {
//                    for(Picture p: response.body()) {
//                        changeFromCodeToWordState(p);
//                        p.setImage(getResources().getIdentifier("album1" , "drawable", getActivity().getPackageName()));
//                        pictures.add(p);
//
//                    }
//                }
//                //spremanje adaptera
//                fullListPictures.addAll(pictures);
//
////                adapter = new FavouritesAndAlertAdapter(FavouritesAndAlertFragment.this, pictures);
////                recyclerView .setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<List<Picture>> call, @NonNull Throwable t) {
//                Toast.makeText(getContext(), "Something went wrong with retrieving PICTURES FROM DB from DB!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void getFavoritedPicturesFromRedis(){
//
//        Call<List<Picture>> call = redisService.getAllPictures();
//        ArrayList<Picture> pictures = new ArrayList<Picture>();
//
//        call.enqueue(new Callback<List<Picture>>() {
//            @Override
//            public void onResponse(@NonNull Call<List<Picture>> call, @NonNull Response<List<Picture>> response) {
//
//                if (response.body() != null) {
//                    for(Picture p: response.body()) {
//                        if(isFavouritedInPreferences(p.getId())){
//                            changeFromCodeToWordState(p);
//                            p.setImage(getResources().getIdentifier("album1" , "drawable", getActivity().getPackageName()));
//                            pictures.add(p);
//                        }
//                    }
//                }
//                //spremanje adaptera
//                fullListPictures = pictures;
//
////                adapter = new FavouritesAndAlertAdapter(FavouritesAndAlertFragment.this, pictures);
////                recyclerView .setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<List<Picture>> call, @NonNull Throwable t) {
//                Toast.makeText(getContext(), "Something went wrong with retrieving PICTURES FROM DB from DB!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//
//    private void getAllPicturesFromRedis(){
//
//        Call<List<Picture>> call = redisService.getAllPictures();
//        ArrayList<Picture> pictures = new ArrayList<Picture>();
//
//        call.enqueue(new Callback<List<Picture>>() {
//            @Override
//            public void onResponse(@NonNull Call<List<Picture>> call, @NonNull Response<List<Picture>> response) {
//
//                if (response.body() != null) {
//                    for(Picture p: response.body()) {
//                        changeFromCodeToWordState(p);
//                        //p.setImage(getResources().getIdentifier(p.getTitle(), "drawable", getActivity().getPackageName()));
//                        p.setImage(getResources().getIdentifier("toplanadudara" , "drawable", getActivity().getPackageName()));
//
//                        pictures.add(p);
//                    }
//                }
//                //filteredListPictures = pictures;
//                fullListPictures = pictures;
////                adapter = new FavouritesAndAlertAdapter(FavouritesAndAlertFragment.this, pictures);
////                recyclerView .setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<List<Picture>> call, @NonNull Throwable t) {
//                Toast.makeText(getContext(), "Something went wrong with retrieving PICTURES FROM DB from DB!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

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

//    private boolean isFavouritedInPreferences(String pictureId){
//        SharedPreferences favorites = getActivity().getSharedPreferences("favorites", Context.MODE_PRIVATE);
//        return favorites.getBoolean(pictureId, false);
//    }

    public void refreshAdapter(){
        if(fullListPictures!=null){
            adapter.pictures = fullListPictures;
            adapter.notifyDataSetChanged();
        }
    }


    private void getFilteredPicturesFromRedis(String query){

    }


//    private void preparePictures() {
//        int[] covers = new int[]{
//                R.drawable.album1,
//                R.drawable.album2,
//                R.drawable.album3,
//                R.drawable.album4,
//                R.drawable.album5,
//                R.drawable.album6,
//                R.drawable.album7,
//                R.drawable.album8,
//                R.drawable.album9,
//                R.drawable.album10,
//                R.drawable.album11};
//
//        Picture p = new Picture("0","naziv", "desc", covers[0],"OK");
//        fullListPictures.add(p);
//
//        p = new Picture("1","Title", "desc", covers[1],"ALARM");
//        fullListPictures.add(p);
//
//        p = new Picture("2","Title", "desc", covers[2],"OFF");
//        fullListPictures.add(p);
//
//        p = new Picture("3","Title", "desc", covers[3],"ERROR");
//        fullListPictures.add(p);
//
//        p = new Picture("4","Title", "desc", covers[4],"OK");
//        fullListPictures.add(p);
//
//        Log.i(TAG, "Values of Pictures: " + fullListPictures);
//        filteredListPictures.addAll(fullListPictures);
//
//        adapter.notifyDataSetChanged();
//    }



}