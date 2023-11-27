package com.uni.astro.activitesfragments.livestreaming.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.uni.astro.R;
import com.uni.astro.activitesfragments.livestreaming.model.LiveCommentModel;
import com.uni.astro.interfaces.AdapterClickListener;
import com.uni.astro.simpleclasses.Functions;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

public class LiveCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    //comment types
    private static final int PRIMARY_ALERT=1;
    private static final int LIKE_STREAM=2;
    private static final int COMMENT_STREAM=3;
    private static final int GIFT_STREAM=4;

    private static final int SELF_INVITE_FOR_STREAM=6;
    private static final int JOINED_STREAMING=7;

    public Context context;
    private AdapterClickListener listener;
    private ArrayList<LiveCommentModel> dataList;


    public interface OnItemClickListener {
        void onItemClick(int positon, Object item, View view);
    }

    public LiveCommentsAdapter(Context context, ArrayList<LiveCommentModel> dataList, AdapterClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {

        View view;
        if (viewtype==PRIMARY_ALERT)
        {
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_live_primary_alert_layout, viewGroup,false);
            return new AlertViewHolder(view);
        }

        if (viewtype==LIKE_STREAM)
        {
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_live_like_layout, viewGroup,false);
            return new LikeViewHolder(view);
        }

        else if (viewtype==GIFT_STREAM)
        {
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_live_gift_layout, viewGroup,false);
            return new GiftViewHolder(view);
        }

        else if (viewtype==SELF_INVITE_FOR_STREAM)
        {
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_self_join_stream_request_layout, viewGroup,false);
            return new SelfInvitationViewHolder(view);
        }

        else if (viewtype==JOINED_STREAMING) {
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_live_join_comment_layout, viewGroup,false);
            return new JoinedStreamingHolder(view);
        }

        else {
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_live_comment_layout,  viewGroup,false);
            return new CommentViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (dataList.get(position).getType().equalsIgnoreCase("alert"))
        {
            return PRIMARY_ALERT;
        }
        else
        if (dataList.get(position).getType().equalsIgnoreCase("like"))
        {
            return LIKE_STREAM;
        }
        else
        if (dataList.get(position).getType().equalsIgnoreCase("gift"))
        {
            return GIFT_STREAM;
        }

        else if (dataList.get(position).getType().equalsIgnoreCase("joined")) {
            return JOINED_STREAMING;
        }
        else
        if (dataList.get(position).getType().equalsIgnoreCase("selfInviteForStream"))
        {
            return SELF_INVITE_FOR_STREAM;
        }
        else

            {
                return COMMENT_STREAM;
            }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {
        final LiveCommentModel item = (LiveCommentModel) dataList.get(i);

        if (holder instanceof CommentViewHolder)
        {
            CommentViewHolder holderItem= (CommentViewHolder) holder;
            holderItem.username.setText(item.getUserName());

            holderItem.message.setText(item.getComment());
            holderItem.userPic.setController(Functions.frescoImageLoad(item.getUserPicture(),holderItem.userPic,false));

            holderItem.bind(i, item, listener);

        }
        else if (holder instanceof JoinedStreamingHolder)
        {
            JoinedStreamingHolder holderItem= (JoinedStreamingHolder) holder;
            holderItem.username.setText(item.getUserName());
            holderItem.userPic.setController(Functions.frescoImageLoad(item.getUserPicture(),holderItem.userPic,false));

            holderItem.bind(i, item, listener);

        }
        else
        if (holder instanceof LikeViewHolder)
        {
            LikeViewHolder holderItem= (LikeViewHolder) holder;
            holderItem.tvTitle.setText(item.getComment());

            holderItem.bind(i, item, listener);

        }
        else
        if (holder instanceof GiftViewHolder)
        {
            GiftViewHolder holderItem= (GiftViewHolder) holder;
            String[] str=item.getComment().split("=====");
            holderItem.tvTitle.setText(item.getUserName()+" "+(context.getString(R.string.send).toLowerCase())+" X "+str[0]+" "+str[1]);
            Uri imageUri = Uri.parse(str[2]);
            holderItem.ivGift.setController(Functions.frescoImageLoad(imageUri+"",holderItem.ivGift,false));
            holderItem.bind(i, item, listener);

        }

        else
        if (holder instanceof SelfInvitationViewHolder)
        {
            SelfInvitationViewHolder holderItem= (SelfInvitationViewHolder) holder;
            holderItem.tvName.setText(item.getUserName()+" "+holderItem.itemView.getContext().getString(R.string.want_to_join_live_stream));
            holderItem.ivProfile.setController(Functions.frescoImageLoad(item.getUserPicture(),holderItem.ivProfile,false));
            holderItem.bind(i, item, listener);

        }
        else
        if (holder instanceof AlertViewHolder)
        {
            AlertViewHolder holderItem= (AlertViewHolder) holder;
            holderItem.tvTitle.setText(item.getComment());

            holderItem.bind(i, item, listener);

        }

    }


    private class CommentViewHolder extends RecyclerView.ViewHolder {

        TextView username, message;
        SimpleDraweeView userPic;

        public CommentViewHolder(View view) {
            super(view);

            username = view.findViewById(R.id.postType);
            userPic = view.findViewById(R.id.user_pic);
            message = view.findViewById(R.id.message);

        }

        public void bind(final int postion, final LiveCommentModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, postion, item);

            });

        }

    }


    private class JoinedStreamingHolder extends RecyclerView.ViewHolder {

        TextView username;
        SimpleDraweeView userPic;

        public JoinedStreamingHolder(View view) {
            super(view);

            username = view.findViewById(R.id.username);
            userPic = view.findViewById(R.id.user_pic);

        }

        public void bind(final int postion, final LiveCommentModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, postion, item);

            });

        }

    }



    private class LikeViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;

        public LikeViewHolder(View view) {
            super(view);

            tvTitle = view.findViewById(R.id.tvTitle);
        }

        public void bind(final int postion, final LiveCommentModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, postion, item);

            });

        }

    }

    private class GiftViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        SimpleDraweeView ivGift;

        public GiftViewHolder(View view) {
            super(view);

            tvTitle = view.findViewById(R.id.tvTitle);
            ivGift=view.findViewById(R.id.ivGift);
        }

        public void bind(final int postion, final LiveCommentModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, postion, item);

            });

        }

    }


    private class AlertViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;

        public AlertViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
        }

        public void bind(final int postion, final LiveCommentModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, postion, item);

            });

        }

    }



    private class SelfInvitationViewHolder extends RecyclerView.ViewHolder {

        View tabAcceptInvitation;
        TextView tvName;
        SimpleDraweeView ivProfile;

        public SelfInvitationViewHolder(View view) {
            super(view);
            tabAcceptInvitation = view.findViewById(R.id.tabAcceptInvitation);
            tvName=view.findViewById(R.id.tvName);
            ivProfile=view.findViewById(R.id.ivProfile);
        }

        public void bind(final int postion, final LiveCommentModel item, final AdapterClickListener listener) {

            tabAcceptInvitation.setOnClickListener(v -> {
                listener.onItemClick(v, postion, item);

            });

        }

    }

}