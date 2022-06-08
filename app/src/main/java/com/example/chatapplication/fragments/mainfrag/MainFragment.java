package com.example.chatapplication.fragments.mainfrag;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.activities.ChatActivity;
import com.example.chatapplication.activities.ChatGroupActivity;
import com.example.chatapplication.activities.GroupSearchActivity;
import com.example.chatapplication.activities.ProfileActivity;
import com.example.chatapplication.activities.SearchActivity;
import com.example.chatapplication.adapters.RecentConversationsAdapter;
import com.example.chatapplication.databinding.FragmentMainBinding;
import com.example.chatapplication.listeners.ConversionListener;
import com.example.chatapplication.models.ChatMessage;
import com.example.chatapplication.models.Group;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainFragment extends Fragment implements ConversionListener, SwipeRefreshLayout.OnRefreshListener {

    private FragmentMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    public RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
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

//        init();
//        loadUserDetails();
//        getToken();
//        listenConversations();
//        setListener();

    }

    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListener() {
        binding.imageProfile.setOnClickListener(v -> {
            User user = new User();
            user.image = preferenceManager.getString(Constants.KEY_IMAGE);
            user.name = preferenceManager.getString(Constants.KEY_NAME);
            user.email = preferenceManager.getString(Constants.KEY_EMAIL);

            Intent intent = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("you", "1");
            intent.putExtra("myID", "myID");
            intent.putExtra("myName", "myName");
            intent.putExtra("myImage", "myImage");
            startActivity(intent);
        });

        binding.imageSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), SearchActivity.class);
            startActivity(intent);
        });

        binding.imageGroups.setOnClickListener(v -> {
            showDialogCreateGroup();
        });
    }

    private void showDialogCreateGroup() {
        Dialog dialog = new Dialog(binding.getRoot().getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_group);

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams w = window.getAttributes();
        w.gravity = Gravity.CENTER;
        window.setAttributes(w);

        if (Gravity.BOTTOM == Gravity.CENTER) {
            dialog.setCancelable(true);
        } else {
            dialog.setCancelable(false);
        }

        Button no = dialog.findViewById(R.id.btn_no);
        Button yes = dialog.findViewById(R.id.btn_yes);
        EditText text = dialog.findViewById(R.id.text);

        no.setOnClickListener(v -> {
            dialog.dismiss();
        });

        yes.setOnClickListener(v -> {
            if (text.getText().toString().isEmpty()) {
                showToast("Bạn chưa đặt tên cho nhóm chat!");
            } else {
                HashMap<String, Object> group = new HashMap<>();
                group.put(Constants.KEY_GROUP_NAME, text.getText().toString());
                group.put(Constants.KEY_LAST_MESSAGE, preferenceManager.getString(Constants.KEY_NAME) + " vừa tạo một nhóm mới");
                group.put(Constants.KEY_TIMESTAMP, new Date());
                group.put(Constants.KEY_GROUP_ADMIN_ID, preferenceManager.getString(Constants.KEY_USER_ID));

                database.collection(Constants.KEY_COLLECTION_GROUPS).add(group).addOnSuccessListener(documentReference -> {
                    HashMap<String, Object> myUser = new HashMap<>();
                    myUser.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                    myUser.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                    myUser.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                    myUser.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));

                    database.collection(Constants.KEY_COLLECTION_GROUPS).document(documentReference.getId()).collection(Constants.KEY_COLLECTION_GROUP_MEMBERS).add(myUser).addOnSuccessListener(documentReference2 -> {
                        dialog.dismiss();
                        Group groupp = new Group();
                        groupp.id = documentReference.getId();
                        groupp.name = text.getText().toString();
                        groupp.image1 = preferenceManager.getString(Constants.KEY_IMAGE);

                        Intent intent = new Intent(getActivity().getApplicationContext(), ChatGroupActivity.class);
                        intent.putExtra(Constants.KEY_GROUP, groupp);
                        startActivity(intent);
                    });
                });
            }
        });

        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(binding.getRoot().getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadUserDetails() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receivedId = receiverId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(documentChange.getDocument().getString(Constants.KEY_LAST_USER))) {
                        chatMessage.messafe = "Bạn: " +documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    } else {
                        chatMessage.messafe = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                        if (documentChange.getDocument().getString(Constants.KEY_LAST_READ).equals("0")) {
                            chatMessage.read = "ok";
                        }
                        chatMessage.conversations = documentChange.getDocument().getString(Constants.KEY_COLLECTION_CONVERSATIONS);
                    }

                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i=0; i<conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receivedId.equals(receiverId)) {

                            if (preferenceManager.getString(Constants.KEY_USER_ID).equals(documentChange.getDocument().getString(Constants.KEY_LAST_USER))) {
                                conversations.get(i).messafe = "Bạn: " +documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            } else {
                                conversations.get(i).messafe = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                if (documentChange.getDocument().getString(Constants.KEY_LAST_READ).equals("0")) {
                                    conversations.get(i).read = "ok";
                                }
                                conversations.get(i).conversations = documentChange.getDocument().getString(Constants.KEY_COLLECTION_CONVERSATIONS);
                            }
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.container.setRefreshing(false);
        } else {
            conversationsAdapter.notifyDataSetChanged();
        }
    };

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token);
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        init();
        loadUserDetails();
        getToken();
        listenConversations();
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        loadUserDetails();
        getToken();
        listenConversations();
        setListener();
    }
}