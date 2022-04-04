package com.example.chatapplication.fragments.storyfrag;

import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StoryFragment extends Fragment implements View.OnClickListener {

    private FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_story, container, false);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(this::onClick);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab:
                Intent intent = new Intent(getActivity(), StoryPost.class);
                startActivity(intent);
                break;
        }
    }
}