package com.uni.astro.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.uni.astro.R;
import com.uni.astro.activitesfragments.profile.models.CloseFriendModel;
import com.uni.astro.simpleclasses.Functions;

import java.util.ArrayList;
import java.util.List;

public class CloseFriendsAdapter extends RecyclerView.Adapter<CloseFriendsAdapter.CloseFriendsViewHolder> {
    List<CloseFriendModel> usersList = new ArrayList<>();
    Context context;
    OnFriendClickListener listener;

    public CloseFriendsAdapter(Context context, OnFriendClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CloseFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CloseFriendsViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.close_friend_item, parent, false),
                listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CloseFriendsViewHolder holder, int position) {
        holder.userPic.setController(
                Functions.frescoImageLoad(
                        usersList.get(position).getProfile_pic(),
                        holder.userPic,
                        false
                )
        );
        holder.userName.setText(
                context.getString(
                        R.string.user_name, usersList.get(position).first_name, usersList.get(position).last_name
                )
        );

        holder.userUserName.setText(
                usersList.get(position).username
        );
        holder.radioButton.setChecked(usersList.get(position).isClose);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setUsersList(List<CloseFriendModel> users) {
        this.usersList = users;
        notifyDataSetChanged();
    }

    public static class CloseFriendsViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView userPic;
        TextView userName;
        TextView userUserName;
        RadioButton radioButton;

        public CloseFriendsViewHolder(@NonNull View itemView, OnFriendClickListener listener) {
            super(itemView);
            this.userPic = itemView.findViewById(R.id.user_profile_pic);
            this.userName = itemView.findViewById(R.id.user_name);
            this.userUserName = itemView.findViewById(R.id.user_user_name);
            this.radioButton = itemView.findViewById(R.id.radioButton);

            this.radioButton.setOnClickListener((view) ->
                    listener.onFriendClick(getAbsoluteAdapterPosition())
            );
        }
    }

    public interface OnFriendClickListener {
        void onFriendClick(int position);
    }
}
