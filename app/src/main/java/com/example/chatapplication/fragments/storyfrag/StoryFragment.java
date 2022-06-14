package com.example.chatapplication.fragments.storyfrag;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.R;
import com.example.chatapplication.adapters.StoryPostAdapters;
import com.example.chatapplication.adapters.UserAdapters;
import com.example.chatapplication.databinding.FragmentStoryBinding;
import com.example.chatapplication.models.PostStory;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StoryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private FragmentStoryBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStoryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());

        binding.container.setOnRefreshListener(this::onRefresh);
        binding.container.setColorSchemeColors(getResources().getColor(R.color.color_main));
        binding.container.setRefreshing(true);

        setListener();
    }

    private void getPosts() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_POSTS).get().addOnCompleteListener(task -> {
            binding.container.setRefreshing(false);
            if (task.isSuccessful() && task.getResult() != null) {
                List<PostStory> postStoryList = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    PostStory postStory = new PostStory();
                    postStory.idAuthor = queryDocumentSnapshot.getString(Constants.KEY_POST_ID_AUTHOR);
                    postStory.nameAuthor = queryDocumentSnapshot.getString(Constants.KEY_POST_NAME_AUTHOR);
                    postStory.imageAuthor = queryDocumentSnapshot.getString(Constants.KEY_POST_IMAGE_AUTHOR);
                    postStory.emailAuthor = queryDocumentSnapshot.getString(Constants.KEY_POST_EMAIL_AUTHOR);
                    postStory.text = queryDocumentSnapshot.getString(Constants.KEY_POST_TEXT);
                    postStory.image = queryDocumentSnapshot.getString(Constants.KEY_POST_IMAGE);
                    postStory.dateObject = queryDocumentSnapshot.getDate(Constants.KEY_POST_TIMESTAMP);

                    postStoryList.add(postStory);
                }
                setAdapterFriends(postStoryList);
            }
        });

    }

    private void setAdapterFriends(List<PostStory> postStoryList) {
        binding.container.setRefreshing(false);

        if (postStoryList.size() > 0) {
            Collections.sort(postStoryList, (user, t1) -> t1.dateObject.compareTo(user.dateObject));

            StoryPostAdapters storyPostAdapters = new StoryPostAdapters(postStoryList);
            binding.storyRecyclerView.setAdapter(storyPostAdapters);
            binding.storyRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void setListener() {
        binding.addStory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), StoryPost.class);
            startActivity(intent);
        });

        binding.imageAddStory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), StoryPost.class);
            startActivity(intent);
        });
    }

    @Override
    public void onRefresh() {
        getPosts();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPosts();
    }
}