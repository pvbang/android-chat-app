package com.example.chatapplication.activities;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.fragments.friendsfrag.FriendsFragment;
import com.example.chatapplication.fragments.mainfrag.MainFragment;
import com.example.chatapplication.fragments.storyfrag.StoryFragment;
import com.example.chatapplication.fragments.userfrag.UserFragment;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity  {

    private ActivityMainBinding binding;

    BottomNavigationView bottomNavigationView;
    MainFragment mainFragment = new MainFragment();
    FriendsFragment friendsFragment = new FriendsFragment();
    StoryFragment storyFragment = new StoryFragment();
    UserFragment userFragment = new UserFragment();

    private PreferenceManager preferenceManager;
    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView = findViewById(R.id.bottom_nav);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(item -> {
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
        });

        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SPLASH)) {
            new Handler().postDelayed(() -> {
                preferenceManager.putBoolean(Constants.KEY_IS_SPLASH, false);
            }, 1000);
        }

        checkForBatteryOptimizations();

    }

    private void checkForBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Cảnh báo");
                builder.setMessage("Tắt tối ưu hóa pin trong ứng dụng để không làm gián đoạn các dịch vụ nền đang chạy.");
                builder.setPositiveButton("Tắt", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    activityResultLaunch.launch(intent);
                });
                builder.setNegativeButton("Không tắt", (dialogInterface, i) -> dialogInterface.dismiss());
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
            checkForBatteryOptimizations();
        }
    }

    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
            checkForBatteryOptimizations();
        }
    });

}