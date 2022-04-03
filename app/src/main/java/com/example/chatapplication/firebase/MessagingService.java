package com.example.chatapplication.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.chatapplication.R;
import com.example.chatapplication.activities.CallingActivity;
import com.example.chatapplication.activities.CallingReceiverActivity;
import com.example.chatapplication.activities.ChatActivity;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        User user = new User();
        user.id = message.getData().get(Constants.KEY_USER_ID);
        user.name = message.getData().get(Constants.KEY_NAME);
        user.token = message.getData().get(Constants.KEY_FCM_TOKEN);

        // calling
        String type = message.getData().get(Constants.REMOTE_MSG_CALLING_TYPE);
        if (type != null) {
            if (type.equals(Constants.REMOTE_MSG_CALLING_INVITATION)) {
                Intent intent = new Intent(getApplicationContext(), CallingReceiverActivity.class);
                intent.putExtra(Constants.REMOTE_MSG_CALLING_MEETING_TYPE, message.getData().get(Constants.REMOTE_MSG_CALLING_TYPE));
                intent.putExtra(Constants.KEY_NAME, message.getData().get(Constants.KEY_NAME));
                intent.putExtra(Constants.KEY_IMAGE, message.getData().get(Constants.KEY_IMAGE));
                intent.putExtra(Constants.KEY_EMAIL, message.getData().get(Constants.KEY_EMAIL));

                intent.putExtra(Constants.REMOTE_MSG_CALLING_TOKEN, message.getData().get(Constants.REMOTE_MSG_CALLING_TOKEN));
                intent.putExtra(Constants.REMOTE_MSG_CALLING_TOKEN, message.getData().get(Constants.REMOTE_MSG_CALLING_TOKEN));

                intent.putExtra(Constants.REMOTE_MSG_CALLING_ROOM, message.getData().get(Constants.REMOTE_MSG_CALLING_ROOM));

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else if (type.equals(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE)) {
                Intent intent = new Intent(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE);
                intent.putExtra(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE, message.getData().get(Constants.REMOTE_MSG_CALLING_INVITATION_RESPONSE));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }

        // notifications
        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (user.name != null && message.getData().get(Constants.KEY_MESSAGE) != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            builder.setSmallIcon(R.drawable.ic_logo);
            builder.setContentTitle("Bạn có tin nhắn mới");
            builder.setContentText(user.name + ": " + message.getData().get(Constants.KEY_MESSAGE));
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                    message.getData().get(Constants.KEY_MESSAGE)
            ));
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence channelName = "Tin nhắn";
                String channelDescription = "Thông báo này dùng để thông báo khi có tin nhắn mới";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                channel.setDescription(channelDescription);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(notificationId, builder.build());
        }
    }





}
