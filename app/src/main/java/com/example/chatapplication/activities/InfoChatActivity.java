package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityInfoChatBinding;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;

public class InfoChatActivity extends AppCompatActivity {

    private ActivityInfoChatBinding binding;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = (User) getIntent().getSerializableExtra("user");

        setListenner();
        setData();
    }

    private void setListenner() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageMore.setOnClickListener(v -> { });

        binding.imageCallAudio.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CallingActivity.class);
            intent.putExtra("type", "audio");
            intent.putExtra("user", user);
            startActivity(intent);
        });

        binding.imageCallVideo.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CallingActivity.class);
            intent.putExtra("type", "video");
            intent.putExtra("user", user);
            startActivity(intent);
        });

        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });
    }

    private void setData() {
        binding.image.setImageBitmap(getBitmapFromEncodedString(user.image));
        binding.name.setText(user.name);
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