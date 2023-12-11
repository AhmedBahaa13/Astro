package com.uni.astro.activitesfragments.comments.adapters;


import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.uni.astro.R;
import com.uni.astro.models.CommentModel;
import java.util.ArrayList;

public class CustomViewHolder extends RecyclerView.ViewHolder {

    public TextView username, like_txt, tvMessageData;
    public SocialTextView message;
    public SimpleDraweeView user_pic;
    public ImageView reply_like_image, ivVarified;
    public LinearLayout reply_layout, like_layout, tabLikedByCreator, tabMessageReply, tabCreator;
    public Comments_Reply_Adapter.OnRelyItemCLickListener replyListener;

    public CustomViewHolder(View view, Comments_Reply_Adapter.OnRelyItemCLickListener replyListener) {
        super(view);
        ivVarified = view.findViewById(R.id.ivVarified);
        tvMessageData = view.findViewById(R.id.tvMessageData);
        tabMessageReply = view.findViewById(R.id.tabMessageReply);
        tabLikedByCreator = view.findViewById(R.id.tabLikedByCreator);
        username = view.findViewById(R.id.username);
        user_pic = view.findViewById(R.id.user_pic);
        tabCreator = view.findViewById(R.id.tabCreator);
        message = view.findViewById(R.id.message);
        reply_layout = view.findViewById(R.id.reply_layout);
        reply_like_image = view.findViewById(R.id.reply_like_image);
        like_layout = view.findViewById(R.id.like_layout);
        like_txt = view.findViewById(R.id.like_txt);
        this.replyListener = replyListener;
    }

    public void bind(int postion, ArrayList<CommentModel> datalist, Comments_Reply_Adapter.OnRelyItemCLickListener listener) {

        itemView.setOnClickListener(v -> {
            listener.onItemClick(datalist, postion, v);
        });

        user_pic.setOnClickListener(v -> {
            class TextCommentViewHolder extends RecyclerView.ViewHolder {

                TextView username, message, replyCount, likeTxt, showLessTxt, tvMessageData;
                SimpleDraweeView userPic;
                FrameLayout tabUserPic;
                ImageView likeImage, ivVarified;
                LinearLayout messageLayout, lessLayout, likeLayout, tabCreator, tabMessageReply, tabPinned, tabLikedByCreator;
                RecyclerView replyRecyclerView;

                public TextCommentViewHolder(View view) {
                    super(view);
                    ivVarified = view.findViewById(R.id.ivVarified);
                    tabLikedByCreator = view.findViewById(R.id.tabLikedByCreator);
                    tvMessageData = view.findViewById(R.id.tvMessageData);
                    tabMessageReply = view.findViewById(R.id.tabMessageReply);
                    tabPinned = view.findViewById(R.id.tabPinned);
                    tabUserPic = view.findViewById(R.id.tabUserPic);
                    username = view.findViewById(R.id.username);
                    userPic = view.findViewById(R.id.user_pic);
                    message = view.findViewById(R.id.message);
                    replyCount = view.findViewById(R.id.reply_count);
                    likeImage = view.findViewById(R.id.like_image);
                    messageLayout = view.findViewById(R.id.message_layout);
                    likeTxt = view.findViewById(R.id.like_txt);
                    tabCreator = view.findViewById(R.id.tabCreator);
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
                            listener.onItemLongPress(postion, item, view);
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
            replyListener.onItemClick(datalist, postion, v);
        });

        username.setOnClickListener(v -> {
            replyListener.onItemClick(datalist, postion, v);
        });

        tabMessageReply.setOnClickListener(v -> {
            replyListener.onItemClick(datalist, postion, v);
        });

        like_layout.setOnClickListener(v -> {
            replyListener.onItemClick(datalist, postion, v);
        });
        reply_layout.setOnLongClickListener(view -> {
            replyListener.onItemLongPress(datalist, postion, view);
            return false;
        });
    }
}
