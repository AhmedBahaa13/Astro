package com.uni.astro.models;

import com.uni.astro.Constants;
import com.uni.astro.simpleclasses.Variables;

import java.io.Serializable;

/**
 * Created by qboxus on 2/25/2019.
 */

public class NotificationModel implements Serializable {


    public String user_id, username, first_name, last_name,button, effected_fb_id, type;
    public String video_id, created,status,live_streaming_id;

    public String id, string;
    private String video, thum, gif,profile_pic;

    public NotificationModel() {
    }

    public String getVideo() {
        if (!video.contains(Variables.http)) {
            video = Constants.BASE_URL + video;
        }
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getThum() {
        if (!thum.contains(Variables.http)) {
            thum = Constants.BASE_URL + thum;
        }
        return thum;
    }

    public void setThum(String thum) {
        this.thum = thum;
    }

    public String getGif() {
        if (!gif.contains(Variables.http)) {
            gif = Constants.BASE_URL + gif;
        }
        return gif;
    }

    public void setGif(String gif) {
        this.gif = gif;
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
