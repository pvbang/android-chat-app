package com.example.chatapplication.fragments.friendsfrag;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.R;
import com.example.chatapplication.activities.ChatActivity;
import com.example.chatapplication.activities.SearchActivity;
import com.example.chatapplication.adapters.FriendFragmentAdapter;
import com.example.chatapplication.databinding.FragmentFriendsBinding;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FriendsFragment extends Fragment {

    private FragmentFriendsBinding binding;
    private PreferenceManager preferenceManager;
    private FriendFragmentAdapter friendFragmentAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());

        setListener();

    }

    private void setListener() {
        binding.imageAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), SearchActivity.class);
            startActivity(intent);
        });

        friendFragmentAdapter = new FriendFragmentAdapter(this.getActivity());
        binding.viewPager.setAdapter(friendFragmentAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Bạn bè");
            }
            if (position == 1) {
                tab.setText("Nhóm");
            }
            if (position == 2) {
                tab.setText("Lời mời");
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).get().addOnCompleteListener(task1 -> {
                    int countRequests = 0;
                    if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                            countRequests++;
                        }
                        if (countRequests != 0) {
                            tab.getOrCreateBadge().setNumber(countRequests);
                        }
                    } else {
                        tab.removeBadge();
                    }
                });
            }
        }).attach();
    }

}