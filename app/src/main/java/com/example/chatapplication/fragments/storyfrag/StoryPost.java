package com.example.chatapplication.fragments.storyfrag;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityProfileBinding;
import com.example.chatapplication.databinding.ActivityStoryPostBinding;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;

public class StoryPost extends AppCompatActivity {

    private ActivityStoryPostBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        setData();
        setListener();
    }

    private void setData() {
        binding.thinking.setText("Tạo bài viết");
        binding.post.setText("Đăng ");
    }

    private void setListener() {
        binding.imageBackStory.setOnClickListener(v -> onBackPressed());

        binding.addImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.post.setOnClickListener(v -> {
            if (binding.writeYourThinking.getText().toString().isEmpty() && encodedImage == null) {
                showToast("Hãy viết gì đó hoặc chọn ảnh để tạo bài viết mới!");
            } else {
                HashMap<String, Object> post = new HashMap<>();
                post.put(Constants.KEY_POST_ID_AUTHOR, preferenceManager.getString(Constants.KEY_USER_ID));
                post.put(Constants.KEY_POST_NAME_AUTHOR, preferenceManager.getString(Constants.KEY_NAME));
                post.put(Constants.KEY_POST_IMAGE_AUTHOR, preferenceManager.getString(Constants.KEY_IMAGE));
                post.put(Constants.KEY_POST_EMAIL_AUTHOR, preferenceManager.getString(Constants.KEY_EMAIL));
                post.put(Constants.KEY_POST_TIMESTAMP, new Date());
                post.put(Constants.KEY_POST_TEXT, binding.writeYourThinking.getText().toString());
                post.put(Constants.KEY_POST_IMAGE, encodedImage);

                database.collection(Constants.KEY_COLLECTION_POSTS).add(post).addOnSuccessListener(documentReference -> {
                    showToast("Bài viết đã được đăng lên trang cá nhân");
                    onBackPressed();
                }).addOnFailureListener(exception -> {});
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 1000;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitMap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitMap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageBackgroundCardView.setVisibility(View.VISIBLE);
                            binding.image.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

}