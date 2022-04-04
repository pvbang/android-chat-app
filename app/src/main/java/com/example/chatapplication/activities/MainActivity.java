package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.fragments.friendsfrag.FriendsFragment;
import com.example.chatapplication.fragments.mainfrag.MainFragment;
import com.example.chatapplication.fragments.storyfrag.StoryFragment;
import com.example.chatapplication.fragments.userfrag.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends BaseActivity  {

    private ActivityMainBinding binding;

    BottomNavigationView bottomNavigationView;
    MainFragment mainFragment = new MainFragment();
    FriendsFragment friendsFragment = new FriendsFragment();
    StoryFragment storyFragment = new StoryFragment();
    UserFragment userFragment = new UserFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView = findViewById(R.id.bottom_nav);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();
                        return true;
                    case R.id.action_friends:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, friendsFragment).commit();
                        return true;
                    case R.id.action_story:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, storyFragment).commit();
                        return true;
                    case R.id.action_user:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, userFragment).commit();
                        return true;
                }

                return false;
            }
        });

    }


}