package com.example.quadelapp;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quadelapp.Models.Picture;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.MyViewHolder> {

    private Fragment mContext;
    public List<Picture> pictures;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description, state;
        public ImageView cover, overflow, ivState;
        public RelativeLayout stateColor, relative_layout_state, rlAlarm;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            cover = view.findViewById(R.id.cover);
            overflow = view.findViewById(R.id.overflow);
            stateColor = view.findViewById(R.id.relative_layout);
            state = view.findViewById(R.id.state);
            relative_layout_state = view.findViewById(R.id.relative_layout_state);
            ivState = view.findViewById(R.id.ivState);
            rlAlarm = view.findViewById(R.id.rlAlarm);
        }
    }
    public PicturesAdapter(Fragment mContext, List<Picture> pictures) {
        this.mContext = mContext;
        this.pictures = pictures;
    }

    @Override
    public PicturesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.picture_card, parent, false);
        final PicturesAdapter.MyViewHolder myViewHolder = new PicturesAdapter.MyViewHolder(itemView);
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pictureId = pictures.get(myViewHolder.getAdapterPosition()).getId();
                Intent intent = new Intent(mContext.getContext(), PictureDetailsActivity.class);
                intent.putExtra("pictureId", pictureId);
                intent.putExtra("activityId", "picAdapter");
                mContext.getContext().startActivity(intent);
            }
        });
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final PicturesAdapter.MyViewHolder holder, int position) {
        Picture picture = pictures.get(position);
        holder.title.setText(picture.getTitle());
        holder.description.setText(picture.getDescription());
        holder.cover.setImageResource(picture.getImage());
        setStateIcon(holder.ivState, picture.getState());
        setIfFavouritedFromPreferences(picture);
        setFavouriteIcon(holder.overflow, picture.isFavourite());
        holder.state.setText(picture.getState());
        holder.state.setTextColor(Color.BLACK);
        setAlarmFrame(holder, picture.getState());


//        if(picture.getState() == "ALARM"){
//            holder.ivState.setBackgroundColor(0xFFFF0000);
//
//            // adding the color to be shown
//            ObjectAnimator animator = ObjectAnimator.ofInt(holder.ivState, "backgroundColor", Color.RED, Color.WHITE, Color.RED);
//
//            // duration of one color
//            animator.setDuration(2000);
//            animator.setEvaluator(new ArgbEvaluator());
//
//            // color will be show in reverse manner
//            animator.setRepeatCount(Animation.REVERSE);
//
//            // It will be repeated up to infinite time
//            animator.setRepeatCount(Animation.INFINITE);
//            animator.start();
//
//
//        }
//        else if(picture.getState() == "OFF"){
//            holder.ivState.setBackgroundColor(0xFF808080);
//        }
//        else if(picture.getState() == "ERROR"){
//            holder.ivState.setBackgroundColor(0xFFA020F0);
//        }
//        else {
//            holder.ivState.setBackgroundColor(Color.GREEN);
//        }

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

    private void setAlarmFrame(MyViewHolder holder, String state){
        if(Objects.equals(state, "ALARM")){
            holder.state.setTextColor(Color.RED);
            holder.rlAlarm.setBackgroundColor(Color.RED);
            ObjectAnimator animator = ObjectAnimator.ofInt(holder.rlAlarm, "backgroundColor", Color.RED, Color.WHITE, Color.RED);

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

    private void setFavouriteIcon(ImageView iv, boolean isFavourite){
        if(!isFavourite){
            iv.setImageResource(R.drawable.icon_add_to_favourites40);
        }
        else
            iv.setImageResource(R.drawable.icon_favourited40);
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

    private void setIfFavouritedFromPreferences(Picture pic){
        SharedPreferences favorites = mContext.getActivity().getSharedPreferences("favorites",Context.MODE_PRIVATE);
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

    @Override
    public int getItemCount() {
        return pictures.size();
    }
}
