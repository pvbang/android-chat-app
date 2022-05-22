package com.example.chatapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.chatapplication.fragments.friendsfrag.FirstFriendsFragment;
import com.example.chatapplication.fragments.friendsfrag.GroupFriendsFragment;

public class FriendFragmentAdapter extends FragmentStateAdapter {

    public FriendFragmentAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FirstFriendsFragment();
            case 1:
                return new GroupFriendsFragment();
            default:
                return new FirstFriendsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
