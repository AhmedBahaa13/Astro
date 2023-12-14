package com.uni.astro.models;

import com.uni.astro.Constants;
import com.uni.astro.simpleclasses.Variables;

import java.util.ArrayList;


public class CommentModel {
    public String video_id, videoOwnerId, userId, user_name, first_name, last_name, commentText, created, type;
    public String comment_id, pin_comment_id, isLikedByOwner, isVerified;
    public String liked;
    public String like_count;
    public String item_count_replies;
    public ArrayList<CommentModel> arrayList;
    public boolean isExpand;
    public boolean isPlaying = false;
    public String comment_reply_id, comment_reply, reply_create_date, arraylist_size, replay_user_name, parent_comment_id;
    public String comment_reply_liked, reply_liked_count;

    private String profile_pic, replay_user_url;

    public CommentModel() { }

    public String getProfile_pic() {
        if (!profile_pic.contains(Variables.http)) {
            profile_pic = Constants.BASE_URL + profile_pic;
        }
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getReplay_user_url() {
        if (!replay_user_url.contains(Variables.http)) {
            replay_user_url = Constants.BASE_URL + replay_user_url;
        }
        return replay_user_url;
    }

    public void setReplay_user_url(String replay_user_url) {
        this.replay_user_url = replay_user_url;
    }
}
