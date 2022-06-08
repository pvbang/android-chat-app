package com.example.chatapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.databinding.ItemContainerGroupSearchBinding;
import com.example.chatapplication.databinding.ItemContainerUserSearchBinding;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.User;

import java.util.ArrayList;
import java.util.List;

public class GroupSearchAdapters extends RecyclerView.Adapter<GroupSearchAdapters.SearchViewHolder> implements Filterable {

    private List<User> usersList;
    private List<User> usersListOld;
    private UserListener userListener;

    public GroupSearchAdapters(List<User> usersList, UserListener userListener) {
        this.usersList = usersList;
        this.usersListOld = usersList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerGroupSearchBinding itemContainerUserSearchBinding = ItemContainerGroupSearchBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );

        return new SearchViewHolder(itemContainerUserSearchBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        if (usersList.get(position).id.equals("###########!!~~")) {
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
        ItemContainerGroupSearchBinding binding;

        SearchViewHolder(ItemContainerGroupSearchBinding itemContainerUserSearchBinding) {
            super(itemContainerUserSearchBinding.getRoot());
            binding = itemContainerUserSearchBinding;
        }

        void setSearchData(User user) {
            binding.textName.setVisibility(View.VISIBLE);
            binding.imageProfile.setVisibility(View.VISIBLE);
            binding.textSuggested.setVisibility(View.GONE);
            binding.btnAdd.setVisibility(View.VISIBLE);

            binding.textName.setText(user.name);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));

            binding.btnAdd.setOnClickListener(v -> {

            });

            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }

        void setText() {
            binding.textName.setVisibility(View.GONE);
            binding.imageProfile.setVisibility(View.GONE);
            binding.btnAdd.setVisibility(View.GONE);
            binding.textSuggested.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
