package com.uni.astro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.uni.astro.interfaces.AdapterClickListener;
import com.uni.astro.R;
import java.util.ArrayList;

/**
 * Created by qboxus on 3/19/2019.
 */


public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.CustomViewHolder> {

    ArrayList<String> datalist;
    AdapterClickListener listener;

    public RecentSearchAdapter(ArrayList<String> arrayList, AdapterClickListener listener) {
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recent_search_list, viewGroup, false);
        return new CustomViewHolder(view);
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final RecentSearchAdapter.CustomViewHolder holder, final int i) {
        String item=datalist.get(i);

        holder.nameTxt.setText(item);

        holder.bind(i, item, listener);

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView nameTxt;
        ImageButton deleteBtn;

        public CustomViewHolder(View view) {
            super(view);

            nameTxt = view.findViewById(R.id.name_txt);

            deleteBtn = view.findViewById(R.id.delete_btn);
        }

        public void bind(final int pos, final Object item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {

                listener.onItemClick(v, pos, item);

            });

            deleteBtn.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });
        }


    }


    public void filter(ArrayList<String> filter_list) {
        datalist.clear();
        datalist.addAll(filter_list);
        notifyDataSetChanged();
    }
}

