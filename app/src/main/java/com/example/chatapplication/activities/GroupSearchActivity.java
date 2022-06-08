package com.example.chatapplication.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.chatapplication.R;
import com.example.chatapplication.adapters.GroupSearchAdapters;
import com.example.chatapplication.databinding.ActivityGroupSearchBinding;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupSearchActivity extends AppCompatActivity implements UserListener, SwipeRefreshLayout.OnRefreshListener{

    private ActivityGroupSearchBinding binding;
    PreferenceManager preferenceManager;
    private GroupSearchAdapters searchAdapters;
    private String myID, myName, myImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        myID = preferenceManager.getString(Constants.KEY_USER_ID);
        myName = preferenceManager.getString(Constants.KEY_NAME);
        myImage = preferenceManager.getString(Constants.KEY_IMAGE);

        getUsersGroup();

        setListener();

        binding.container.setOnRefreshListener(this::onRefresh);
        binding.container.setColorSchemeColors(getResources().getColor(R.color.color_main));

        setStatusBarColor();

    }

    private void setListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        binding.searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAdapters.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchAdapters.getFilter().filter(newText);
                return false;
            }
        });

        binding.imageVoice.setOnClickListener(v -> {
            speak(v);
        });
    }

    private void speak(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi_VN");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Đọc tên hoặc email người bạn muốn tìm kiếm...");
        someActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    binding.searchView.setQuery(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0), false);
                }
            }
        });

    private void setStatusBarColor() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
    }

    private void getUsersGroup() {
        binding.container.setRefreshing(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(task -> {
            binding.container.setRefreshing(false);
            String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
            if (task.isSuccessful() && task.getResult() != null) {
                List<User> usersList = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                        continue;
                    }
                    User user = new User();
                    user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                    user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                    user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                    user.id = queryDocumentSnapshot.getId();
                    usersList.add(user);
                }
                getFriends(usersList);
            } else {
                showErrorMessage();
            }
        });
    }

    private void getFriends(List<User> usersList) {
        FirebaseFirestore database2 = FirebaseFirestore.getInstance();
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        database2.collection(Constants.KEY_COLLECTION_USERS).document(currentUserId).collection(Constants.KEY_COLLECTION_FRIENDS).get().addOnCompleteListener(task2 -> {
            if (task2.isSuccessful() && task2.getResult() != null) {
                List<User> friendsList = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot2 : task2.getResult()) {
                    User user = new User();
                    user.id = queryDocumentSnapshot2.getString(Constants.KEY_USER_ID);
                    friendsList.add(user);
                }
                setAdapterFriends(usersList, friendsList);
            }
        });
    }

    private void setAdapterFriends(List<User> usersList, List<User> friendsList) {
        List<User> friendsFinal = new ArrayList<>();
        User userr = new User();
        userr.name = "###########!!~~";
        userr.email = "###########!!~~";
        userr.image = "###########!!~~";
        userr.token = "###########!!~~";
        userr.id = "###########!!~~";
        friendsFinal.add(userr);

        if (friendsList.size() > 0) {
            for (User friend : friendsList) {
                for (User user : usersList) {
                    if (user.id.equals(friend.id)) {
                        friendsFinal.add(user);
                        break;
                    }
                }
            }
        }

        binding.container.setRefreshing(false);

        if (friendsFinal.size() > 0) {
            Collections.sort(friendsFinal, (user, t1) -> user.getName().compareToIgnoreCase(t1.getName()));

            searchAdapters = new GroupSearchAdapters(friendsFinal, this);
            binding.searchRecyclerView.setAdapter(searchAdapters);
            binding.searchRecyclerView.setVisibility(View.VISIBLE);

        } else {
            showErrorMessage();
        }
    }


    private void showErrorMessage() {
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        getUsersGroup();
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("you", "0");
        intent.putExtra("myID", myID);
        intent.putExtra("myName", myName);
        intent.putExtra("myImage", myImage);
        startActivity(intent);
    }

}