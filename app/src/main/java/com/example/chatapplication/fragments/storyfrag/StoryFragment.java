package com.example.chatapplication.fragments.storyfrag;

import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StoryFragment extends Fragment implements View.OnClickListener {

    private FloatingActionButton fab;
    private ArrayList<StoryItem> storyItems = new ArrayList<>();
    private RecyclerView recyclerView;
    private StoryRecyclerView storyRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_story, container, false);
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(this::onClick);
        recyclerView = view.findViewById(R.id.storyRecyclerView);
        displayAllPosts();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        storyRecyclerView = new StoryRecyclerView(storyItems);
        recyclerView.setAdapter(storyRecyclerView);
        storyRecyclerView.notifyDataSetChanged();

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

    public void displayAllPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance("https://chat-application-f4d3d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    StoryItem storyItem = dataSnapshot.getValue(StoryItem.class);
                    storyItems.add(storyItem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}