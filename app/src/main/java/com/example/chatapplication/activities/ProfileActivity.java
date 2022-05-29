package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityProfileBinding;
import com.example.chatapplication.models.User;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = (User) getIntent().getSerializableExtra("user");

        setListenner();

        if (user != null) {
            setData();
        }

        setStatusBarColor();
    }

    private void setListenner() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void setData() {
        binding.image.setImageBitmap(getBitmapFromEncodedString(user.image));
        binding.name.setText(user.name);

        if (getIntent().getStringExtra("you").equals("1")) {
            binding.btnAdd.setVisibility(View.GONE);
            binding.btnMessage.setVisibility(View.GONE);
        }
    }

    private void setStatusBarColor() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.background));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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