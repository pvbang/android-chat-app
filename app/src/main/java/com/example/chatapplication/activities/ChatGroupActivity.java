package com.example.chatapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.chatapplication.adapters.ChatAdapter;
import com.example.chatapplication.adapters.ChatGroupAdapter;
import com.example.chatapplication.adapters.GroupAdapters;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.databinding.ActivityChatGroupBinding;
import com.example.chatapplication.models.ChatGroupMessage;
import com.example.chatapplication.models.ChatMessage;
import com.example.chatapplication.models.Group;
import com.example.chatapplication.models.User;
import com.example.chatapplication.network.ApiClient;
import com.example.chatapplication.network.ApiService;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatGroupActivity extends BaseActivity {

    private ActivityChatGroupBinding binding;

    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Group group;
    private List<ChatGroupMessage> chatGroupMessage;
    private ChatGroupAdapter chatGroupAdapter;

    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        listenMessages();
        chatGroupAdapter = new ChatGroupAdapter(chatGroupMessage, group, preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(chatGroupAdapter);
        setListeners();
        setData();

    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        group = (Group) getIntent().getSerializableExtra(Constants.KEY_GROUP);
        chatGroupMessage = new ArrayList<>();

    }

    private void setData() {
        binding.textName.setText(group.name);
        binding.imageProfile1.setImageBitmap(getGroupImage(group.image1));
        binding.imageProfile2.setImageBitmap(getGroupImage(group.image2));

    }

    private void intentInfoChatActivity() {
        Intent intent = new Intent(getApplicationContext(), InfoChatGroupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.KEY_GROUP, (Group) group);
        startActivity(intent);
    }

    private Bitmap getGroupImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        binding.layoutSend.setOnClickListener(v -> {
            if (!binding.inputMessage.getText().toString().isEmpty()) {
                sendMessage();
            }
        });

        binding.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.imageCallAudio.setOnClickListener(v -> {});
        binding.imageCallVideo.setOnClickListener(v -> {});

        binding.imageInfo.setOnClickListener(v -> {
            intentInfoChatActivity();
        });

        binding.textName.setOnClickListener(v -> {
            intentInfoChatActivity();
        });
        binding.imageProfile.setOnClickListener(v -> {
            intentInfoChatActivity();
        });
        binding.imageProfile1.setOnClickListener(v -> {
            intentInfoChatActivity();
        });
        binding.imageProfileBackground.setOnClickListener(v -> {
            intentInfoChatActivity();
        });
        binding.imageOnline.setOnClickListener(v -> {
            intentInfoChatActivity();
        });
        binding.textAvailablility.setOnClickListener(v -> {
            intentInfoChatActivity();
        });

        binding.imageVoice.setOnClickListener(v -> {
            speak(v);
        });

    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
        message.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());

        database.collection(Constants.KEY_COLLECTION_GROUPS).document(group.id).collection(Constants.KEY_COLLECTION_GROUP_MESSAGES).add(message);

        DocumentReference documentReferenceee = database.collection(Constants.KEY_COLLECTION_GROUPS).document(group.id);
        documentReferenceee.update(Constants.KEY_LAST_MESSAGE, preferenceManager.getString(Constants.KEY_NAME)+ ": " +binding.inputMessage.getText().toString());
        documentReferenceee.update(Constants.KEY_TIMESTAMP, new Date());

        binding.inputMessage.setText(null);
    }

    private void sendImageMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
        message.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        message.put(Constants.KEY_MESSAGE, encodedImage);
        message.put(Constants.KEY_TIMESTAMP, new Date());

        database.collection(Constants.KEY_COLLECTION_GROUPS).document(group.id).collection(Constants.KEY_COLLECTION_GROUP_MESSAGES).add(message);

        DocumentReference documentReferenceee = database.collection(Constants.KEY_COLLECTION_GROUPS).document(group.id);
        documentReferenceee.update(Constants.KEY_LAST_MESSAGE, preferenceManager.getString(Constants.KEY_NAME)+ " đã gửi một hình ảnh");
        documentReferenceee.update(Constants.KEY_TIMESTAMP, new Date());

    }

    private void speak(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi_VN");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Đọc đoạn tin nhắn bạn muốn gửi đi...");
        someActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    binding.inputMessage.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
                }
            }
        });

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            encodedImage = encodeImage(bitmap);
                            sendImageMessage();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 1000;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitMap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitMap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_GROUPS).document(group.id).collection(Constants.KEY_COLLECTION_GROUP_MESSAGES).addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        } if (value != null) {
            int count = chatGroupMessage.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatGroupMessage chatGroupMessage1 = new ChatGroupMessage();
                    chatGroupMessage1.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatGroupMessage1.senderImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                    chatGroupMessage1.senderName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                    chatGroupMessage1.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatGroupMessage1.dataTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatGroupMessage1.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatGroupMessage1.encodedImage = encodedImage;

                    chatGroupMessage.add(chatGroupMessage1);
                }
            }
            Collections.sort(chatGroupMessage, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

            if (count == 0) {
                chatGroupAdapter.notifyDataSetChanged();
            } else {
                chatGroupAdapter.notifyItemRangeInserted(chatGroupMessage.size(), chatGroupMessage.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatGroupMessage.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.getDefault()).format(date);
    }

}