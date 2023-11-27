package com.uni.astro.activitesfragments.spaces.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uni.astro.activitesfragments.spaces.models.HomeUserModel;
import com.uni.astro.databinding.JoinRoomUserItemViewBinding;

import java.util.ArrayList;

public class JoinRoomUserAdapter extends RecyclerView.Adapter<JoinRoomUserAdapter.ViewHolder> {

    ArrayList<HomeUserModel> list;

    public JoinRoomUserAdapter(ArrayList<HomeUserModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        JoinRoomUserItemViewBinding binding = JoinRoomUserItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeUserModel item=list.get(position);

        holder.binding.tvName.setText(""+item.getUserModel().getUsername());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        JoinRoomUserItemViewBinding binding;
        public ViewHolder(@NonNull JoinRoomUserItemViewBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }

    }
}
