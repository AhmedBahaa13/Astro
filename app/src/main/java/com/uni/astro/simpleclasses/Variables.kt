package com.uni.astro.simpleclasses

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Locale


object Variables {
    const val DEVICE = "android"

    //default video height is 620X1102
    const val videoHeight = 1102
    const val videoWidth = 620
    const val SelectedAudio_AAC = "SelectedAudio.aac"
    const val APP_STORY_EDITED_FOLDER = "Hided/"
    const val APP_OUTPUT_FOLDER = "Output/"
    const val APP_HIDED_FOLDER = "HiddenAstro/"
    const val APP_HIDED_RESULT_FOLDER = "HiddenResultAstro/"
    const val DRAFT_APP_FOLDER = "Draft/"
    const val onlineUser = "OnlineUsers"
    const val LiveStreaming = "LiveStreamingUsers"
    const val SETTING_PREF_NAME = "SETTING_PREF_NAME"
    const val PREF_NAME = "pref_name"
    const val U_ID = "u_id"
    const val U_WALLET = "u_wallet"
    const val U_total_coins_all_time = "U_total_coins_all_time"
    const val U_PAYOUT_ID = "u_payout_id"
    const val U_NAME = "u_name"
    const val U_PHONE_NO = "Phone_No"
    const val U_EMAIL = "User_Email"
    const val U_SOCIAL_ID = "Social_Id"
    const val IS_VERIFIED = "is_verified"
    const val IS_VERIFICATION_APPLY = "is_verification_apply"
    const val U_PIC = "u_pic"
    const val U_GIF = "u_gif"
    const val U_PROFILE_VIEW = "u_profile_view"
    const val F_NAME = "f_name"
    const val L_NAME = "l_name"
    const val GENDER = "u_gender"
    const val U_BIO = "U_bio"
    const val U_LINK = "U_link"
    const val REFERAL_CODE = "referal_code"
    const val IS_LOGIN = "is_login"
    const val DEVICE_TOKEN = "device_token"
    const val DEVICE_IP = "device_ip"
    const val DEVICE_LAT = "device_lat"
    const val DEVICE_LNG = "device_lng"
    const val AUTH_TOKEN = "api_token"
    const val DEVICE_ID = "device_id"
    const val UPLOADING_VIDEO_THUMB = "uploading_video_thumb"
    const val APP_LANGUAGE = "app_language"
    const val APP_LANGUAGE_CODE = "app_language_code"
    const val DEFAULT_LANGUAGE_CODE = "en"
    const val DEFAULT_LANGUAGE = "English"
    const val countryRegion = "countryRegion"
    const val IsExtended = "IsExtended"
    const val IsPrivacyPolicyAccept = "IsPrivacyPolicyAccept"
    const val ShowAdvertAfter = "show_advert_after"
    const val CoinWorth = "coin_worth"
    const val AddType = "add_type"

    //Paper DB collection
    const val MultiAccountKey = "Accounts"
    const val PrivacySetting = "Setting"
    const val PrivacySettingModel = "PrivacySettingModel"
    const val PushSettingModel = "PushSettingModel"
    const val PromoAds = "Promo"
    const val PromoAdsModel = "ads"
    const val GIF_FIRSTPART = "https://media.giphy.com/media/"
    const val GIF_SECONDPART = "/100w.gif"
    const val http = "http"
    const val https = "https"

    @JvmField
    val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ", Locale.ENGLISH)

    @JvmField
    val df2 = SimpleDateFormat("dd-MM-yyyy HH:mmZZ", Locale.ENGLISH)

    const val roomKey = "LiveRoom"
    const val roomInvitation = "RoomInvitation"
    const val roomchat = "RoomChat"
    const val roomUsers = "Users"
    const val roomUserWave = "UserWave"
    const val joinedKey = "Joined"
    const val ACTION_KEY_CHANNEL_NAME = "ecHANEL"

    @JvmField
    var outputfile2 = APP_HIDED_FOLDER + "output2.mp4"

    @JvmField
    var videoChunk = APP_HIDED_FOLDER + "videoChunk"

    @JvmField
    var output_filter_file = APP_HIDED_FOLDER + "output-filtered.mp4"

    @JvmField
    var gallery_trimed_video = APP_HIDED_FOLDER + "gallery_trimed_video.mp4"

    @JvmField
    var gallery_resize_video = APP_HIDED_FOLDER + "gallery_resize_video.mp4"

    @JvmField
    var sharedPreferences: SharedPreferences? = null

    @JvmField
    var settingsPreferences: SharedPreferences? = null

    @JvmField
    var selectedSoundId = "null"

    @JvmField
    var reloadMyVideos = false

    @JvmField
    var reloadMyVideosInner = false

    @JvmField
    var reloadMyLikesInner = false

    @JvmField
    var reloadMyNotification = false

    //this variable is used to handle compression between gallery and camera upload video
    @JvmField
    var isCompressionApplyOnStart = false

    @JvmField
    var VideoDirectory = "Videos"

    @JvmField
    var liked_your_video_en = "liked your video"

    @JvmField
    var has_posted_a_video_en = "has posted a video"

    @JvmField
    var started_following_you_en = "started following you"

    @JvmField
    var commented_en = "commented:"

    @JvmField
    var is_live_now_en = "is live now"

    @JvmField
    var mentioned_you_in_a_comment_en = "mentioned you in a comment:"

    @JvmField
    var replied_to_your_comment_en = "replied to your comment:"

    @JvmField
    var liked_your_video_ar = "قام بالإعجاب على الفيديو الخاص بك"

    @JvmField
    var has_posted_a_video_ar = "نشر مقطع فيديو"

    @JvmField
    var started_following_you_ar = "بدأ بمتابعتك"

    @JvmField
    var commented_ar = "علق:"

    @JvmField
    var is_live_now_ar = "في بث مباشر الآن"

    @JvmField
    var mentioned_you_in_a_comment_ar = "ذكرك في تعليق:"

    @JvmField
    var replied_to_your_comment_ar = "رد على تعليقك:"

    //current follower list
    @JvmField
    var followMapList = HashMap<String, String>()
}