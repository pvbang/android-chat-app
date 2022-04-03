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
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityWaitCallingBinding;
import com.example.chatapplication.databinding.ActivityWaitCallingReceiverBinding;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallingReceiverActivity extends AppCompatActivity {

    private ActivityWaitCallingReceiverBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWaitCallingReceiverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        binding.nameUser.setText(getIntent().getStringExtra(Constants.KEY_NAME));
        binding.avatarUser.setImageBitmap(getBitmapFromEncodedString(preferenceManager.getString(Constants.KEY_IMAGE)));

        binding.imageAccepted.setOnClickListener(view -> sendInvitationResponse(Constants.REMOTE_MSG_CALLING_INVITATION_ACCEPTED, getIntent().getStringExtra(Constants.REMOTE_MSG_CALLING_TOKEN)));
        binding.imageEndCalling.setOnClickListener(view -> sendInvitationResponse(Constants.REMOTE_MSG_CALLING_INVITATION_REJECTED, getIntent().getStringExtra(Constants.REMOTE_MSG_CALLING_TOKEN)));

    }

    private void showToast(String message) {
        Toast.makeText(CallingReceiverActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void sendInvitationResponse(String type, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_CALLING_TYPE, Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_CALLING_DATA, data);
            body.put(Constants.REMOTE_MSG_CALLING_REGISTRATION_IDS, tokens);

            sendRemoteCalling(body.toString(), type);

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
                    if (type.equals(Constants.REMOTE_MSG_CALLING_INVITATION_ACCEPTED)) {
                        try {
                            URL serverURL = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(serverURL)
                                    .setWelcomePageEnabled(false)
                                    .setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_CALLING_ROOM))
                                    .build();
                            JitsiMeetActivity.launch(CallingReceiverActivity.this, conferenceOptions);
                            finish();

                        } catch (Exception exception) {
                            showToast(exception.getMessage());
                            finish();
                        }
                    } else {
                        showToast("Kết thúc cuộc gọi");
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

    private BroadcastReceiver callingResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_CALLING_INVITATION_CANCELLED)) {
                    showToast("Cuộc gọi kết thúc!");
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

