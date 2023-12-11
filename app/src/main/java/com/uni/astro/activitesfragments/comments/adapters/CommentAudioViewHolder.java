package com.uni.astro.activitesfragments.comments.adapters;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.uni.astro.R;
import com.uni.astro.activitesfragments.comments.CommentF;
import com.uni.astro.models.CommentModel;

public class CommentAudioViewHolder extends RecyclerView.ViewHolder {
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
    public ProgressBar pBar;
    public ImageView notSendMessageIcon;
    public ImageView playBtn;
    public TextView totalTime;
    public SeekBar seekBar;
    public LinearLayout audioBubble;






    public CommentAudioViewHolder(View view) {
        super(view);
        this.audioBubble = view.findViewById(R.id.comment_audio_bubble);
        this.notSendMessageIcon = view.findViewById(R.id.not_send_messsage);
        this.pBar = view.findViewById(R.id.p_bar);
        this.isVerified =view.findViewById(R.id.ivVarified);
        this.tabLikedByCreator=view.findViewById(R.id.tabLikedByCreator);
        this.tvMessageData=view.findViewById(R.id.tvMessageData);
        this.tabMessageReply=view.findViewById(R.id.tabMessageReply);
        this.tabPinned=view.findViewById(R.id.tabPinned);
        this.tabUserPic=view.findViewById(R.id.tabUserPic);
        this.username = view.findViewById(R.id.username);
        this.userPic = view.findViewById(R.id.user_pic);
        this.replyCount = view.findViewById(R.id.reply_count);
        this.likeImage = view.findViewById(R.id.like_image);
        this.messageLayout = view.findViewById(R.id.message_layout);
        this.likeTxt = view.findViewById(R.id.like_txt);
        this.tabCreator=view.findViewById(R.id.tabCreator);
        this.replyRecyclerView = view.findViewById(R.id.reply_recycler_view);
        this.lessLayout = view.findViewById(R.id.less_layout);
        this.showLessTxt = view.findViewById(R.id.show_less_txt);
        this.likeLayout = view.findViewById(R.id.like_layout);
        this.playBtn = view.findViewById(R.id.play_btn);
        this.seekBar = view.findViewById(R.id.seek_bar);
        this.totalTime = view.findViewById(R.id.total_time);
    }

    public void bind(final CommentModel item, int position, final CommentsAdapter.OnItemClickListener listener, final Comments_Reply_Adapter.OnLongClickListener long_listener) {
//item, v, position
        audioBubble.setOnClickListener(v ->
        {
            if(item.isPlaying){
                this.playBtn.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.ic_play_icon));
                this.seekBar.setProgress(0);
            }
            else {
                this.playBtn.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.ic_pause_icon));
                this.seekBar.setProgress(CommentF.getMediaPlayerProgress());
            }
            listener.onItemClick(position, item, v);
        });

        audioBubble.setOnLongClickListener(v -> {
            long_listener.onLongclick(item, v);
            return false;
        });

        seekBar.setOnTouchListener((v, event) -> true);
    }
}
