package com.uni.astro.activitesfragments.profile.models;

import com.uni.astro.Constants;
import com.uni.astro.simpleclasses.Variables;

public class CloseFriendModel {
    public String fb_id, username, first_name, last_name, gender, bio;
    public String follow_status_button;
    public boolean is_select, isFollow, isClose;
    public String notificationType;
    private String profile_pic;

    public String getProfile_pic() {
        if (!profile_pic.contains(Variables.http)) {
            profile_pic = Constants.BASE_URL + profile_pic;
        }
        return profile_pic;
    }
    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

}
