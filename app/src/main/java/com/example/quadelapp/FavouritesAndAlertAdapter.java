package com.example.quadelapp;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quadelapp.Models.Picture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FavouritesAndAlertAdapter extends RecyclerView.Adapter<FavouritesAndAlertAdapter.MyViewHolder> {
    private final Fragment mContext;
    private List<Picture> favoritesPictures;
    public List<Picture> pictures;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description, state;
        public ImageView cover, overflow, ivState;
        public RelativeLayout stateColor, rlAlarm;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            cover = view.findViewById(R.id.cover);
            overflow = view.findViewById(R.id.overflow);
            stateColor = view.findViewById(R.id.statecolor);
            state = view.findViewById(R.id.state);
            ivState = view.findViewById(R.id.ivState);
            rlAlarm = view.findViewById(R.id.rlAlarm);
        }
    }
    public FavouritesAndAlertAdapter(Fragment mContext, List<Picture> pics) {
        this.mContext = mContext;
        this.pictures = pics;
        notifyDataSetChanged();
    }
    @Override
    public FavouritesAndAlertAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favourites_and_alert_card, parent, false);
        final FavouritesAndAlertAdapter.MyViewHolder myViewHolder = new MyViewHolder(itemView);
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pictureId = pictures.get(myViewHolder.getAdapterPosition()).getId();
                Intent intent = new Intent(mContext.getContext(), PictureDetailsActivity.class);
                intent.putExtra("pictureId", pictureId);
                intent.putExtra("activityId", "favAdapter");
                mContext.getContext().startActivity(intent);
            }
        });
        return myViewHolder;
    }
    @Override
    public void onBindViewHolder(final FavouritesAndAlertAdapter.MyViewHolder holder, int position) {
        Picture picture = pictures.get(position);
        holder.title.setText(picture.getTitle());
        holder.description.setText(picture.getDescription());
        holder.cover.setImageResource(picture.getImage());
        holder.state.setText(picture.getState());
        setIfFavouritedFromPreferences(picture);
        setFavouriteIcon(holder.overflow, picture.isFavourite());
        setAlarmFrame(holder.rlAlarm, picture.getState());
        setStateIcon(holder.ivState, picture.getState());


        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showPopupMenu(holder.overflow);
                if(!picture.isFavourite()){
                    addPictureToFavourites(picture);
                    FavouritesAndAlertFragment.fullListPictures.add(picture);
                    holder.overflow.setImageResource(R.drawable.icon_favourited40);

                }
                else{
                    removePictureFromFavourites(picture);
                    FavouritesAndAlertFragment.fullListPictures.remove(picture);
                    holder.overflow.setImageResource(R.drawable.icon_add_to_favourites40);
                }
            }
        });
    }

    public void setAlarmFrame(RelativeLayout rlAlarm, String state){
        if(Objects.equals(state, "ALARM")){
            rlAlarm.setBackgroundColor(Color.RED);
            ObjectAnimator animator = ObjectAnimator.ofInt(rlAlarm, "backgroundColor", Color.RED, Color.WHITE, Color.RED);

            // duration of one color
            animator.setDuration(500);
            animator.setEvaluator(new ArgbEvaluator());

            // color will be show in reverse manner
            animator.setRepeatCount(Animation.REVERSE);

            // It will be repeated up to infinite time
            animator.setRepeatCount(Animation.INFINITE);
            animator.start();
        }
    }

    public void setStateIcon(ImageView iv, String state){
        if(Objects.equals(state, "ALARM")) {
            iv.setImageResource(R.drawable.ic_red_circle);
        } else if (Objects.equals(state, "FAULT")) {
            iv.setImageResource(R.drawable.ic_yellow_circle);
        } else if (Objects.equals(state, "OFF")) {
            iv.setImageResource(R.drawable.ic_grey_circle);
        } else if (Objects.equals(state, "OK")) {
            iv.setImageResource(R.drawable.ic_green_circle);
        }
    }

    private void setFavouriteIcon(ImageView iv, boolean isFavourite){
        if(!isFavourite){
            iv.setImageResource(R.drawable.icon_add_to_favourites40);
        }
        else
            iv.setImageResource(R.drawable.icon_favourited40);
    }
    @Override
    public int getItemCount() {
        return pictures.size();
    }

    private void setIfFavouritedFromPreferences(Picture pic){
        SharedPreferences favorites = mContext.getActivity().getSharedPreferences("favorites", Context.MODE_PRIVATE);
        pic.setFavourite(favorites.getBoolean(pic.getId(), false));
    }

    private void addPictureToFavourites(Picture pic){
        switchFavoriteState(pic);
        //FavouritesAndAlertFragment.fullListPictures.add(0, pic);
        savePictureFavouriteStateToPreferences(pic.getId(), pic.isFavourite());
        showToastMessaggeShort("Picture is added to favourites");
    }

    private void savePictureFavouriteStateToPreferences(String pictureId, boolean isFavorite){
        //SharedPreferences sharedPref = mContext.getActivity().getPreferences(mContext.getContext().MODE_PRIVATE);
        SharedPreferences favorites = mContext.getActivity().getSharedPreferences("favorites", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = favorites.edit();
        editor.putBoolean(pictureId, isFavorite);
        editor.apply();
    }

    private void removePictureFromFavourites(Picture pic){
        switchFavoriteState(pic);
        //FavouritesAndAlertFragment.fullListPictures.remove(pic);
        removePictureFavouriteStateFromPreferences(pic.getId());
        showToastMessaggeShort("Picture is removed from favourites");
    }
    private void removePictureFavouriteStateFromPreferences(String pictureId){
        //SharedPreferences sharedPref = mContext.getActivity().getPreferences(mContext.getContext().MODE_PRIVATE);
        SharedPreferences favorites = mContext.getActivity().getSharedPreferences("favorites", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = favorites.edit();
        editor.remove(pictureId);
        editor.commit();
    }
    private void showToastMessaggeShort(String messagge){
        Toast.makeText(mContext.getContext(), messagge, Toast.LENGTH_SHORT).show();
    }
    private void switchFavoriteState(Picture picture){
        if(picture.isFavourite()) {
            picture.setFavourite(false);
        }
        else picture.setFavourite(true);
    }

}
