package com.uni.astro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.uni.astro.R;
import com.uni.astro.models.FollowingModel;
import com.uni.astro.simpleclasses.Functions;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.CustomViewHolder> {

    public Context context;
    ArrayList<FollowingModel> datalist;
    boolean isSelf;
    String fromFrag;

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, FollowingModel item);
    }

    public OnItemClickListener listener;

    public FollowingAdapter(Context context, boolean isSelf, String fromFrag, ArrayList<FollowingModel> arrayList, OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.isSelf = isSelf;
        this.listener = listener;
        this.fromFrag = fromFrag;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_following, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView userImage;
        TextView userName;
        ImageView icAddFollower;
        ImageView icFollowed;
        RelativeLayout mainlayout;

        public CustomViewHolder(View view) {
            super(view);
            userImage = view.findViewById(R.id.user_image);
            userName = view.findViewById(R.id.user_name);
            icAddFollower = view.findViewById(R.id.ic_add_follower);
            icFollowed = view.findViewById(R.id.ic_followed);
            mainlayout = view.findViewById(R.id.mainlayout);
        }

        public void bind(final int pos, final FollowingModel item, final OnItemClickListener listener) {

            mainlayout.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

            icFollowed.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

            icAddFollower.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

            //region the below comment not used when the design changed

//            actionTxt.setOnClickListener(v -> {
//                listener.onItemClick(v, pos, item);
//
//            });
//
//            ivCross.setOnClickListener(v -> {
//                listener.onItemClick(v, pos, item);
//
//            });
            //endregion
        }
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        FollowingModel item = datalist.get(i);

        holder.userName.setText(item.username);

        holder.userImage.setController(Functions.frescoImageLoad(item.getProfile_pic(), R.drawable.ic_user_icon, holder.userImage, false));

        // region unused code
//        if (isSelf)
//        {
//            if (fromFrag.equalsIgnoreCase("following"))
//            {
//                if (item.notificationType.equalsIgnoreCase("1"))
//                {
//                    holder.ivCross.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_live_notification));
//                }
//                else
//                if (item.notificationType.equalsIgnoreCase("0"))
//                {
//                    holder.ivCross.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_mute_notification));
//                }
//
//            }else
//            if (fromFrag.equalsIgnoreCase("fan"))
//            {
//                holder.ivCross.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_option));
//            }else
//            {
//                holder.ivCross.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_cross));
//            }
//
//        }
        //endregion unused code

        if (item.isFollow) {
            holder.icAddFollower.setVisibility(View.GONE);
            holder.icFollowed.setVisibility(View.VISIBLE);
        } else {
            holder.icFollowed.setVisibility(View.GONE);
            holder.icAddFollower.setVisibility(View.VISIBLE);
        }

        // region not used code
//        holder.userId.setText(item.first_name + " " + item.last_name);
//        holder.actionTxt.setText(item.follow_status_button);
//
//        if (item.follow_status_button != null &&
//                (item.follow_status_button.equalsIgnoreCase("follow") || item.follow_status_button.equalsIgnoreCase("follow back"))) {
//            holder.actionTxt.setBackground(ContextCompat.getDrawable(context, R.drawable.button_rounded_background));
//            holder.actionTxt.setTextColor(ContextCompat.getColor(context, R.color.whiteColor));
//
//        } else if (item.follow_status_button != null &&
//                (item.follow_status_button.equalsIgnoreCase("following") || item.follow_status_button.equalsIgnoreCase("friends"))) {
//            holder.actionTxt.setBackground(ContextCompat.getDrawable(context, R.drawable.d_gray_border));
//            holder.actionTxt.setTextColor(ContextCompat.getColor(context, R.color.black));
//
//        } else if (item.follow_status_button != null && item.follow_status_button.equalsIgnoreCase("0")) {
//            holder.actionTxt.setVisibility(View.GONE);
//        }

        //endregion not used code

        holder.bind(i, datalist.get(i), listener);

    }


}