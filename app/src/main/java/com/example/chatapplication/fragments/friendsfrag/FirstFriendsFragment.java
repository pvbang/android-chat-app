package com.example.chatapplication.fragments.friendsfrag;

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
import com.example.chatapplication.activities.ChatActivity;
import com.example.chatapplication.adapters.FriendFragmentAdapter;
import com.example.chatapplication.adapters.UserAdapters;
import com.example.chatapplication.databinding.FragmentFirstFriendsBinding;
import com.example.chatapplication.databinding.FragmentFriendsBinding;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FirstFriendsFragment extends Fragment implements UserListener, SwipeRefreshLayout.OnRefreshListener {

    private FragmentFirstFriendsBinding binding;
    PreferenceManager preferenceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstFriendsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());

        binding.container.setOnRefreshListener(this::onRefresh);
        binding.container.setColorSchemeColors(getResources().getColor(R.color.color_main));


        getUsers();
    }

    private void getUsers() {
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
                if (usersList.size() > 0) {
                    Collections.sort(usersList, (user, t1) -> user.getName().compareToIgnoreCase(t1.getName()));

                    UserAdapters userAdapters = new UserAdapters(usersList, this);
                    binding.usersRecyclerView.setAdapter(userAdapters);
                    binding.usersRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    showErrorMessage();
                }
            } else {
                showErrorMessage();
            }
        });

    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "Chưa có người dùng nào"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        getUsers();
    }
}