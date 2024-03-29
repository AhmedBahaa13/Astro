package com.uni.astro.activitesfragments.chat.viewholders;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.uni.astro.R;
import com.uni.astro.activitesfragments.chat.ChatAdapter;
import com.uni.astro.activitesfragments.chat.ChatModel;

public class ChatStoryCommentViewholder extends RecyclerView.ViewHolder {
    public TextView datetxt, messageSeen,storyEmoticon;
    public RelativeLayout tabStory;
    public SimpleDraweeView userStory;
    public View view;

    public ChatStoryCommentViewholder(View itemView) {
        super(itemView);
        view = itemView;
        userStory=view.findViewById(R.id.userStory);
        storyEmoticon = view.findViewById(R.id.storyEmoticon);
        datetxt = view.findViewById(R.id.datetxt);
        messageSeen = view.findViewById(R.id.message_seen);
        tabStory=view.findViewById(R.id.tabStory);
    }

    public void bind(final ChatModel item, final ChatAdapter.OnItemClickListener listener, int position) {
        tabStory.setOnClickListener(v -> {
            listener.onItemClick(item, v,position);
        });
    }
}

