package com.uni.astro.activitesfragments.livestreaming.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.uni.astro.R;
import com.uni.astro.databinding.ItemStreamingViewersBinding;
import com.uni.astro.interfaces.AdapterClickListener;
import com.uni.astro.models.StreamJoinModel;
import com.uni.astro.simpleclasses.Functions;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/19/2019.
 */

public class ViewersAdapter extends RecyclerView.Adapter<ViewersAdapter.CustomViewHolder> {
    public Context context;
    ArrayList<StreamJoinModel> datalist;
    AdapterClickListener adapterClickListener;

    public ViewersAdapter(Context context, ArrayList<StreamJoinModel> arrayList, AdapterClickListener adapterClickListener) {
        this.context = context;
        datalist = arrayList;
        this.adapterClickListener = adapterClickListener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        ItemStreamingViewersBinding binding= DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),R.layout.item_streaming_viewers, viewGroup, false);
        return new CustomViewHolder(binding);
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);
        StreamJoinModel item = (StreamJoinModel) datalist.get(i);

        if (item.getUserPic() != null && !item.getUserPic().equals("")) {
            holder.binding.image.setController(Functions.frescoImageLoad(item.getUserPic(),R.drawable.ic_user_icon,holder.binding.image,false));
        }
        holder.binding.usernameTxt.setText(item.getUserName());

        holder.binding.countTxt.setText(""+(i+1));

        holder.bind(i, item, adapterClickListener);

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ItemStreamingViewersBinding binding;

        public CustomViewHolder(ItemStreamingViewersBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }

        public void bind(final int pos, final Object item, final AdapterClickListener listener) {

            binding.getRoot().setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

        }

    }


}

