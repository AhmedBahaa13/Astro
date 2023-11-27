package com.uni.astro.models;

import com.uni.astro.Constants;
import com.uni.astro.simpleclasses.Variables;

import java.io.Serializable;

public class UsersModel implements Serializable {

    public String fb_id, username, first_name, last_name, gender,
     videos, followers_count;
    private String profile_pic;
    public boolean isSelected=false;

    public UsersModel() {
    }

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
