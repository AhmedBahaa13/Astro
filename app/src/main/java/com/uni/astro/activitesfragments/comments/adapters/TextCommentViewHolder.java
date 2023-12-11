package com.uni.astro.activitesfragments.comments.adapters;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.uni.astro.R;
import com.uni.astro.models.CommentModel;

public class TextCommentViewHolder extends RecyclerView.ViewHolder {

    public TextView message;
    public TextView username;
    public TextView replyCount;
    public TextView likeTxt;
    public TextView showLessTxt;
    public TextView tvMessageData;
    public SimpleDraweeView userPic;
    public FrameLayout tabUserPic;
    public ImageView likeImage, isVerified;
    public LinearLayout messageLayout, lessLayout, likeLayout,tabCreator,tabMessageReply,tabPinned,tabLikedByCreator;
    public RecyclerView replyRecyclerView;

    public TextCommentViewHolder(View view) {
        super(view);
        message = view.findViewById(R.id.message);
        isVerified =view.findViewById(R.id.ivVarified);
        tabLikedByCreator=view.findViewById(R.id.tabLikedByCreator);
        tvMessageData=view.findViewById(R.id.tvMessageData);
        tabMessageReply=view.findViewById(R.id.tabMessageReply);
        tabPinned=view.findViewById(R.id.tabPinned);
        tabUserPic=view.findViewById(R.id.tabUserPic);
        username = view.findViewById(R.id.username);
        userPic = view.findViewById(R.id.user_pic);
        replyCount = view.findViewById(R.id.reply_count);
        likeImage = view.findViewById(R.id.like_image);
        messageLayout = view.findViewById(R.id.message_layout);
        likeTxt = view.findViewById(R.id.like_txt);
        tabCreator=view.findViewById(R.id.tabCreator);
        replyRecyclerView = view.findViewById(R.id.reply_recycler_view);
        lessLayout = view.findViewById(R.id.less_layout);
        showLessTxt = view.findViewById(R.id.show_less_txt);
        likeLayout = view.findViewById(R.id.like_layout);
    }

    public void bind(int postion, CommentModel item, CommentsAdapter.OnItemClickListener listener) {

        itemView.setOnClickListener(v -> {
            listener.onItemClick(postion, item, v);
        });
        tabUserPic.setOnClickListener(view -> {
            listener.onItemClick(postion, item, view);
        });
        userPic.setOnClickListener(v -> {
            listener.onItemClick(postion, item, v);
        });
        username.setOnClickListener(v -> {
            listener.onItemClick(postion, item, v);
        });

        messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onItemLongPress(postion,item,view);
                return false;
            }
        });

        likeLayout.setOnClickListener(v -> {
            listener.onItemClick(postion, item, v);
        });
        tabMessageReply.setOnClickListener(v -> {
            listener.onItemClick(postion, item, v);
        });
        replyCount.setOnClickListener(v -> {
            listener.onItemClick(postion, item, v);
        });
        showLessTxt.setOnClickListener(v -> {
            listener.onItemClick(postion, item, v);
        });



    }


}
