package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.databinding.ActivityWaitCallingBinding;
import com.example.chatapplication.models.User;
import com.example.chatapplication.network.ApiClient;
import com.example.chatapplication.network.ApiService;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallingActivity extends AppCompatActivity {

    private ActivityWaitCallingBinding binding;
    private User user;

    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String callingType = null;

    String callingRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWaitCallingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        user = (User) getIntent().getSerializableExtra("user");
        callingType = getIntent().getStringExtra("type");

        binding.nameUser.setText(user.name);
        binding.imageEndCalling.setOnClickListener(v -> {
            if (user != null) {
                cancelInvitation(user.token);
            }
        });
        binding.avatarUser.setImageBitmap(getBitmapFromEncodedString(user.image));

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult();
                if (callingType != null && user != null) {
                    initCalling(callingType, user.token);
                }
            }
        });

    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(CallingActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void initCalling(String callingType, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_CALLING_TYPE, Constants.REMOTE_MSG_CALLING_INVITATION);
            data.put(Constants.REMOTE_MSG_CALLING_MEETING_TYPE, callingType);
            data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_CALLING_TOKEN, inviterToken);

            callingRoom = preferenceManager.getString(Constants.KEY_USER_ID)+ "_" + UUID.randomUUID().toString().substring(0, 5);
            data.put(Constants.REMOTE_MSG_CALLING_ROOM, callingRoom);

            body.put(Constants.REMOTE_MSG_CALLING_DATA, data);
            body.put(Constants.REMOTE_MSG_CALLING_REGISTRATION_IDS, tokens);

            sendRemoteCalling(body.toString(), Constants.REMOTE_MSG_CALLING_INVITATION);

        } catch (Exception e) {
            showToast(e.getMessage());
            finish();
        }
    }

    private void sendRemoteCalling(String remoteCallingBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteCallingHeaders(), remoteCallingBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MSG_CALLING_INVITATION)) {
                        showToast("Người nhận đang đỗ chuông...");
                    } else if (type.equals(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE)) {
                        showToast("Cancelled");
                        finish();
                    }
                } else {
                    showToast(response.message());
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_CALLING_TYPE, Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE, Constants.REMOTE_MSG_CALLING_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_CALLING_DATA, data);
            body.put(Constants.REMOTE_MSG_CALLING_REGISTRATION_IDS, tokens);

            sendRemoteCalling(body.toString(), Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE);

        } catch (Exception e) {
            showToast(e.getMessage());
            finish();
        }
    }

    private BroadcastReceiver callingResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_CALLING_INVITATION_ACCEPTED)) {
                    try {
                        URL serverURL = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                                .setServerURL(serverURL)
                                .setWelcomePageEnabled(false)
                                .setRoom(callingRoom)
                                .build();
                        JitsiMeetActivity.launch(CallingActivity.this, conferenceOptions);
                        finish();

                    } catch (Exception exception) {
                        showToast(exception.getMessage());
                        finish();
                    }
                } else if (type.equals(Constants.REMOTE_MSG_CALLING_INVITATION_REJECTED)) {
                    showToast("Kết thúc cuộc gọi");
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(callingResponseReceiver, new IntentFilter(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(callingResponseReceiver);
    }
}