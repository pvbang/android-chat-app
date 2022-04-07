package com.example.chatapplication.fragments.storyfrag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapplication.R;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EventListener;
import java.util.HashMap;

public class StoryPost extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageBackStory, imageUser;
    private TextView thinking, post, username;
    private EditText writeYourThinking;
    private PreferenceManager preferenceManager;
    private String postDate, postTimeRan, postTimeOff, postRandomName, uid;

    private DatabaseReference userRef, postRef;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_post);

        preferenceManager = new PreferenceManager(getApplicationContext());

        imageBackStory = (ImageView) findViewById(R.id.imageBackStory);
        thinking = (TextView) findViewById(R.id.thinking);
        post = (TextView) findViewById(R.id.post);
        writeYourThinking = (EditText) findViewById(R.id.writeYourThinking);
        imageUser = (ImageView) findViewById(R.id.imageUser);
        username = (TextView) findViewById(R.id.username);

        //Set Text;
        thinking.setText("Tạo bài viết");
        post.setText("Đăng");

        //Hello There

        //On CLick;
        post.setOnClickListener(this::onClick);
        imageBackStory.setOnClickListener(this);



        userRef = FirebaseDatabase.getInstance("https://chat-application-f4d3d-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Users");
        postRef = FirebaseDatabase.getInstance("https://chat-application-f4d3d-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Posts");

        loadInfo();
        storeToFirebase();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.post:
                if (!writeYourThinking.getText().toString().isEmpty()) {
                    savePost();
                }
                break;
            case R.id.imageBackStory:
                onBackPressed();
                break;
        }
    }

    public void loadInfo(){
        username.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageUser.setImageBitmap(bitmap);
    }

    public void storeToFirebase(){
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        postDate = currentDateFormat.format(calendarDate.getTime());

        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm:ss");
        postTimeRan = currentTimeFormat.format(calendarTime.getTime());

        postRandomName = postDate + postTimeRan;

        Calendar calendarTimeOff = Calendar.getInstance();
        SimpleDateFormat currentTimeFormatOff = new SimpleDateFormat("HH:mm");
        postTimeOff = currentTimeFormatOff.format(calendarTimeOff.getTime());

    }

    public void savePost(){
        String uidSave = preferenceManager.getString(Constants.KEY_USER_ID);
        userRef.child(uidSave).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String userName = preferenceManager.getString(Constants.KEY_NAME);
                    String imageProfile = preferenceManager.getString(Constants.KEY_IMAGE);

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", uidSave);
                    postsMap.put("date", postDate);
                    postsMap.put("time", postTimeOff);
                    postsMap.put("description", writeYourThinking.getText().toString());
                    postsMap.put("username", userName);
                    postsMap.put("profileimage", imageProfile);

                    postRef.child(uidSave+postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "Đăng thành công", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        }
                    });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}