package com.example.quadelapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public static String currentFragment;
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity)
    {
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0) {
            currentFragment = "favs";
            return new FavouritesAndAlertFragment();
        }
        else if(position == 1){
            currentFragment = "pics";
            return new PicturesFragment();
        }
        else{
            currentFragment = "elements";
            return new SystemElementsFragment();
        }
    }
    @Override
    public int getItemCount() {return 3; }
}