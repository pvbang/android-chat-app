package com.example.chatapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.databinding.ItemContainerGroupMessageNotificationBinding;
import com.example.chatapplication.databinding.ItemContainerReceivedImageBinding;
import com.example.chatapplication.databinding.ItemContainerReceivedMessageBinding;
import com.example.chatapplication.databinding.ItemContainerSentImageBinding;
import com.example.chatapplication.databinding.ItemContainerSentMessageBinding;
import com.example.chatapplication.models.ChatGroupMessage;
import com.example.chatapplication.models.ChatMessage;
import com.example.chatapplication.models.Group;

import java.util.List;

public class ChatGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatGroupMessage> chatGroupMessages;
    private final String senderId;
    private Group group;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_SENT_IMAGE = 3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;
    public static final int VIEW_TYPE_NOTIFICATION = 5;

    public ChatGroupAdapter(List<ChatGroupMessage> chatGroupMessages, Group group, String senderId) {
        this.chatGroupMessages = chatGroupMessages;
        this.group = group;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            return new SentImageViewHolder(
                    ItemContainerSentImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else if (viewType == VIEW_TYPE_RECEIVED_IMAGE) {
            return new ReceivedImageViewHolder(
                    ItemContainerReceivedImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            return new NotificationViewHolder(
                    ItemContainerGroupMessageNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatGroupMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_SENT_IMAGE) {
            ((SentImageViewHolder) holder).setData(chatGroupMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_IMAGE) {
            ((ReceivedImageViewHolder) holder).setData(chatGroupMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED){
            ((ReceivedMessageViewHolder) holder).setData(chatGroupMessages.get(position));
        } else {
            ((NotificationViewHolder) holder).setData(chatGroupMessages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatGroupMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatGroupMessages.get(position).senderId.equals("##########################~~")) {
            return VIEW_TYPE_NOTIFICATION;
        }
        if (chatGroupMessages.get(position).senderId.equals(senderId)) {
            if (chatGroupMessages.get(position).message.contains("/9j/")) {
                return VIEW_TYPE_SENT_IMAGE;
            } else {
                return VIEW_TYPE_SENT;
            }
        } else {
            if (chatGroupMessages.get(position).message.contains("/9j/")) {
                return VIEW_TYPE_RECEIVED_IMAGE;
            } else {
                return VIEW_TYPE_RECEIVED;
            }
        }

    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatGroupMessage chatGroupMessage) {
            binding.textMessage.setText(chatGroupMessage.message);
            binding.textDateTime.setText(chatGroupMessage.dataTime);

        }
    }


    static class SentImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentImageBinding bindingImage;

        SentImageViewHolder(ItemContainerSentImageBinding itemContainerSentImageBinding) {
            super(itemContainerSentImageBinding.getRoot());
            bindingImage = itemContainerSentImageBinding;

        }

        void setData(ChatGroupMessage chatGroupMessage) {
            bindingImage.textMessage.setImageBitmap(getBitmapFromEncodedString(chatGroupMessage.message));
            bindingImage.textDateTime.setText(chatGroupMessage.dataTime);
        }

    }

    private static Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatGroupMessage chatGroupMessage) {
            binding.textMessage.setText(chatGroupMessage.message);
            binding.textDateTime.setText(chatGroupMessage.dataTime+ " - " +chatGroupMessage.senderName);
            binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(chatGroupMessage.senderImage));
        }
    }

    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedImageBinding binding;

        ReceivedImageViewHolder(ItemContainerReceivedImageBinding itemContainerReceivedImageBinding) {
            super(itemContainerReceivedImageBinding.getRoot());
            binding = itemContainerReceivedImageBinding;
        }

        void setData(ChatGroupMessage chatGroupMessage) {
            binding.textMessage.setImageBitmap(getBitmapFromEncodedString(chatGroupMessage.message));
            binding.textDateTime.setText(chatGroupMessage.dataTime+ " - " +chatGroupMessage.senderName);
            binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(chatGroupMessage.senderImage));

        }
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerGroupMessageNotificationBinding binding;

        NotificationViewHolder(ItemContainerGroupMessageNotificationBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatGroupMessage chatGroupMessage) {
            binding.textNotification.setText(chatGroupMessage.message);

        }
    }

}