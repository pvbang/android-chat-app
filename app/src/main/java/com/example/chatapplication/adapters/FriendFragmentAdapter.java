package com.example.chatapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.chatapplication.fragments.friendsfrag.FirstFriendsFragment;
import com.example.chatapplication.fragments.friendsfrag.FriendsFragment;
import com.example.chatapplication.fragments.friendsfrag.GroupFriendsFragment;

public class FriendFragmentAdapter extends FragmentStateAdapter {

    public FriendFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new GroupFriendsFragment();
        }
        return new FirstFriendsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
