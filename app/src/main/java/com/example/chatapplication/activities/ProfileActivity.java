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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private User user;
    private String myID, myName, myImage;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();

        user = (User) getIntent().getSerializableExtra("user");
        myID = getIntent().getStringExtra("myID");
        myName = getIntent().getStringExtra("myName");
        myImage = getIntent().getStringExtra("myImage");

        getFriendStatus();

        if (user != null) {
            setData();
        }

        setListenner();

        setStatusBarColor();
    }

    private void getFriendStatus() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                binding.btnAdd.setVisibility(View.GONE);
                binding.btnMessage.setVisibility(View.GONE);

                binding.btnDeleteRequest.setVisibility(View.GONE);
                binding.btnMessageDeleteRequest.setVisibility(View.GONE);

                binding.btnAcceptRequest.setVisibility(View.GONE);
                binding.btnMessageAcceptRequest.setVisibility(View.GONE);

                binding.btnDeleteAdd.setVisibility(View.VISIBLE);
                binding.btnMessageDeleteAdd.setVisibility(View.VISIBLE);
            } else {
                database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                        binding.btnAdd.setVisibility(View.GONE);
                        binding.btnMessage.setVisibility(View.GONE);

                        binding.btnDeleteRequest.setVisibility(View.VISIBLE);
                        binding.btnMessageDeleteRequest.setVisibility(View.VISIBLE);

                        binding.btnAcceptRequest.setVisibility(View.GONE);
                        binding.btnMessageAcceptRequest.setVisibility(View.GONE);

                        binding.btnDeleteAdd.setVisibility(View.GONE);
                        binding.btnMessageDeleteAdd.setVisibility(View.GONE);
                    } else {
                        database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task3 -> {
                            if (task3.isSuccessful() && !task3.getResult().isEmpty()) {
                                binding.btnAdd.setVisibility(View.GONE);
                                binding.btnMessage.setVisibility(View.GONE);

                                binding.btnDeleteRequest.setVisibility(View.GONE);
                                binding.btnMessageDeleteRequest.setVisibility(View.GONE);

                                binding.btnAcceptRequest.setVisibility(View.VISIBLE);
                                binding.btnMessageAcceptRequest.setVisibility(View.VISIBLE);

                                binding.btnDeleteAdd.setVisibility(View.GONE);
                                binding.btnMessageDeleteAdd.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        });
    }

    private void setListenner() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        // gửi lời kết bạn
        binding.btnAdd.setOnClickListener(v -> {
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

            binding.btnAdd.setVisibility(View.GONE);
            binding.btnMessage.setVisibility(View.GONE);

            binding.btnDeleteRequest.setVisibility(View.VISIBLE);
            binding.btnMessageDeleteRequest.setVisibility(View.VISIBLE);

            binding.btnAcceptRequest.setVisibility(View.GONE);
            binding.btnMessageAcceptRequest.setVisibility(View.GONE);

            binding.btnDeleteAdd.setVisibility(View.GONE);
            binding.btnMessageDeleteAdd.setVisibility(View.GONE);
        });

        // chấp nhận lời mời kết bạn
        binding.btnAcceptRequest.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            HashMap<String, Object> userFriend = new HashMap<>();
            userFriend.put(Constants.KEY_USER_ID, user.id);
            userFriend.put(Constants.KEY_NAME, user.name);
            userFriend.put(Constants.KEY_IMAGE, user.image);

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteRequest(queryDocumentSnapshot.getId());
                    }
                }
            });

            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, myID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        hashMyUser(user.id, queryDocumentSnapshot.getString(Constants.KEY_NAME), queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                        deleteWait(user.id, queryDocumentSnapshot.getId());
                    }
                }
            });

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_FRIENDS).add(userFriend).addOnSuccessListener(documentReference -> {
                showToast("Bạn vừa có thêm một người bạn mới ^^");
            }).addOnFailureListener(exception -> {
                showToast(exception.getMessage());
            });

            binding.btnAdd.setVisibility(View.GONE);
            binding.btnMessage.setVisibility(View.GONE);

            binding.btnDeleteRequest.setVisibility(View.GONE);
            binding.btnMessageDeleteRequest.setVisibility(View.GONE);

            binding.btnAcceptRequest.setVisibility(View.GONE);
            binding.btnMessageAcceptRequest.setVisibility(View.GONE);

            binding.btnDeleteAdd.setVisibility(View.VISIBLE);
            binding.btnMessageDeleteAdd.setVisibility(View.VISIBLE);

        });

        // hủy kết bạn
        binding.btnDeleteAdd.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            HashMap<String, Object> userFriend = new HashMap<>();
            userFriend.put(Constants.KEY_USER_ID, user.id);
            userFriend.put(Constants.KEY_NAME, user.name);
            userFriend.put(Constants.KEY_IMAGE, user.image);

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteFriend(myID, queryDocumentSnapshot.getId());
                    }
                }
            });

            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).collection(Constants.KEY_COLLECTION_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, myID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteFriend(user.id, queryDocumentSnapshot.getId());
                        showToast("Bạn vừa hủy kết bạn với " +user.name);
                    }
                }
            });

            binding.btnAdd.setVisibility(View.VISIBLE);
            binding.btnMessage.setVisibility(View.VISIBLE);

            binding.btnDeleteRequest.setVisibility(View.GONE);
            binding.btnMessageDeleteRequest.setVisibility(View.GONE);

            binding.btnAcceptRequest.setVisibility(View.GONE);
            binding.btnMessageAcceptRequest.setVisibility(View.GONE);

            binding.btnDeleteAdd.setVisibility(View.GONE);
            binding.btnMessageDeleteAdd.setVisibility(View.GONE);

        });

        // hủy lời mời kết bạn đi
        binding.btnDeleteRequest.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            HashMap<String, Object> userFriend = new HashMap<>();
            userFriend.put(Constants.KEY_USER_ID, user.id);
            userFriend.put(Constants.KEY_NAME, user.name);
            userFriend.put(Constants.KEY_IMAGE, user.image);

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteWait(myID, queryDocumentSnapshot.getId());
                    }
                }
            });

            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, myID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteRequest(user.id, queryDocumentSnapshot.getId());
                    }
                }
            });

            binding.btnAdd.setVisibility(View.VISIBLE);
            binding.btnMessage.setVisibility(View.VISIBLE);

            binding.btnDeleteRequest.setVisibility(View.GONE);
            binding.btnMessageDeleteRequest.setVisibility(View.GONE);

            binding.btnAcceptRequest.setVisibility(View.GONE);
            binding.btnMessageAcceptRequest.setVisibility(View.GONE);

            binding.btnDeleteAdd.setVisibility(View.GONE);
            binding.btnMessageDeleteAdd.setVisibility(View.GONE);

        });

    }

    private void deleteRequest(String string) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).document(string).delete().addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });
    }

    private void deleteRequest(String id, String string) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(id).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).document(string).delete().addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });
    }

    private void deleteWait(String id, String string) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(id).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).document(string).delete().addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });
    }

    private void deleteFriend(String id, String string) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(id).collection(Constants.KEY_COLLECTION_FRIENDS).document(string).delete().addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });
    }

    private void hashMyUser(String id, String name, String imgage) {
        HashMap<String, Object> myUser = new HashMap<>();
        myUser.put(Constants.KEY_USER_ID, myID);
        myUser.put(Constants.KEY_NAME, name);
        myUser.put(Constants.KEY_IMAGE, imgage);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(id).collection(Constants.KEY_COLLECTION_FRIENDS).add(myUser).addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setData() {
        binding.image.setImageBitmap(getBitmapFromEncodedString(user.image));
        binding.name.setText(user.name);

        if (!getIntent().getStringExtra("you").isEmpty()) {
            if (getIntent().getStringExtra("you").equals("1")) {
                binding.btnAdd.setVisibility(View.GONE);
                binding.btnMessage.setVisibility(View.GONE);

                binding.btnDeleteRequest.setVisibility(View.GONE);
                binding.btnMessageDeleteRequest.setVisibility(View.GONE);

                binding.btnAcceptRequest.setVisibility(View.GONE);
                binding.btnMessageAcceptRequest.setVisibility(View.GONE);

                binding.btnDeleteAdd.setVisibility(View.GONE);
                binding.btnMessageDeleteAdd.setVisibility(View.GONE);
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