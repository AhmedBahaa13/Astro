package com.uni.astro.models

import com.uni.astro.Constants
import com.uni.astro.simpleclasses.Variables
import java.io.Serializable

data class UserModel(
    var id: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var gender: String? = null,
    var bio: String? = null,
    var website: String? = null,
    var dob: String? = null,
    var social_id: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var password: String? = null,
    var role: String? = null,
    var username: String? = null,
    var socialType: String? = null,
    var deviceToken: String? = null,
    var token: String? = null,
    var active: String? = null,
    var online: String? = null,
    var verified: String? = null,
    var applyVerification: String? = null,
    var referalCode: String? = null,
    var authToken: String? = null,
    var version: String? = null,
    var device: String? = null,
    var ip: String? = null,
    var city: String? = null,
    var country: String? = null,
    var cityId: String? = null,
    var stateId: String? = null,
    var countryId: String? = null,
    var paypal: String? = null,
    var resetWalletDatetime: String? = null,
    var fbId: String? = null,
    var created: String? = null,
    var followersCount: String? = null,
    var followingCount: String? = null,
    var likesCount: String? = null,
    var videoCount: String? = null,
    var notification: String? = null,
    var button: String? = null,
    var profileView: String? = null,
    private var profilePic: String? = null,
    private var profileGif: String? = null,
    var block: String? = null,
    var blockByUser: String? = null,
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var wallet: Long = 0,
    var visitorCount: Long = 0,
    var total_all_time_coins: Long = 0,
    var notificationCount: Long = 0,
    var isSelected: Boolean = false
) : Serializable {
    fun getProfileGif(): String? {
        if (!profileGif!!.contains(Variables.http)) {
            profileGif = Constants.BASE_URL + profileGif
        }
        return profileGif
    }

    fun setProfileGif(profileGif: String?) {
        this.profileGif = profileGif
    }

    fun getProfilePic(): String? {
        if (!profilePic!!.contains(Variables.http)) {
            profilePic = Constants.BASE_URL + profilePic
        }
        return profilePic
    }

    fun setProfilePic(profilePic: String?) {
        this.profilePic = profilePic
    }
}