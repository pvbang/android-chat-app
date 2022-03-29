package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.databinding.ActivityWaitCallingBinding;

public class WaitCallingActivity extends AppCompatActivity {

    private ActivityWaitCallingBinding binding;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWaitCallingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String nameUser = getIntent().getStringExtra("name");
        binding.nameUser.setText(nameUser);

        binding.imageEndCalling.setOnClickListener(v -> onBackPressed());
        binding.avatarUser.setImageBitmap(getBitmapFromEncodedString(getIntent().getStringExtra("image")));
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