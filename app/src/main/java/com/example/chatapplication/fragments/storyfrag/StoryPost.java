package com.example.chatapplication.fragments.storyfrag;

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

public class StoryPost extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageBackStory, imageUser;
    private TextView thinking, post, username;
    private EditText writeYourThinking;
    private PreferenceManager preferenceManager;

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


        //On CLick;
        post.setOnClickListener(this::onClick);
        imageBackStory.setOnClickListener(this);

        loadInfo();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.post:
                Toast.makeText(this, "Đăng thành công", Toast.LENGTH_SHORT).show();
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
}