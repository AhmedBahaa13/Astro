package com.uni.astro.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.uni.astro.Constants;
import com.uni.astro.R;
import com.uni.astro.databinding.PhotoUploadItemViewBinding;
import com.uni.astro.interfaces.AdapterClickListener;
import com.uni.astro.simpleclasses.Functions;

import java.io.File;
import java.util.ArrayList;

public class PhotoUploadAdapter extends RecyclerView.Adapter<PhotoUploadAdapter.CustomViewHolder> {

    ArrayList<String> list;
    AdapterClickListener adapterClickListener;

    public PhotoUploadAdapter(ArrayList<String> list, AdapterClickListener adapterClickListener) {
        this.list = list;
        this.adapterClickListener = adapterClickListener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        PhotoUploadItemViewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),R.layout.photo_upload_item_view, viewGroup,false);
        return new CustomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {

        String item = list.get(i);
        Log.d(Constants.TAG_,"item: "+item);
        holder.binding.ivUploadPhoto.setController(Functions.frescoImageLoad(Uri.fromFile(new File(item)),R.drawable.ic_upload_photo,holder.binding.ivUploadPhoto));

        holder.bind(i, item, adapterClickListener);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        PhotoUploadItemViewBinding binding;

        public CustomViewHolder(PhotoUploadItemViewBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }

        public void bind(final int pos, final Object item,
                         final AdapterClickListener adapterClickListener) {
            binding.ivDeletePhoto.setOnClickListener(v -> {
                adapterClickListener.onItemClick(v, pos, item);

            });
        }

    }

}