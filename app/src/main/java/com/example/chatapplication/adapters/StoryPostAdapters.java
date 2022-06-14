package com.example.chatapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.databinding.ItemContainerStoryPostBinding;
import com.example.chatapplication.databinding.ItemContainerUserBinding;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.PostStory;
import com.example.chatapplication.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StoryPostAdapters extends RecyclerView.Adapter<StoryPostAdapters.PostViewHolder>{

    private final List<PostStory> postStoryList;

    public StoryPostAdapters(List<PostStory> postStoryList) {
        this.postStoryList = postStoryList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerStoryPostBinding itemContainerStoryPostBinding = ItemContainerStoryPostBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );

        return new PostViewHolder(itemContainerStoryPostBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.setPostData(postStoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return postStoryList.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        ItemContainerStoryPostBinding binding;

        PostViewHolder(ItemContainerStoryPostBinding itemContainerStoryPostBinding) {
            super(itemContainerStoryPostBinding.getRoot());
            binding = itemContainerStoryPostBinding;
        }

        void setPostData(PostStory postData) {
            binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(postData.imageAuthor));
            binding.textName.setText(postData.nameAuthor);
            binding.textDateTime.setText(getReadableDateTime(postData.dateObject));

            if (postData.text.isEmpty()) {
                binding.textWrite.setVisibility(View.GONE);
            } else {
                binding.textWrite.setVisibility(View.VISIBLE);
                binding.textWrite.setText(postData.text);
            }

            if (postData.image == null) {
                binding.imageBackgroundCardView.setVisibility(View.GONE);
            } else {
                binding.imageBackgroundCardView.setVisibility(View.VISIBLE);
                binding.image.setImageBitmap(getBitmapFromEncodedString(postData.image));
            }


        }
    }


    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

}
