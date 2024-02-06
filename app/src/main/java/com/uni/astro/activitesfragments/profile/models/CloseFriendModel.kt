package com.uni.astro.activitesfragments.profile.models

import com.uni.astro.Constants
import com.uni.astro.simpleclasses.Variables

data class CloseFriendModel(
    var id: String? = null,
    var username: String? = null,
    var first_name: String? = null,
    var last_name: String? = null,
    var gender: String? = null,
    var bio: String? = null,
    var follow_status_button: String? = null,
    var is_select: Boolean = false,
    var isFollow: Boolean = false,
    var isClose: Boolean = false,
    var notificationType: String? = null,
    private var profile_pic: String? = null
) {
    fun getProfile_pic(): String? {
        if (!profile_pic!!.contains(Variables.http)) {
            profile_pic = Constants.BASE_URL + profile_pic
        }
        return profile_pic
    }

    fun setProfile_pic(profile_pic: String?) {
        this.profile_pic = profile_pic
    }
}