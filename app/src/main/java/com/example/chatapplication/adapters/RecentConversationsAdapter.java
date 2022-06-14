package com.example.chatapplication.adapters;

import static android.content.ContentValues.TAG;

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
import com.example.chatapplication.databinding.ItemContainerRecentGroupBinding;
import com.example.chatapplication.fragments.mainfrag.MainFragment;
import com.example.chatapplication.listeners.ConversionListener;
import com.example.chatapplication.listeners.GroupListener;
import com.example.chatapplication.models.ChatMessage;
import com.example.chatapplication.models.Group;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class RecentConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    private FirebaseFirestore database;
    private final List<Group> groupsList;
    private final GroupListener groupListener;

    public static final int VIEW_TYPE_CONVERSIONS = 1;
    public static final int VIEW_TYPE_GROUPS = 2;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener, List<Group> groupsList, GroupListener groupListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
        this.groupsList = groupsList;
        this.groupListener = groupListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CONVERSIONS) {
            return new ConversionViewHolder(
                    ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            return new UserViewHolder(
                    ItemContainerRecentGroupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_CONVERSIONS) {
            ((RecentConversationsAdapter.ConversionViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((RecentConversationsAdapter.UserViewHolder) holder).setUserData(groupsList.get(position - chatMessages.size()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= chatMessages.size()-1) {
            return VIEW_TYPE_CONVERSIONS;
        } else {
            return VIEW_TYPE_GROUPS;
        }
    }

    @Override
    public int getItemCount() {
        return groupsList.size() + chatMessages.size();
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
                if (chatMessage.conversations != null) {
                    updateConversion(chatMessage.conversations);
                }
                database.collection(Constants.KEY_COLLECTION_USERS).document(chatMessage.conversionId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                setValueEmail(document.getString(Constants.KEY_EMAIL), chatMessage);
                            } else {}
                        } else {}
                    }
                });
            });
        }
    }

    private void setValueEmail(String email, ChatMessage chatMessage) {
        User user = new User();
        user.id = chatMessage.conversionId;
        user.name = chatMessage.conversionName;
        user.image = chatMessage.conversionImage;
        user.email = email;

        conversionListener.onConversionClicked(user);
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



    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentGroupBinding binding;

        UserViewHolder(ItemContainerRecentGroupBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(Group group) {
            binding.textName.setText("Nhóm: " +group.name);
            binding.imageProfile1.setImageBitmap(getGroupImage(group.image1));
            binding.imageProfile2.setImageBitmap(getGroupImage(group.image2));
            binding.textRecentMessage.setText(group.message);
            binding.textDateTime.setText(" · " +group.time);

            binding.getRoot().setOnClickListener(v -> groupListener.onGroupClicked(group));
        }
    }

    private Bitmap getGroupImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
