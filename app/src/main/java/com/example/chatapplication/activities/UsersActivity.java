package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatapplication.R;
import com.example.chatapplication.adapters.UserAdapters;
import com.example.chatapplication.databinding.ActivityUsersBinding;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(task -> {
           loading(false);
           String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
           if (task.isSuccessful() && task.getResult() != null) {
               List<User> usersList = new ArrayList<>();
               for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                   if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                       continue;
                   }
                   User user = new User();
                   user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                   user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                   user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                   user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                   user.id = queryDocumentSnapshot.getId();
                   usersList.add(user);
               }
               if (usersList.size() > 0) {
                   UserAdapters userAdapters = new UserAdapters(usersList, this);
                   binding.usersRecyclerView.setAdapter(userAdapters);
                   binding.usersRecyclerView.setVisibility(View.VISIBLE);
               } else {
                   showErrorMessage();
               }
           } else {
               showErrorMessage();
           }
        });

    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "Chưa có người dùng nào"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}