package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityInfoChatBinding;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoChatActivity extends AppCompatActivity {

    private ActivityInfoChatBinding binding;
    private User user;
    private String myID, myName, myImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = (User) getIntent().getSerializableExtra("user");
        myID = getIntent().getStringExtra("myID");
        myName = getIntent().getStringExtra("myName");
        myImage = getIntent().getStringExtra("myImage");

        setListenner();
        setData();
        setStatusBarColor();
    }

    private void setStatusBarColor() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
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
            intent.putExtra("you", "0");
            intent.putExtra("myID", myID);
            intent.putExtra("myName", myName);
            intent.putExtra("myImage", myImage);
            startActivity(intent);
        });

        binding.imageMore.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(InfoChatActivity.this, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_chat_info, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.action_delete_chat:
                        return true;
                    default:
                        return false;
                }
            });

            popupMenu.show();
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