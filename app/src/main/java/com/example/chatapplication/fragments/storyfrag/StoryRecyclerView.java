package com.example.chatapplication.fragments.storyfrag;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;

import java.util.ArrayList;

public class StoryRecyclerView extends RecyclerView.Adapter<StoryRecyclerView.ItemHolder> {

    private ArrayList<StoryItem> storyItems;

    public StoryRecyclerView(ArrayList<StoryItem> storyItems){
        this.storyItems = storyItems;
    }

    @NonNull
    @Override
    public StoryRecyclerView.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryRecyclerView.ItemHolder holder, int position) {
        StoryItem storyItem = storyItems.get(position);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(storyItem.getProfileImage(), 0, storyItem.getProfileImage().length);
//        holder.imageUser.setImageBitmap(bitmap);
        holder.username.setText(storyItem.getUsername());
        holder.dateTime.setText(storyItem.getDate() + " " + storyItem.getTime());
        holder.tvStatus.setText(storyItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return storyItems.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ImageView imageUser;
        TextView username, dateTime, tvStatus;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            imageUser = itemView.findViewById(R.id.imageUser);
            username = itemView.findViewById(R.id.username);
            dateTime = itemView.findViewById(R.id.dateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
