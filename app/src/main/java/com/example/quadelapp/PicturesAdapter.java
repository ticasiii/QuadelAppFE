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

public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.MyViewHolder> {

    private Fragment mContext;
    public List<Picture> pictures;
   // private Map<String, Object> speakersList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description, state;
        public ImageView cover, overflow;
        public RelativeLayout stateColor, relative_layout_state;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            cover = view.findViewById(R.id.cover);
            overflow = view.findViewById(R.id.overflow);
            stateColor = view.findViewById(R.id.relative_layout);
            state = view.findViewById(R.id.state);
            relative_layout_state = view.findViewById(R.id.relative_layout_state);
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
        setIfFavouritedFromPreferences(picture);



        if(!picture.isFavourite()){
            holder.overflow.setImageResource(R.drawable.icon_add_to_favourites40);
        }
        else
            holder.overflow.setImageResource(R.drawable.icon_favourited40);

        holder.state.setText(picture.getState());


        if(picture.getState() == "ALARM"){
            holder.relative_layout_state.setBackgroundColor(0xFFFF0000);

            // adding the color to be shown
            ObjectAnimator animator = ObjectAnimator.ofInt(holder.relative_layout_state, "backgroundColor", Color.RED, Color.WHITE, Color.RED);

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
            holder.relative_layout_state.setBackgroundColor(0xFF808080);
        }
        else if(picture.getState() == "ERROR"){
            holder.relative_layout_state.setBackgroundColor(0xFFA020F0);
        }
        else {
            holder.relative_layout_state.setBackgroundColor(Color.GREEN);
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

    /**
     * Showing popup menu when tapping on 3 dots
     */
/*    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.picture_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PicturesAdapter.MyMenuItemClickListener());
        popup.show();
    }*/

    /**
     * Click listener for popup menu items
     */
/*    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    Toast.makeText(mContext.getContext(), "Add to favourite", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_play_next:
                    Toast.makeText(mContext.getContext(), "Play next", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }*/

    @Override
    public int getItemCount() {
        return pictures.size();
    }
}
