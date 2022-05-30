package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityProfileBinding;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private User user;
    private String myID, myName, myImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = (User) getIntent().getSerializableExtra("user");
        myID = getIntent().getStringExtra("myID");
        myName = getIntent().getStringExtra("myName");
        myImage = getIntent().getStringExtra("myImage");

        if (user != null) {
            setData();
        }

        setListenner();

        setStatusBarColor();
    }

    private void setListenner() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        binding.btnAdd.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            HashMap<String, Object> userFriend = new HashMap<>();
            userFriend.put(Constants.KEY_USER_ID, user.id);
            userFriend.put(Constants.KEY_NAME, user.name);
            userFriend.put(Constants.KEY_IMAGE, user.image);

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).add(userFriend).addOnSuccessListener(documentReference -> {

            }).addOnFailureListener(exception -> {
                showToast(exception.getMessage());
            });

            HashMap<String, Object> myUser = new HashMap<>();
            myUser.put(Constants.KEY_USER_ID, myID);
            myUser.put(Constants.KEY_NAME, myName);
            myUser.put(Constants.KEY_IMAGE, myImage);

            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).add(myUser).addOnSuccessListener(documentReference -> {
                showToast("Đã gửi lời mời kết bạn đến " +user.name);
            }).addOnFailureListener(exception -> {
                showToast(exception.getMessage());
            });

        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setData() {
        binding.image.setImageBitmap(getBitmapFromEncodedString(user.image));
        binding.name.setText(user.name);

        if (!getIntent().getStringExtra("you").isEmpty()) {
            switch (getIntent().getStringExtra("you")) {
                case "1":
                    binding.btnAdd.setVisibility(View.GONE);
                    binding.btnMessage.setVisibility(View.GONE);
                    break;
                case "2":
                    binding.btnAdd.setVisibility(View.GONE);
                    binding.btnDeleteAdd.setVisibility(View.VISIBLE);
                    break;
                case "3":
                    binding.btnAdd.setVisibility(View.GONE);
                    binding.btnMessage.setVisibility(View.GONE);
                    binding.btnMessageFriend.setVisibility(View.VISIBLE);
                    break;
                case "0":
                    break;
                default:
            }
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