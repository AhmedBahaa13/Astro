package com.uni.astro.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.uni.astro.interfaces.AdapterClickListener;
import com.uni.astro.R;
import com.uni.astro.databinding.StoryEmojiItemViewBinding;

import java.util.ArrayList;

public class StoryEmojiAdapter extends RecyclerView.Adapter<StoryEmojiAdapter.CustomViewHolder> {
    ArrayList<String> list;
    private AdapterClickListener listener;


    public StoryEmojiAdapter(ArrayList<String> datalist, AdapterClickListener listener) {
        this.list = datalist;
        this.listener = listener;

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        StoryEmojiItemViewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),R.layout.story_emoji_item_view, viewGroup,false);
        return new CustomViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        String item=list.get(i);

        try{
            holder.binding.tvEmoji.setText(item);
        }
        catch (Exception e)
        {
            holder.binding.tvEmoji.setText("");
        }

        holder.bind(i,item, listener);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        StoryEmojiItemViewBinding binding;

        public CustomViewHolder(StoryEmojiItemViewBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }

        public void bind(int position,String item,AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v,position,item);
            });


        }

    }
}