package com.example.quadelapp;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quadelapp.Models.SystemElement;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SystemElementsAdapter extends RecyclerView.Adapter<SystemElementsAdapter.MyViewHolder> {

    private final Fragment mContext;
    private final List<SystemElement> systemElements;
    private Map<String, Object> speakersList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description;
        public ImageView cover;
        public ImageView ivState;
        public RelativeLayout rlAlarm;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            cover = view.findViewById(R.id.cover);
            ivState = view.findViewById(R.id.ivState);
            rlAlarm = view.findViewById(R.id.rlAlarm);
        }
    }
    public SystemElementsAdapter(Fragment mContext, List<SystemElement> elements) {
        this.mContext = mContext;
        this.systemElements = elements;
    }
    @Override
    public SystemElementsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_card, parent, false);
        final SystemElementsAdapter.MyViewHolder myViewHolder = new SystemElementsAdapter.MyViewHolder(itemView);
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String elementId = systemElements.get(myViewHolder.getAdapterPosition()).getId();
                String elementType = "d";
                if(Integer.parseInt(elementId)<28){
                    elementType = "cp";
                }
                Intent intent = new Intent(mContext.getContext(), SystemElementDetailsActivity.class);
                intent.putExtra("elementId", elementId);
                intent.putExtra("elementType", elementType );
                mContext.getContext().startActivity(intent);
            }
        });
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final SystemElementsAdapter.MyViewHolder holder, int position) {
        SystemElement systemElement = (SystemElement) systemElements.get(position);
        holder.title.setText(systemElement.getTitle());
        holder.description.setText(systemElement.getDescription());
        holder.cover.setImageResource(systemElement.getElementImage());
        holder.ivState.setImageResource(R.drawable.ic_green_circle);
        setStateImageViewAndRL(holder, systemElement.getState());
        //blinkStateLayout(holder.ivState);
        holder.ivState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showPopupMenu(holder.overflow);
            }
        });
    }


    @Override
    public int getItemCount() {
        return systemElements.size();
    }

    public void setStateImageViewAndRL(MyViewHolder holder, String state){
        if(Objects.equals(state, "OK")) {
            holder.ivState.setImageResource(R.drawable.ic_green_circle);
        } else if (Objects.equals(state, "ALARM")) {
            holder.ivState.setImageResource(R.drawable.ic_red_circle);
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

        } else if (Objects.equals(state, "FAULT")) {
            holder.ivState.setImageResource(R.drawable.ic_yellow_circle);
        } else
            holder.ivState.setImageResource(R.drawable.ic_grey_circle);
    }

    public void blinkStateLayout(RelativeLayout layout){
        ObjectAnimator blinkAnimation = ObjectAnimator.ofArgb(layout, "backgroundColor",mContext.getResources().getColor(R.color.cardview_green_background), Color.WHITE );
        blinkAnimation.setDuration(200);
        blinkAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        blinkAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        blinkAnimation.start();
    }
}
