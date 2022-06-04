package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.fragments.friendsfrag.FriendsFragment;
import com.example.chatapplication.fragments.mainfrag.MainFragment;
import com.example.chatapplication.fragments.storyfrag.StoryFragment;
import com.example.chatapplication.fragments.userfrag.UserFragment;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends BaseActivity  {

    private ActivityMainBinding binding;

    BottomNavigationView bottomNavigationView;
    MainFragment mainFragment = new MainFragment();
    FriendsFragment friendsFragment = new FriendsFragment();
    StoryFragment storyFragment = new StoryFragment();
    UserFragment userFragment = new UserFragment();

    private FirebaseFirestore database;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView = findViewById(R.id.bottom_nav);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();
                    getFriendRequests();
                    return true;
                case R.id.action_friends:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, friendsFragment).commit();
                    getFriendRequests();
                    return true;
                case R.id.action_story:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, storyFragment).commit();
                    getFriendRequests();
                    return true;
                case R.id.action_user:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, userFragment).commit();
                    getFriendRequests();
                    return true;
            }
            return false;
        });

        getFriendRequests();

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getFriendRequests() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                int countRequests = 0;
                for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                    countRequests++;
                }
                if (countRequests != 0) {
                    bottomNavigationView.getOrCreateBadge(R.id.action_friends).setNumber(countRequests);
                }
            } else {
                bottomNavigationView.removeBadge(R.id.action_friends);
            }
        });
    }
}