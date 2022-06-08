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
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.activities.ChatActivity;
import com.example.chatapplication.activities.ChatGroupActivity;
import com.example.chatapplication.activities.InfoChatGroupActivity;
import com.example.chatapplication.adapters.FriendFragmentAdapter;
import com.example.chatapplication.adapters.GroupAdapters;
import com.example.chatapplication.adapters.RequestAdapters;
import com.example.chatapplication.databinding.FragmentFriendsBinding;
import com.example.chatapplication.databinding.FragmentGroupFriendsBinding;
import com.example.chatapplication.listeners.GroupListener;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.Group;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupFriendsFragment extends Fragment implements GroupListener, SwipeRefreshLayout.OnRefreshListener{

    private FragmentGroupFriendsBinding binding;
    PreferenceManager preferenceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGroupFriendsBinding.inflate(inflater, container, false);
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

        getGroups();

    }

    private void getGroups() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_GROUPS).get().addOnCompleteListener(task -> {
            binding.container.setRefreshing(false);
            String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
            if (task.isSuccessful() && task.getResult() != null) {
                List<Group> groupsList = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                        continue;
                    }
                    Group group = new Group();
                    group.id = queryDocumentSnapshot.getId();
                    group.name = queryDocumentSnapshot.getString(Constants.KEY_GROUP_NAME);
                    group.image1 = preferenceManager.getString(Constants.KEY_IMAGE);
                    group.time = getReadableDateTime(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
                    group.dateObject = queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP);
                    group.message = queryDocumentSnapshot.getString(Constants.KEY_LAST_MESSAGE);
                    groupsList.add(group);
                }
                if (groupsList.size() > 0) {
                    Collections.sort(groupsList, (user, t1) -> t1.getDateObject().compareTo(user.getDateObject()));

                    GroupAdapters groupAdapters = new GroupAdapters(groupsList, this, currentUserId);
                    binding.groupsRecyclerView.setAdapter(groupAdapters);
                    binding.groupsRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
    }

    @Override
    public void onGroupClicked(Group group) {
        Intent intent = new Intent(getActivity().getApplicationContext(), ChatGroupActivity.class);
        intent.putExtra(Constants.KEY_GROUP, group);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        binding.container.setRefreshing(true);
        getGroups();
    }

    @Override
    public void onResume() {
        super.onResume();
        getGroups();
    }
}