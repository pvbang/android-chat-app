package com.example.chatapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.activities.ChatActivity;
import com.example.chatapplication.databinding.FragmentMainBinding;
import com.example.chatapplication.databinding.ItemContainerRecentConversionBinding;
import com.example.chatapplication.fragments.mainfrag.MainFragment;
import com.example.chatapplication.listeners.ConversionListener;
import com.example.chatapplication.models.ChatMessage;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    private FirebaseFirestore database;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;
        private FragmentMainBinding bindingMainFragment;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage) {
            database = FirebaseFirestore.getInstance();

            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.messafe);
            binding.textDateTime.setText(" · " +getReadableDateTime(chatMessage.dateObject));

            if (chatMessage.read != null) {
                binding.textName.setTypeface(null, Typeface.BOLD);
                binding.textRecentMessage.setTextColor(Color.rgb(40,167,241));
                binding.textDateTime.setTextColor(Color.rgb(40,167,241));
            }

            database.collection(Constants.KEY_COLLECTION_USERS).document(chatMessage.conversionId).addSnapshotListener((value, error) -> {
                if (value != null) {
                    if (value.getLong(Constants.KEY_AVAILABLILITY) != null) {
                        int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABLILITY)).intValue();

                        if (availability == 0) {
                            binding.imageOnline.setVisibility(View.GONE);
                        } else {
                            binding.imageOnline.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;

                if (chatMessage.conversations != null) {
                    updateConversion(chatMessage.conversations);
                }

                conversionListener.onConversionClicked(user);
            });
        }
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
    }

    private Bitmap getConversionImage(String encodeImage) {
        byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void updateConversion(String conversionId) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(Constants.KEY_LAST_READ, "1");
    }

}
