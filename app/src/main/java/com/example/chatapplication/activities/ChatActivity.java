package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.adapters.ChatAdapter;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.fragments.mainfrag.MainFragment;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.ChatMessage;
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
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receivedUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;

    public String SENT_IMAGE = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(binding.getRoot());
        setListeners();
        loadReceivedDetails();
        init();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, getBitmapFromEncodedString(receivedUser.image), preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receivedUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());

        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null) {
            updateCOnversion(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receivedUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receivedUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receivedUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());

            addConversion(conversion);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receivedUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);

    }

    private void sendImageMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receivedUser.id);
        message.put(Constants.KEY_MESSAGE, "/9j/4AAQSkZJRgABAQAAAQABAAD/4gIoSUNDX1BST0ZJTEUAAQEAAAIYAAAAAAIQAABtbnRyUkdCIFhZWiAAAAAAAAAAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAAHRyWFlaAAABZAAAABRnWFlaAAABeAAAABRiWFlaAAABjAAAABRyVFJDAAABoAAAAChnVFJDAAABoAAAAChiVFJDAAABoAAAACh3dHB0AAAByAAAABRjcHJ0AAAB3AAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAFgAAAAcAHMAUgBHAEIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFhZWiAAAAAAAABvogAAOPUAAAOQWFlaIAAAAAAAAGKZAAC3hQAAGNpYWVogAAAAAAAAJKAAAA+EAAC2z3BhcmEAAAAAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABYWVogAAAAAAAA9tYAAQAAAADTLW1sdWMAAAAAAAAAAQAAAAxlblVTAAAAIAAAABwARwBvAG8AZwBsAGUAIABJAG4AYwAuACAAMgAwADEANv/bAEMAEAsMDgwKEA4NDhIREBMYKBoYFhYYMSMlHSg6Mz08OTM4N0BIXE5ARFdFNzhQbVFXX2JnaGc+TXF5cGR4XGVnY//bAEMBERISGBUYLxoaL2NCOEJjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY//AABEIAJYAlgMBIgACEQEDEQH/xAAbAAABBQEBAAAAAAAAAAAAAAAAAgMEBQYBB//EAEMQAAEDAgQCBwQFCQgDAAAAAAEAAgMEEQUSITFBUQYTImFxgZGhscHRFBUyUnMHIyQzNDVCovAWRGJyg5Lh8SVDU//EABoBAAIDAQEAAAAAAAAAAAAAAAMEAAIFAQb/xAAtEQACAgEEAQIFAgcAAAAAAAABAgADEQQSITFBBRMUIjJR8GFxIzNCgZGx0f/aAAwDAQACEQMRAD8A9AQhCkkEITFZVwUNO6epkEcbdyV0DPAkjyr8RxzDsNJZU1DetAJ6pnadte1htfvssjjPS6qrA6GiDqWG/wBsH84fMbcNvVZtPVaInl5zM1tZ04lcSKKkawX0dKcxI8Bax8yqOox/FahwdJXzAgW/NuyD+Wyrk9S0slVJkjtpqXHYJv26aVLEYAnCcdwkmqa2UdbJLPIdBmcXFSocLkdrM4M7hqVe0dFDRx5Y23dxedyuzQOc8ubbXgs+r1ap7CnS+DFLrXx8ki0eeiZkp5pmA7gSEA+WyclmlmIMsj5COLnEpBFjY7oWngdzPZ2bszoJaQQSCNiFIjxGsjeHNqpSR95xI9CoyFCoPYnAxHRl3TdIpGgNqYg8feZofT/pXdLWU9W0ugkDrbjYjyWObTvc0HQX5pyKKaGVskbw17TcEFZdr6Q5w4Bjld9q/UMibVCrcMxE1DWxT2E3MbO+SskqGVvpOZoKwYZEEIQuy0EITFZVQ0VLJU1D8kUYu4qdyRnFcTp8Ko3VFS7TZrRu48gvNcUxapxeqM1Q85ASI4+DBfb/AJ7kjGMWnxetdUTEtZfsRBxIYO7015puqqRV1Uk4gigDyLRxNytbYAaBaump2HJ7nDJOE4bJiNU1mVwhB7bwNAOXio9XSy0dQ6Gdpa4ejhzHcrTAMYdRytpqh/6M46E/+s/L/vneHimKT4lNd5yxA9iPg35qqPqTqmUgbMfn9/uPw2IXb+sThEdPLiULKuN8kBJztYbE6HvHFX8cENODHThwizEjNufHvUfAMMe6B05GUyDskjh/z8lMex0bsr2lp71jepav3bTWp4H+4rbuxnHEfXCQNyB4rqjVZ7TRyCz9Jp/iLRXnEWdtozGpXB0jiNkhCF7JFCKFHQiROTmCdjgc4gkWadU4ymFu2TfkFIAsLBY2s9VCjbQcn7/8h0p8tBCELzkZhsr/AAyt+kx5H/rGDX/EOaoEuCV0EzZWfaabo1NprbPiXRtpmqQkRvbLG17DdrhcIW13HIpefdNsX+mVooYXXgpz29N5NQfTb17lsOkGI/VmDz1DXAS2yx3I+0dBa+9t7dy8pTmlryd5nDOKZNQ1NHHC+oiLGzsEkbrghzT4e5Q1JY6oqnsjLpZnNAZG0kuIA2aO7uWguczkQpNBQVFfUNip4pJLuAcWtvlB4nktTgnRBn63FO2eETHGw04kcfDlxWuiijhjbHExrGN0DWiwHkl7tWq/KnMg5EoaPCa2JtmdXELWyud7dLp76gcdXVWp37F/irtCwxpqxyRK+2MYMo34NO2+WRjgBxuCVVYhRVMJzyQuDAPtDUD02WxXEbTolFnuKIOzTq4x1MElR/rWeIWqr8Gp6sOfGBFMdcw2PiFnJKOelqhFMyxBvfgRzBWqdQj1MeuDM96HrIz1JCEIXjIzBCEKSQQhCkku8Fmz0zojvGdPA/0UKDhMrIalzpHBoLCNfEIWtp7R7YyY1Ww28yn/ACg1hdPS0TS6zWmVwvoSdB5ix9VSYdh9DU4fUTVFaIpGWNspOQX3I43202SOkc7qnpBXPcACJSzT/D2fgq1bgqY1BVbH7QoODzOta572sY0uc42AAuSe5b3o50fbhjBUVADqtw8oxyHfzP8ARp+hmFioqH10zA6OE5Ywde3vfyHv7ltJJGxsL3uDWjclUvsOdixW6zHElxCzB36papKjHCDlpoxlH8T+Pkq2bEqp3bkqXtAGpDso9iCumduTxAN6hUnyrzNZeyS2WN5IZI1xG4BvZYWXEaVru3UBxOtxd3uVvhmP4ZDSBkk2RwJucju136D+rK76VlGRzL06p7WwUwPz9JpV1UrOkOFPeGirAJNtWOA9SFPgqoagH6PPHKG75Hh1vRLtWy9iN7pLTNTTx1MWSQXG4twPNKDzx1SwQdkNlyMGdyDxMvUQPp5nRvGo2PMc02r/ABWmE1MZB9uMXB7uIVAsa+r22x4irrtMEIQgSkEIXCQBcmwXQM8CSdQpGFxw1lS6NzjYMLtPEfNCZGkt8jH7y6IXGRMBVTuqaqaofYOleXm3Mm6kUuE11ZD1sFOXR3sHFwF/C5UNehYOxow6iaGgAxsJA7wLr1lr+2BiEusKAYk7CKP6vwunpSbuY3ta37R1PlclV/SCs6qWKHKT2c5105fAq8WZ6S/vCP8ACHvKX0w3W5MTtG9SDKDEcXlEroqchjRu62pNtf67lV53SSZnuLnHcuNyu1P7TJ/mKIW9sEp8DmOU0pUo2iPRx8SnVxdRYScTb3lxytQ95ccrUtjA0d6kktsN6QV9CQ18pqIr6slNzw2O428O5b2CVsscc0Zux7Q4HmCvKZJLaDfmtjg9bUdRRQ9aertGLWG2nFJamgMMrxAXXCnGfM1iz9TQTMmkyR3YCSLHgtCo847YPcsWylbeGhbh8uZmkLlVIRVTCw+273poyuPILHKEHEVzHlCrZw1wYNdLp0uJ3Kg1pDZMxNgG6rT9JrB1Iz9jKPyMRqLGZ8NresgylxYWkHbUg/BCppHmSRzzxKF6d6Uc5Ij1a7FAncQgbS4jVU7CS2KV7BfkCQt5hH7BRfhR+4LJ9LKP6H0gqQGkMlPWtub3zbn/AHZlNwrpNDS0cUVTHK58XZBYAQQNuI8EpYC6AiD1CFgMTbrM9Jf3hH+EPeVplmekv7wj/CHvKHo/5sVfqZCdv6TIT94oYbOBSqj9fJ/mKbWpjEfX6RJKbe8uOVqZ6x32WnRORvDRqNea4DmdxHWMDR380iSS2g3SXzi1hoU3mHNTIkxOrXYR/cf9P4LIZhzWvwf+4/6fwQ7D8szvUP6P3m1TM/8ACnlSYnjcFJVSQGOR0jANrWJIvvfvWNWjOcKI9cQE5lTV/tc34jvemk3DK6cySPtdzy4gbC+qcWRqKzXayHxFAcwJsLnZUeL1LZJerjNwNHEc+SdxDE2uY6GnN76Of8lWwTyU07J4XZZIzma617Fbfpujav8AjPwfAh66/Jln0ZwyLFcQkgnJDWxF4tzuB8UK66CxPqKiuxKYEuccgfoA4k5naf7fVCLqL3FhCmMiK/KBQl9PT1zGX6smOQga2O1zyBv5uWGXr+JUTMQw+ekk0bK0i9r5TwPkbFeSVEElNUSQTNLJI3Frm8iETSvldv2kM9C6L1razBoQXAyQjq3gcLbey3tSOkdE6WNlTG25jFn2+7z8visxhFZ/Z/G3RyTxTU7gGSPhcXMsbEOHO3zW9E0TnNa2Vhc4ZmgOFyOY7kM5qs3r1ErUwZ57PSNldmDi1x34qHLSTt2bmHNuq9Dq8Ho6nXq+qf8Aej09myq5ejkzbdTOx/PMC35p1dTU45OJVbHTjuYzqnRjtNcO8hIc62gWr+pcQ/8Ah/O35pP1TiDXgCncDzDh77ou+vHDCX+IPlZlGRvkdljY57uTRcqRFh1XKSGwPFvvDL71tm4ZWO2i/nb80/FglS+xkcyMcRe5Hw9qXNqDzF/i9Q/0V/5/BMnTYEA5rqiUOAOrGcfNanCKR09U2SxEcZzE8yNgrGDBaeLtTPMp79Applgp2BoLGN2AFgEvZqQRtSRdPa7izUN14jksjYYnyyGzGNLnHkAvPaufr6iad3ZzvLtTtc7XVh0zx5w/8bTOLToZjbzAv6H071j3SvcbuNzzKLo12KWPZjtie7jniXjcRgp4nC5e++gbttzUCrxGapu0nJH90fEqvzE8U/FWTw0s9Mx9oqjL1gsO1lNxqu/D1Cw2hcsfvLJUqxsvHDVIJJ3XFa9G8MOKYvFE5t4WHPKeGUcPM2HmjO+Bkwk33RmhOH4HTxPZllcOskFrG5117wLDyQrVCxWO4kmWgsf05wUyMGJ07CXMAbM0AfZ4O8tj3W5LYrhAcCCAQdCCrVuUbcJJ4wr6F4DWPhdYCxaW6WV7UdEMPgnMobK6Jx0YX6N7uftSfqSjZE5tOwxE63zF3sJWomqQRHUuCceRH8Nx1r8sVZZrtAJBsfHl4+5XixFTSTUxtKzTg4bFLpcQqqTSGUhv3TqPRcs0qv8ANWYAP95tEKgg6SHQVFODzdGfgfmp4xinc0EMlsddh80m2nsXsSNai9mWCFHpKsVb3CJhDWjtOdw5aKYGgITKVODC1j3Bleo24PLTkALuGY2Cz1aJxUO+kiz/AGW7lpXOawXcQB3qoxCCKulcZA4sy5bBxF/RFocIeYHV0hlAzzMBihY7EJjGQWkjUc7a+1M03UfSGfSus6n+Lq7ZtuF9FuXdEsMlib+blhdxLHm/tuqbE+iFTTtfLRSCoY0E5CLPtyHA6eHgmveR+M4j1TqFC/aVeNOoHVl8PDgy3a4N2Gw39VXJTmuY8te0tc02IIsQVLmmoDhVPFFTPFaHEzTFxsRc2AF+8cBtxur1r7aBBkxgnJzIbWue4NaC5xNgALkleodGcIGEYYGPH6RL25Tpofu3HAe+/NU/Q/o46nLcSrW2kIvCy+rb3uSOdtvHntsEnqbt3yLOQQhCUnYIQhSScc0OBBAIPAqsqqR0RL2as9ytFxdBxBWVLYMGUTmtc0tcAQdwRuoNRhMEpJjvE48tR6LQz0LH6x9h3Lgmn0rQACCCOI4o6WleVMQah1mYdg0od2ZWFvM3B9FIbRzNaGhlwBbcK6dSn+F3qEn6M/m1H+Kc9xeynf3ImH9dSPc7K0tcNWl1vgpz61xHZaG+OqSKV3FwHgnG0zBvcoDvuOTC1q6LtU8SP+cmdxcVJigDNXau9ycAAFgAPBdVMy4XyYIQlNjc7Yac1yEAJ6mS6YYMHtbX0sfbuGSMY0dq50dzJuQOO45KR0a6Jmne2sxNjTIPsQHXKeDidr93x21jIw3vPNLVze23aI5WpAwZxdQhAhIIQhSSCEIUkghCFJILiEKSRBiae7wSDCeBCELoME6LjOIhzS02NklCFaKEYMW2MuFxZKbDzPohC4TD1op7jjY2t2HqlIQqxgADqdQhCk7BCEKSQQhCkk//2Q==");
        message.put(Constants.KEY_TIMESTAMP, new Date());

        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null) {
            updateCOnversion(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receivedUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receivedUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receivedUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, "Tin nhắn dạng hình ảnh...");
            conversion.put(Constants.KEY_TIMESTAMP, new Date());

            addConversion(conversion);
        }
        binding.inputMessage.setText(null);

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(), messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("lỗi"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    showToast(preferenceManager.getString(Constants.KEY_NAME));
//                    showToast(preferenceManager.getString(Constants.KEY_USER_ID));
                } else {
                    showToast("Lỗi: " +response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void listenAvailablilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receivedUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABLILITY) != null) {
                    int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABLILITY)).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receivedUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                if (receivedUser.image == null) {
                    receivedUser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceivierProfileImage(getBitmapFromEncodedString(receivedUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isReceiverAvailable) {
                binding.textAvailablility.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailablility.setVisibility(View.GONE);
            }
        });
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        } if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receivedId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.messafe = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dataTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    chatMessage.image = SENT_IMAGE;

                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

        if (conversionId == null) {
            checkForConversion();
        }
    };

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }

    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> {
            if (!binding.inputMessage.getText().toString().isEmpty()) {
                SENT_IMAGE = "0";
                sendMessage();
            }
        });

        binding.imageView.setOnClickListener(v -> {
            SENT_IMAGE = "1";
            sendImageMessage();
        });

        binding.imageCallAudio.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CallingActivity.class);
            intent.putExtra("type", "audio");
            intent.putExtra("user", (User) getIntent().getSerializableExtra(Constants.KEY_USER));
            intent.putExtra("image", getIntent().getStringExtra(Constants.KEY_IMAGE));
            startActivity(intent);
        });
        binding.imageCallVideo.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CallingActivity.class);
            intent.putExtra("type", "video");
            intent.putExtra("user", (User) getIntent().getSerializableExtra(Constants.KEY_USER));
            intent.putExtra("image", getIntent().getStringExtra(Constants.KEY_IMAGE));
            startActivity(intent);
        });
    }

    private void loadReceivedDetails() {
        receivedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receivedUser.name);
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).add(conversion).addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateCOnversion(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkForConversion() {
        if (chatMessages.size() != 0) {
            checkForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID), receivedUser.id);
            checkForConversionRemotely(receivedUser.id, preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get().addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailablilityOfReceiver();
    }

}