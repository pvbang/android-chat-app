package com.example.chatapplication.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityInfoChatBinding;
import com.example.chatapplication.databinding.ActivityInfoChatGroupBinding;
import com.example.chatapplication.models.Group;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;

public class InfoChatGroupActivity extends AppCompatActivity {

    private ActivityInfoChatGroupBinding binding;
    private Group group;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoChatGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        group = (Group) getIntent().getSerializableExtra(Constants.KEY_GROUP);

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

        });

        binding.imageCallVideo.setOnClickListener(v -> {

        });

        binding.imageAddMember.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), GroupSearchActivity.class);
            startActivity(intent);
        });

        binding.imageMore.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(InfoChatGroupActivity.this, view);
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
        binding.imageProfile1.setImageBitmap(getBitmapFromEncodedString(group.image1));
        binding.name.setText(group.name);
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