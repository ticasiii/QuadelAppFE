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
import java.util.List;
import java.util.Locale;

public class FavouritesAndAlertAdapter extends RecyclerView.Adapter<FavouritesAndAlertAdapter.MyViewHolder> {

    private final Fragment mContext;
    private List<Picture> favoritesPictures;
    public List<Picture> pictures;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description, state;
        public ImageView cover, overflow;
        public RelativeLayout stateColor;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            cover = view.findViewById(R.id.cover);
            overflow = view.findViewById(R.id.overflow);
            stateColor = view.findViewById(R.id.statecolor);
            state = view.findViewById(R.id.state);
        }
    }
    public FavouritesAndAlertAdapter(Fragment mContext, List<Picture> pics) {
        this.mContext = mContext;
        this.pictures = pics;
        notifyDataSetChanged();
        //this.fullListPictures = pics;
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
                //ovde ubaciti gde da ide dalje
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

        setIfFavouritedFromPreferences(picture);



        if(!picture.isFavourite()){
            holder.overflow.setImageResource(R.drawable.icon_add_to_favourites40);
        }
        else
            holder.overflow.setImageResource(R.drawable.icon_favourited40);


        holder.state.setText(picture.getState());


        if(picture.getState() == "ALARM"){
            holder.stateColor.setBackgroundColor(0xFFFF0000);

            // adding the color to be shown
            ObjectAnimator animator = ObjectAnimator.ofInt(holder.stateColor, "backgroundColor", Color.RED, Color.WHITE, Color.RED);

            // duration of one color
            animator.setDuration(2000);
            animator.setEvaluator(new ArgbEvaluator());

            // color will be show in reverse manner
            animator.setRepeatCount(Animation.REVERSE);

            // It will be repeated up to infinite time
            animator.setRepeatCount(Animation.INFINITE);
            animator.start();


        }
        else if(picture.getState() == "OFF"){
            holder.stateColor.setBackgroundColor(0xFF808080);
        }
        else if(picture.getState() == "ERROR"){
            holder.stateColor.setBackgroundColor(0xFFA020F0);
        }
        else {
            holder.stateColor.setBackgroundColor(Color.GREEN);
        }


        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showPopupMenu(holder.overflow);
                if(!picture.isFavourite()){
                    addPictureToFavourites(picture);
                    FavouritesAndAlertFragment.fullListPictures.add(picture);

                    //FavouritesAndAlertFragment.adapter.notifyItemInserted(FavouritesAndAlertFragment.fullListPictures.size()-1);
                    holder.overflow.setImageResource(R.drawable.icon_favourited40);

                }
                else{
                    removePictureFromFavourites(picture);
                    FavouritesAndAlertFragment.fullListPictures.remove(picture);

                    //FavouritesAndAlertFragment.adapter.notifyDataSetChanged();
                    holder.overflow.setImageResource(R.drawable.icon_add_to_favourites40);
                }

            }
        });


//        holder.overflow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showPopupMenu(holder.overflow);
//            }
//        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.favourites_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new FavouritesAndAlertAdapter.MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    Toast.makeText(mContext.getContext(), "Remove from favourite", Toast.LENGTH_SHORT).show();
                    return true;
                /*case R.id.action_play_next:
                    Toast.makeText(mContext.getContext(), "Play next", Toast.LENGTH_SHORT).show();
                    return true;*/
                default:
            }
            return false;
        }
    }
    @Override
    public int getItemCount() {
        return pictures.size();
    }

    public void filterInAndroid(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        List<Picture> fullListPictures = new ArrayList<>();
        fullListPictures.addAll(pictures);
        pictures.clear();
        if (charText.length() < 2) {
            pictures.addAll(fullListPictures);
        } else {
            String filterPattern = charText.toLowerCase().trim();

            for (Picture pic : fullListPictures) {
                if (pic.getTitle().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                    pictures.add(pic);
                }
            }
        }

        notifyDataSetChanged();
        //pictures.addAll(fullListPictures);

    }

    public void getFilteredDataFromRedis(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());

        //pictures.addAll(getFilteredPicturesFromBackend(charText));

        notifyDataSetChanged();
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
