package com.uni.astro.activitesfragments.profile

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.uni.astro.Constants
import com.uni.astro.R
import com.uni.astro.activitesfragments.WebviewA
import com.uni.astro.activitesfragments.accounts.LoginA
import com.uni.astro.activitesfragments.accounts.ManageAccountsF
import com.uni.astro.activitesfragments.chat.ChatA
import com.uni.astro.activitesfragments.profile.followtabs.NotificationPriorityF
import com.uni.astro.activitesfragments.profile.likedvideos.LikedVideoF
import com.uni.astro.activitesfragments.profile.privatevideos.PrivateVideoF
import com.uni.astro.activitesfragments.profile.usersstory.ViewStoryA
import com.uni.astro.activitesfragments.profile.uservideos.UserVideoF
import com.uni.astro.adapters.SuggestionAdapter
import com.uni.astro.adapters.ViewPagerAdapter
import com.uni.astro.apiclasses.ApiLinks
import com.uni.astro.databinding.ActivityProfileBinding
import com.uni.astro.databinding.ItemReportUserDialogBinding
import com.uni.astro.databinding.LayoutOtherProfileBottomTabsBinding
import com.uni.astro.databinding.ShowLikesAlertPopupDialogBinding
import com.uni.astro.models.FollowingModel
import com.uni.astro.models.PrivacyPolicySettingModel
import com.uni.astro.models.PushNotificationSettingModel
import com.uni.astro.models.StoryModel
import com.uni.astro.models.StoryVideoModel
import com.uni.astro.simpleclasses.AppCompatLocaleActivity
import com.uni.astro.simpleclasses.DataParsing
import com.uni.astro.simpleclasses.DebounceClickHandler
import com.uni.astro.simpleclasses.Functions
import com.uni.astro.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import org.json.JSONObject
import java.util.Locale

class ProfileA : AppCompatLocaleActivity() {
    private lateinit var bind: ActivityProfileBinding

    private lateinit var bottomTabsBind: LayoutOtherProfileBottomTabsBinding

    private val tabIcons = intArrayOf(
        R.drawable.ic_videos,
        R.drawable.ic_like_list
    )

    private var isdataload = false
    private var isDirectMessage = false
    private var isLikeVideoShow = false
    private var picUrl: String? = null
    private var profileGif: String? = null
    private var followerCount: String? = null
    private var followingCount: String? = null

    var userId: String? = null
    var userName: String? = null
    var fullName: String? = null
    var buttonStatus: String? = null
    var userPic: String? = null
    private var totalLikes = ""

    var adapterSuggestion: SuggestionAdapter? = null
    var notificationType: String? = "1"
    private var isUserAlreadyBlock = "0"
    private var blockByUserId = "0"
    private var rootref: DatabaseReference? = null
    private var fragmentUserVides: UserVideoF? = null
    private var fragmentLikesVides: LikedVideoF? = null
    private var storyDataList = ArrayList<StoryModel>()
    private var isSuggestion = true

    var suggestionList = ArrayList<FollowingModel>()

    // get the profile details of user
    private var isRunFirstTime = false

    private var adapter: ViewPagerAdapter? = null

    var resultCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                updateProfile()
            }
        }
    }

    private var resultFollowCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                callApiForGetAllvideos()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Functions.setLocale(
            Functions.getSharedPreference(this@ProfileA).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE), this, javaClass, false
        )
        bind = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bind.root)

        if (intent.hasExtra("user_id") && intent.hasExtra("user_name") && intent.hasExtra("user_pic")) {
            userId = intent.getStringExtra("user_id")
            userName = intent.getStringExtra("user_name")
            userPic = intent.getStringExtra("user_pic")
        } else {
            userName = intent.getStringExtra("user_name")
        }


        bottomTabsBind = bind.bottomTabs


        bottomTabsBind.apply {
            setupViewPager(pager)
            TabLayoutMediator(tabs, pager) { _, _ -> }.attach()
            setupTabIcons()
            pager.offscreenPageLimit = 2
        }


        rootref = FirebaseDatabase.getInstance().reference
        isdataload = true

        callApiForGetAllvideos()
        setUpSuggestionRecyclerview()

        bind.apply {
            circleStatusBar.strokeLineColor = getColor(R.color.colorAccent)

            messageBtn.setOnClickListener { openChatF() }
            backBtn.setOnClickListener(DebounceClickHandler { onBackPressed() })
            menuBtn.setOnClickListener(DebounceClickHandler { showVideoOption() })
            fansLayout.setOnClickListener(DebounceClickHandler { openFollowers() })
            favBtn.setOnClickListener(DebounceClickHandler { openFavouriteVideos() })
            followingLayout.setOnClickListener(DebounceClickHandler { openFollowing() })
            editProfileBtn.setOnClickListener(DebounceClickHandler { openEditProfile() })
            tabPrivacyLikes.setOnClickListener(DebounceClickHandler { showMyLikesCounts() })
            tabAllSuggestion.setOnClickListener(DebounceClickHandler { openSuggestionScreen() })
            notificationBtn.setOnClickListener(DebounceClickHandler { selectNotificationPriority() })

            userImage.setOnClickListener(DebounceClickHandler {
                if (circleStatusBar.visibility == View.VISIBLE) {
                    openStoryView()
                } else {
                    openProfileShareTab()
                }
            })

            tabLink.setOnClickListener(DebounceClickHandler {
                openWebUrl(
                    getString(R.string.web_browser),
                    tvLink.text.toString()
                )
            })

            suggestionBtn.setOnClickListener(DebounceClickHandler {
                isSuggestion = if (isSuggestion) {
                    suggestionBtn.animate().rotation(180f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            tabSuggestion.visibility = View.VISIBLE
                            if (suggestionList.isEmpty()) {
                                showLoadingProgressSuggestionButton()
                                suggestionUserList
                            }
                        }
                    }).start()
                    false

                } else {
                    suggestionBtn.animate().rotation(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            tabSuggestion.visibility = View.GONE
                        }
                    }).start()
                    true
                }
            })

            unFriendBtn.setOnClickListener(DebounceClickHandler {
                if (Functions.checkLoginUser(this@ProfileA)) followUnFollowUser()
            })

            tvFollowBtn.setOnClickListener(DebounceClickHandler {
                if (Functions.checkLoginUser(this@ProfileA)) {
                    followUnFollowUser()
                }
            })
        }


        bottomTabsBind.apply {
            tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    pager.setCurrentItem(tab.position, true)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) { }

                override fun onTabReselected(tab: TabLayout.Tab) { }
            })


            pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tabs.getTabAt(position)?.select()
                }
            })
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        fragmentUserVides = UserVideoF.newInstance(false, userId, userName, isUserAlreadyBlock)
        fragmentLikesVides = LikedVideoF.newInstance(false, userId, userName, isLikeVideoShow, isUserAlreadyBlock)

        val adapter2 = ViewPagerAdapter(this)
        adapter2.addFragment(fragmentUserVides!!, getString(R.string.my_videos))
        adapter2.addFragment(fragmentLikesVides!!, getString(R.string.liked_videos))
        viewPager.adapter = adapter2
    }
    private fun setupTabIcons() {
        bottomTabsBind.apply {
            tabs.getTabAt(0)?.setIcon(tabIcons[0])
            tabs.getTabAt(1)?.setIcon(tabIcons[1])
        }
    }

    internal class ViewPagerAdapter(fmanager: FragmentActivity) : FragmentStateAdapter(fmanager) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getItemCount(): Int = mFragmentList.size

        override fun createFragment(position: Int): Fragment = mFragmentList[position]
    }


    private fun showVideoOption() {
        val alertDialog = Dialog(this@ProfileA)
        val dialogBind = ItemReportUserDialogBinding.inflate(layoutInflater)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(dialogBind.root)
        Log.d(Constants.TAG_, "blockObj: $blockByUserId")
        Log.d(Constants.TAG_, "isUserAlreadyBlock: $isUserAlreadyBlock")

        dialogBind.apply {
            if (blockByUserId == Functions.getSharedPreference(this@ProfileA).getString(Variables.U_ID, "")) {
                tabBlockUser.visibility = View.VISIBLE
            } else {
                if (isUserAlreadyBlock == "1") {
                    tabBlockUser.visibility = View.GONE
                } else {
                    tabBlockUser.visibility = View.VISIBLE
                }
            }

            if (isUserAlreadyBlock == "1")
                tvBlockUser.text = getString(R.string.unblock_user)
            else
                tvBlockUser.text = getString(R.string.block_user)

            tabShareProfile.setOnClickListener { v: View? ->
                alertDialog.dismiss()
                if (Functions.checkLoginUser(this@ProfileA)) {
                    shareProfile()
                }
            }

            tabReportUser.setOnClickListener { v: View? ->
                alertDialog.dismiss()
                if (Functions.checkLoginUser(this@ProfileA)) {
                    openUserReport()
                }
            }

            tabBlockUser.setOnClickListener { v: View? ->
                alertDialog.dismiss()
                if (Functions.checkLoginUser(this@ProfileA)) {
                    openBlockUserDialog()
                }
            }
        }
        alertDialog.show()
    }

    private fun openUserReport() {
        val intent = Intent(this@ProfileA, ReportTypeA::class.java)
        intent.putExtra("user_id", userId)
        intent.putExtra("isFrom", false)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private fun showLoadingProgressSuggestionButton() {
        val request = ImageRequestBuilder.newBuilderWithResourceId(R.raw.ic_progress_animation)
            .build()

        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(bind.suggestionBtn.controller)
            .setAutoPlayAnimations(true)
            .build()
        bind.suggestionBtn.controller = controller
    }

    private fun openChatF() {
        val intent = Intent(this@ProfileA, ChatA::class.java)
        intent.putExtra("user_id", userId)
        intent.putExtra("user_name", userName)
        intent.putExtra("user_pic", userPic)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openProfileShareTab() {
        var isGif = false
        val mediaURL: String?
        if (profileGif != null && profileGif != Constants.BASE_URL) {
            isGif = true
            mediaURL = profileGif
        } else {
            isGif = false
            mediaURL = picUrl
        }
        val fragment = ShareAndViewProfileF(isGif, mediaURL, userId) { bundle ->
            if (bundle.getString("action") == "profileShareMessage") {
                if (Functions.checkLoginUser(this@ProfileA)) {
                    // firebase sharing
                }
            }
        }
        fragment.show(supportFragmentManager, "")
    }

    // open the favourite videos fragment
    private fun openFavouriteVideos() {
        val intent = Intent(this@ProfileA, FavouriteMainA::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun openEditProfile() {
        val intent = Intent(this@ProfileA, EditProfileA::class.java)
        resultCallback.launch(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    // place the profile data
    private fun updateProfile() {
        bind.username2Txt.text = Functions.showUsername(
            Functions.getSharedPreference(this).getString(Variables.U_NAME, "")
        )

        val firstName = Functions.getSharedPreference(this).getString(Variables.F_NAME, "")
        val lastName = Functions.getSharedPreference(this).getString(Variables.L_NAME, "")

        if (firstName.equals("", ignoreCase = true) && lastName.equals("", ignoreCase = true)) {
            bind.username.text = Functions.getSharedPreference(this)
                .getString(Variables.U_NAME, "")
        } else {
            bind.username.text = "$firstName $lastName"
        }

        if (TextUtils.isEmpty(Functions.getSharedPreference(this).getString(Variables.U_BIO, ""))) {
            bind.tvBio.visibility = View.GONE
        } else {
            bind.tvBio.visibility = View.VISIBLE
            bind.tvBio.text = Functions.getSharedPreference(this).getString(Variables.U_BIO, "")
        }

        if (TextUtils.isEmpty(Functions.getSharedPreference(this).getString(Variables.U_LINK, ""))) {
            bind.tabLink.visibility = View.GONE
        } else {
            bind.tabLink.visibility = View.VISIBLE
            bind.tvLink.text = Functions.getSharedPreference(this).getString(Variables.U_LINK, "")
        }

        picUrl = Functions.getSharedPreference(this).getString(Variables.U_PIC, "")
        profileGif = Functions.getSharedPreference(this).getString(Variables.U_GIF, "")

        if (profileGif == Constants.BASE_URL) {
            bind.userImage.controller = Functions.frescoImageLoad(picUrl, bind.userImage, false)
        } else {
            bind.userImage.controller = Functions.frescoImageLoad(
                profileGif,
                R.drawable.ic_user_icon,
                bind.userImage,
                true
            )
        }
    }

    private fun selectNotificationPriority() {
        var isFriend = false
        isFriend = bind.tvFollowBtn.visibility == View.GONE

        val f = NotificationPriorityF(notificationType, isFriend, userName, userId) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                notificationType = bundle.getString("type")
                setUpNotificationIcon(notificationType)
            } else {
                callApiForGetAllvideos()
            }
        }

        f.show(supportFragmentManager, "")
    }

    private fun setUpNotificationIcon(type: String?) {
        if (type.equals("1", ignoreCase = true)) {
            bind.notificationBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_live_notification
                )
            )

        } else if (type.equals("0", ignoreCase = true)) {
            bind.notificationBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_mute_notification
                )
            )
        }
    }

    private fun showMyLikesCounts() {
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        val likeDialogBind = ShowLikesAlertPopupDialogBinding.inflate(layoutInflater)
        dialog.setContentView(R.layout.show_likes_alert_popup_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        likeDialogBind.tvMessage.text =
            bind.username.text.toString() + " " + getString(R.string.received_a_total_of) + " " + totalLikes + " " + getString(
                R.string.likes_across_all_video
            )
        likeDialogBind.tvDone.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun openSuggestionScreen() {
        val intent = Intent(this@ProfileA, FollowsMainTabA::class.java)
        intent.putExtra("id", userId)
        intent.putExtra("from_where", "suggestion")
        intent.putExtra("userName", userName)
        intent.putExtra("followingCount", followingCount)
        intent.putExtra("followerCount", followerCount)
        resultFollowCallback.launch(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private val suggestionUserList: Unit
        get() {
            val parameters = JSONObject()
            try {
                parameters.put(
                    "user_id",
                    Functions.getSharedPreference(this).getString(Variables.U_ID, "0")
                )
                parameters.put("starting_point", "0")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            VolleyRequest.JsonPostRequest(
                this@ProfileA,
                ApiLinks.showSuggestedUsers,
                parameters,
                Functions.getHeaders(this)
            ) { resp ->
                Functions.checkStatus(this@ProfileA, resp)
                hideSuggestionButtonProgress()
                suggestionList.clear()
                try {
                    val jsonObject = JSONObject(resp)
                    val code = jsonObject.optString("code")
                    if (code == "200") {
                        val msgArray = jsonObject.getJSONArray("msg")
                        for (i in 0 until msgArray.length()) {
                            val `object` = msgArray.optJSONObject(i)
                            val userDetailModel =
                                DataParsing.getUserDataModel(`object`.optJSONObject("User"))
                            val item = FollowingModel()
                            item.fb_id = userDetailModel.id
                            item.first_name = userDetailModel.firstName
                            item.last_name = userDetailModel.lastName
                            item.bio = userDetailModel.bio
                            item.username = userDetailModel.username
                            item.profile_pic = userDetailModel.profilePic
                            item.follow_status_button = "follow"
                            suggestionList.add(item)
                            adapterSuggestion!!.notifyDataSetChanged()
                        }
                        if (suggestionList.isEmpty()) {
                            findViewById<View>(R.id.tvNoSuggestionFound).visibility = View.VISIBLE
                        } else {
                            findViewById<View>(R.id.tvNoSuggestionFound).visibility = View.GONE
                        }
                    } else {
                        findViewById<View>(R.id.tvNoSuggestionFound).visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private fun hideSuggestionButtonProgress() {
        val request = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.ic_arrow_drop_down_black_24dp).build()
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(bind.suggestionBtn.controller)
            .build()

        bind.suggestionBtn.controller = controller
    }

    private fun setUpSuggestionRecyclerview() {
        val layoutManager = LinearLayoutManager(this@ProfileA)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        bind.rvSugesstion.setLayoutManager(layoutManager)

        adapterSuggestion = SuggestionAdapter(suggestionList) { view, postion, item ->
            if (view.id == R.id.tvFollowBtn) {
                if (Functions.checkLoginUser(this@ProfileA)) followSuggestedUser(
                    item.fb_id,
                    postion
                )

            } else if (view.id == R.id.user_image) {
                if (Functions.checkProfileOpenValidation(item.fb_id)) {
                    val intent = Intent(view.context, ProfileA::class.java)
                    intent.putExtra("user_id", item.fb_id)
                    intent.putExtra("user_name", item.username)
                    intent.putExtra("user_pic", item.profile_pic)
                    startActivity(intent)
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
                }
            } else if (view.id == R.id.ivCross) {
                suggestionList.removeAt(postion)
                adapterSuggestion!!.notifyDataSetChanged()
            }
        }

        bind.rvSugesstion.adapter = adapterSuggestion
    }

    public override fun onResume() {
        super.onResume()
        if (isRunFirstTime) {
            callApiForGetAllvideos()
        }
    }


    private fun callApiForGetAllvideos() {
        if (intent == null) {
            userId = Functions.getSharedPreference(this).getString(Variables.U_ID, "0")
        }
        val parameters = JSONObject()
        try {
            if (Functions.getSharedPreference(this)
                    .getBoolean(Variables.IS_LOGIN, false) && userId != null
            ) {
                parameters.put(
                    "user_id",
                    Functions.getSharedPreference(this).getString(Variables.U_ID, "")
                )
                parameters.put("other_user_id", userId)
            } else if (userId != null) {
                parameters.put("user_id", userId)
            } else {
                if (Functions.getSharedPreference(this).getBoolean(Variables.IS_LOGIN, false)) {
                    parameters.put(
                        "user_id",
                        Functions.getSharedPreference(this).getString(Variables.U_ID, "")
                    )
                }
                parameters.put("username", userName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        VolleyRequest.JsonPostRequest(
            this@ProfileA,
            ApiLinks.showUserDetail,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@ProfileA, resp)
            isRunFirstTime = true
            parseData(resp)
        }
    }

    fun parseData(response: String?) {
        try {
            val jsonObject = JSONObject(response)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONObject("msg")
                val userObj = msg.optJSONObject("User")
                val userDetailModel = DataParsing.getUserDataModel(userObj)
                try {
                    val storyArray = userObj.getJSONArray("story")
                    storyDataList.clear()
                    val storyItem = StoryModel()
                    storyItem.userModel = userDetailModel
                    val storyVideoList = ArrayList<StoryVideoModel>()

                    for (i in 0 until storyArray.length()) {
                        val itemObj = storyArray.getJSONObject(i)
                        val storyVideoItem =
                            DataParsing.getVideoDataModel(itemObj.optJSONObject("Video"))
                        storyVideoList.add(storyVideoItem)
                    }

                    storyItem.videoList = storyVideoList
                    if (storyVideoList.size > 0) {
                        storyDataList.add(storyItem)
                        bind.circleStatusBar!!.visibility = View.VISIBLE
                        bind.circleStatusBar!!.counts = storyVideoList.size
                    } else {
                        bind.circleStatusBar!!.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.d(Constants.TAG_, "Exception: $e")
                }

                val push_notification_setting = msg.optJSONObject("PushNotification")
                val privacy_policy_setting = msg.optJSONObject("PrivacySetting")
                if (userId == null) {
                    userId = userDetailModel.id
                    setupTabIcons()
                }

                val first_name = userDetailModel.firstName
                val last_name = userDetailModel.lastName
                if (first_name.equals("", ignoreCase = true) && last_name.equals("", ignoreCase = true)) {
                    bind.username.text = userDetailModel.username
                } else {
                    bind.username.text = "$first_name $last_name"
                }

                bind.username2Txt.text = Functions.showUsername(userDetailModel.username)
                picUrl = userDetailModel.profilePic
                profileGif = userDetailModel.profileGif
                userPic = userDetailModel.profilePic
                fullName = "$first_name $last_name"
                buttonStatus = userDetailModel.button.lowercase(Locale.getDefault())

                if (profileGif == Constants.BASE_URL) {
                    bind.userImage.controller = Functions.frescoImageLoad(picUrl, bind.userImage, false)
                } else {
                    bind.userImage.controller = Functions.frescoImageLoad(
                        profileGif,
                        R.drawable.ic_user_icon,
                        bind.userImage,
                        true
                    )
                }

                if (TextUtils.isEmpty(userDetailModel.bio)) {
                    bind.tvBio.visibility = View.GONE
                } else {
                    bind.tvBio.visibility = View.VISIBLE
                    bind.tvBio.text = userDetailModel.bio
                }

                if (TextUtils.isEmpty(userDetailModel.website)) {
                    bind.tabLink.visibility = View.GONE
                } else {
                    bind.tabLink.visibility = View.VISIBLE
                    bind.tvLink.text = userDetailModel.website
                }

                followingCount = userDetailModel.followingCount
                followerCount = userDetailModel.followersCount
                totalLikes = userDetailModel.likesCount
                isUserAlreadyBlock = userDetailModel.block
                blockByUserId = userDetailModel.blockByUser
                notificationType = userDetailModel.notification
                setUpNotificationIcon(notificationType)

                if (fragmentUserVides != null) {
                    if (msg.has("Playlist")) {
                        val playlistArray = msg.getJSONArray("Playlist")
                        fragmentUserVides!!.updateUserPlaylist(
                            playlistArray,
                            userDetailModel.verified
                        ) { bundle ->
                            if (bundle.getBoolean("isShow", false)) {
                                callApiForGetAllvideos()
                            }
                        }
                    }
                }

                val pushNotificationSetting_model = PushNotificationSettingModel()
                pushNotificationSetting_model.comments =
                    push_notification_setting.optString("comments")
                pushNotificationSetting_model.likes = push_notification_setting.optString("likes")
                pushNotificationSetting_model.newfollowers =
                    push_notification_setting.optString("new_followers")
                pushNotificationSetting_model.mentions =
                    push_notification_setting.optString("mentions")
                pushNotificationSetting_model.directmessage =
                    push_notification_setting.optString("direct_messages")
                pushNotificationSetting_model.videoupdates =
                    push_notification_setting.optString("video_updates")

                val privacyPolicySetting_model = PrivacyPolicySettingModel()
                privacyPolicySetting_model.videos_download =
                    privacy_policy_setting.optString("videos_download")
                privacyPolicySetting_model.direct_message =
                    privacy_policy_setting.optString("direct_message")
                privacyPolicySetting_model.duet = privacy_policy_setting.optString("duet")
                privacyPolicySetting_model.liked_videos =
                    privacy_policy_setting.optString("liked_videos")
                privacyPolicySetting_model.video_comment =
                    privacy_policy_setting.optString("video_comment")

                isLikeVideoShow =
                    !privacyPolicySetting_model.liked_videos.equals("only_me", ignoreCase = true)
                Log.d(Constants.TAG_, "isUserAlreadyBlock:: $isUserAlreadyBlock")


                //perform block functionality
                if (isUserAlreadyBlock == "1") {
                    bind.notificationBtn.visibility = View.GONE
                    bind.tabFollowOtherUser.visibility = View.GONE
                    bind.followCountTxt.text = Functions.getSuffix("0")
                    bind.fanCountTxt.text = Functions.getSuffix("0")
                    bind.heartCountTxt.text = Functions.getSuffix("0")
                    Log.d(Constants.TAG_, "isUserAlreadyBlock:: $isUserAlreadyBlock")

                } else {
                    bind.notificationBtn.visibility = View.VISIBLE
                    bind.tabFollowOtherUser.visibility = View.VISIBLE
                    bind.followCountTxt.text = Functions.getSuffix(followingCount)
                    bind.fanCountTxt.text = Functions.getSuffix(followerCount)
                    bind.heartCountTxt.text = Functions.getSuffix(totalLikes)
                }

                fragmentLikesVides!!.updateLikeVideoState(isLikeVideoShow)
                fragmentLikesVides!!.updateUserData(
                    userId,
                    Functions.getUserName(this),
                    isUserAlreadyBlock
                )

                fragmentUserVides!!.updateUserData(
                    userId,
                    Functions.getUserName(this),
                    isUserAlreadyBlock
                )

                isDirectMessage = Functions.isShowContentPrivacy(
                    this, privacyPolicySetting_model.direct_message,
                    userDetailModel.button.equals("friends", ignoreCase = true)
                )

                val follow_status = userDetailModel.button.lowercase(Locale.getDefault())
                if (follow_status.equals("following", ignoreCase = true)) {
                    bind.unFriendBtn.visibility = View.VISIBLE
                    bind.tvFollowBtn.visibility = View.GONE

                } else if (follow_status.equals("friends", ignoreCase = true)) {
                    bind.unFriendBtn.visibility = View.VISIBLE
                    bind.tvFollowBtn.visibility = View.GONE

                } else if (follow_status.equals("follow back", ignoreCase = true)) {
                    bind.unFriendBtn.visibility = View.GONE
                    bind.tvFollowBtn.visibility = View.VISIBLE
                } else {
                    bind.unFriendBtn.visibility = View.GONE
                    bind.tvFollowBtn.visibility = View.VISIBLE
                }

                val verified = userDetailModel.verified
                if (verified != null && verified.equals("1", ignoreCase = true)) {
                    findViewById<View>(R.id.varified_btn).visibility = View.VISIBLE
                }

            } else {
                Functions.showToast(this, jsonObject.optString("msg"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openBlockUserDialog() {
        val params = JSONObject()
        try {
            params.put(
                "user_id",
                Functions.getSharedPreference(this@ProfileA).getString(Variables.U_ID, "")
            )
            params.put("block_user_id", userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        VolleyRequest.JsonPostRequest(
            this@ProfileA,
            ApiLinks.blockUser,
            params,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@ProfileA, resp)
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                isUserAlreadyBlock = if (code == "200") {
                    val msgObj = jsonObject.getJSONObject("msg")
                    if (msgObj.has("BlockUser")) {
                        Functions.showToast(this@ProfileA, getString(R.string.user_blocked))
                        "1"
                    } else {
                        "0"
                    }
                } else {
                    "0"
                }
                callApiForGetAllvideos()
            } catch (e: Exception) {
                Log.d(Constants.TAG_, "Exception: $e")
            }
        }
    }

    private fun shareProfile() {
        var fromSetting = false
        fromSetting = userId.equals(
            Functions.getSharedPreference(this@ProfileA).getString(Variables.U_ID, ""),
            ignoreCase = true
        )
        val fragment = ShareUserProfileF(
            userId,
            userName,
            fullName,
            userPic,
            buttonStatus,
            isDirectMessage,
            fromSetting
        ) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                callApiForGetAllvideos()
            }
        }
        fragment.show(supportFragmentManager, "")
    }

    private fun followSuggestedUser(userId: String, position: Int) {
        Functions.callApiForFollowUnFollow(this@ProfileA,
            Functions.getSharedPreference(this).getString(Variables.U_ID, ""),
            userId,
            object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>?) {}
                override fun onSuccess(responce: String) {
                    suggestionList.removeAt(position)
                    adapterSuggestion!!.notifyDataSetChanged()
                    callApiForGetAllvideos()
                }

                override fun onFail(responce: String) {}
            })
    }

    private fun followUnFollowUser() {
        Functions.callApiForFollowUnFollow(this@ProfileA,
            Functions.getSharedPreference(this).getString(Variables.U_ID, ""),
            userId,
            object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>?) {}
                override fun onSuccess(responce: String) {
                    callApiForGetAllvideos()
                }

                override fun onFail(responce: String) {}
            })
    }

    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(this@ProfileA, WebviewA::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openManageMultipleAccounts() {
        val f = ManageAccountsF { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                Functions.hideSoftKeyboard(this@ProfileA)
                val intent = Intent(this@ProfileA, LoginA::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            }
        }
        f.show(supportFragmentManager, "")
    }

    // open the following screen
    private fun openFollowing() {
        val intent = Intent(this@ProfileA, FollowsMainTabA::class.java)
        intent.putExtra("id", userId)
        intent.putExtra("from_where", "following")
        intent.putExtra("userName", userName)
        intent.putExtra("followingCount", followingCount)
        intent.putExtra("followerCount", followerCount)
        resultFollowCallback.launch(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // open the followers screen
    private fun openFollowers() {
        val intent = Intent(this@ProfileA, FollowsMainTabA::class.java)
        intent.putExtra("id", userId)
        intent.putExtra("from_where", "fan")
        intent.putExtra("userName", userName)
        intent.putExtra("followingCount", followingCount)
        intent.putExtra("followerCount", followerCount)
        resultFollowCallback.launch(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun openStoryView() {
        val myIntent = Intent(this@ProfileA, ViewStoryA::class.java)
        myIntent.putExtra("storyList", storyDataList) //Optional parameters
        myIntent.putExtra("position", 0) //Optional parameters
        startActivity(myIntent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("isShow", true)
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }
}
