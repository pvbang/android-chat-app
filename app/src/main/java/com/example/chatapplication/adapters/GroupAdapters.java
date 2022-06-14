package com.example.chatapplication.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.activities.ChatGroupActivity;
import com.example.chatapplication.databinding.ItemContainerRecentGroupBinding;
import com.example.chatapplication.databinding.ItemContainerUserRequestBinding;
import com.example.chatapplication.listeners.GroupListener;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.Group;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GroupAdapters extends RecyclerView.Adapter<GroupAdapters.UserViewHolder>{

    private final List<Group> groupsList;
    private final GroupListener groupListener;

    public GroupAdapters(List<Group> groupsList, GroupListener groupListener) {
        this.groupsList = groupsList;
        this.groupListener = groupListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerRecentGroupBinding itemContainerUserBinding = ItemContainerRecentGroupBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );

        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(groupsList.get(position));
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentGroupBinding binding;

        UserViewHolder(ItemContainerRecentGroupBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(Group group) {
            binding.textName.setText("Nhóm: " +group.name);
            binding.imageProfile1.setImageBitmap(getGroupImage(group.image1));
            binding.imageProfile2.setImageBitmap(getGroupImage(group.image2));
            binding.textRecentMessage.setText(group.message);
            binding.textDateTime.setText(" · " +group.time);

            binding.getRoot().setOnClickListener(v -> groupListener.onGroupClicked(group));
        }
    }

    private Bitmap getGroupImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
