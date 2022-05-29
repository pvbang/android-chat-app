package com.example.chatapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.databinding.ItemContainerUserBinding;
import com.example.chatapplication.databinding.ItemContainerUserSearchBinding;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchAdapters extends RecyclerView.Adapter<SearchAdapters.SearchViewHolder> implements Filterable {

    private List<User> usersList;
    private List<User> usersListOld;
    private UserListener userListener;

    public SearchAdapters(List<User> usersList, UserListener userListener) {
        this.usersList = usersList;
        this.usersListOld = usersList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserSearchBinding itemContainerUserSearchBinding = ItemContainerUserSearchBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );

        return new SearchViewHolder(itemContainerUserSearchBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        if (usersList.get(position).getName().equals("##################################")) {
            holder.setText();
        } else {
            holder.setSearchData(usersList.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String strSearch = charSequence.toString();
                if (strSearch.isEmpty()) {
                    usersList = usersListOld;
                } else {
                    List<User> list = new ArrayList<>();
                    for (User user : usersListOld) {
                        if (user.getName().toLowerCase().contains(strSearch.toLowerCase()) | user.getEmail().toLowerCase().contains(strSearch.toLowerCase())) {
                            list.add(user);
                        }
                    }
                    usersList = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = usersList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                usersList = (List<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserSearchBinding binding;

        SearchViewHolder(ItemContainerUserSearchBinding itemContainerUserSearchBinding) {
            super(itemContainerUserSearchBinding.getRoot());
            binding = itemContainerUserSearchBinding;
        }

        void setSearchData(User user) {
            binding.textName.setText(user.name);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));

        }

        void setText() {
            binding.textName.setVisibility(View.GONE);
            binding.imageProfile.setVisibility(View.GONE);
            binding.textSuggested.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
