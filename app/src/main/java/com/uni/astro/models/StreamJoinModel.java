package com.uni.astro.models;

import com.uni.astro.Constants;
import com.uni.astro.simpleclasses.Variables;

import java.io.Serializable;

public class StreamJoinModel implements Serializable {
    public String userId,userName;
    private String userPic;

    public StreamJoinModel() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPic() {
        if (!userPic.contains(Variables.http)) {
            userPic = Constants.BASE_URL + userPic;
        }
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }
}
