package com.example.chatapplication.fragments.friendsfrag;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.R;
import com.example.chatapplication.adapters.FriendFragmentAdapter;
import com.example.chatapplication.databinding.FragmentFriendsBinding;
import com.example.chatapplication.databinding.FragmentGroupFriendsBinding;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.material.tabs.TabLayoutMediator;

public class GroupFriendsFragment extends Fragment {

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



    }

}