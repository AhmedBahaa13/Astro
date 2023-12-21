package com.uni.astro.activitesfragments.profile

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uni.astro.Constants.TAG_
import com.uni.astro.R
import com.uni.astro.activitesfragments.InboxA
import com.uni.astro.activitesfragments.NotificationA
import com.uni.astro.activitesfragments.WebviewA
import com.uni.astro.activitesfragments.accounts.LoginA
import com.uni.astro.activitesfragments.accounts.ManageAccountsF
import com.uni.astro.activitesfragments.livestreaming.activities.ConcertSelectionA
import com.uni.astro.activitesfragments.profile.likedvideos.LikedVideoF
import com.uni.astro.activitesfragments.profile.models.CloseFriendModel
import com.uni.astro.activitesfragments.profile.privatevideos.PrivateVideoF
import com.uni.astro.activitesfragments.profile.usersstory.ViewStoryA
import com.uni.astro.activitesfragments.profile.uservideos.UserVideoF
import com.uni.astro.adapters.ViewPagerAdapter
import com.uni.astro.apiclasses.ApiLinks
import com.uni.astro.databinding.FragmentProfileTabBinding
import com.uni.astro.databinding.ItemTabsProfileMenuBinding
import com.uni.astro.databinding.LayoutProfileBottomTabsBinding
import com.uni.astro.databinding.ShowLikesAlertPopupDialogBinding
import com.uni.astro.models.InboxModel
import com.uni.astro.models.PrivacyPolicySettingModel
import com.uni.astro.models.PushNotificationSettingModel
import com.uni.astro.models.StoryModel
import com.uni.astro.models.StoryVideoModel
import com.uni.astro.models.UserModel
import com.uni.astro.simpleclasses.DataParsing
import com.uni.astro.simpleclasses.DebounceClickHandler
import com.uni.astro.simpleclasses.Functions
import com.uni.astro.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import io.agora.rtc.Constants
import io.paperdb.Paper
import org.json.JSONObject
import java.io.File

class ProfileTabF : Fragment() {
    private lateinit var bind: FragmentProfileTabBinding

    private lateinit var bottomTabsBinding: LayoutProfileBottomTabsBinding

    private val tabIcons = intArrayOf(
        R.drawable.ic_my_video_select,
        R.drawable.ic_liked_video_gray,
        R.drawable.ic_repost_gray,
        R.drawable.ic_fav_gray,
        R.drawable.ic_lock_gray
    )

    private var totalLikes = ""

    private var adapter: ViewPagerAdapter? = null

    private var myVideosTab: UserVideoF? = null
    private var pushNotificationSettingModel: PushNotificationSettingModel? = null
    private var privacyPolicySettingModel: PrivacyPolicySettingModel? = null

    private var storyDataList = ArrayList<StoryModel>()
    private var mNewVideoReceiver: NewVideoBroadCast? = null
    private var mNotificationReceiver: NotificationBroadCast? = null
    var streamingId = ""
    var notReadInboxlist = ArrayList<InboxModel?>()

    private var picUrl: String? = null
    private var profileGif: String? = null
    private var followerCount: String? = null
    private var followingCount: String? = null
    private var myvideoCount = 0

    private val closeFriends: List<CloseFriendModel> = ArrayList()
    private val closeFriendsPicList: MutableList<ImageView> = ArrayList()


    var resultCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                updateProfile()
            }
        }
    }

    private var resultUserDetailCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                callApiForUserDetail()
            }
        }
    }

    private val mPermissionResult: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            activity, key
                        )
                    )
                }
            }

            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    requireContext(),
                    getString(R.string.we_need_camera_and_recording_permission_for_live_streaming)
                )
            } else if (allPermissionClear) {
                liveStreamingId
            }
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FragmentProfileTabBinding.inflate(inflater, container, false)

        bottomTabsBinding = bind.bottomLayout


        bottomTabsBinding.apply {
            setupViewPager(pager)
            TabLayoutMediator(tabs, pager) { _, _ -> }.attach()
            setupTabIcons()
            pager.offscreenPageLimit = 2
        }

        showDraftCount()

        bind.apply {
            circleStatusBar.strokeLineColor = requireContext().getColor(R.color.colorAccent)

            closeFriendsPicList.add(closeFriend1)
            closeFriendsPicList.add(closeFriend2)
            Log.d(TAG_, "onCreate: " + closeFriends.size)

            closeFriend1.setOnClickListener { view: View? ->
                val intent = Intent(requireActivity(), ProfileA::class.java)
                intent.putExtra("user_id", closeFriends[0].fb_id)
                intent.putExtra("user_name", closeFriends[0].username)
                intent.putExtra("user_pic", closeFriends[0].profile_pic)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            }

            closeFriend2.setOnClickListener { view: View? ->
                val intent = Intent(requireActivity(), ProfileA::class.java)
                intent.putExtra("user_id", closeFriends[1].fb_id)
                intent.putExtra("user_name", closeFriends[1].username)
                intent.putExtra("user_pic", closeFriends[1].profile_pic)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            }

            userImage.setOnClickListener(DebounceClickHandler {
                if (circleStatusBar.visibility == View.VISIBLE) {
                    openStoryDetail()
                } else {
                    openProfileDetail()
                }
            })

            editProfileBtn.setOnClickListener(DebounceClickHandler { openEditProfile() })

            tabAccount.setOnClickListener(DebounceClickHandler { openManageMultipleAccounts() })

            tabLink.setOnClickListener(DebounceClickHandler {
                openWebUrl(getString(R.string.web_browser), tvLink.text.toString())
            })

            messageBtn.setOnClickListener(DebounceClickHandler { view ->
                val intent = Intent(view.context, SettingAndPrivacyA::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            })

            tabInbox.setOnClickListener { openInboxF() }

            tabNotification.setOnClickListener(DebounceClickHandler { openNotifications() })

            tabViewsHistory.setOnClickListener(DebounceClickHandler { openProfileViewHistory() })

            tabPrivacyLikes.setOnClickListener(DebounceClickHandler { showMyLikesCounts() })

            followingLayout.setOnClickListener(DebounceClickHandler { openFollowing() })

            fansLayout.setOnClickListener(DebounceClickHandler { openFollowers() })

            addFriendsBtn.setOnClickListener(DebounceClickHandler { openInviteFriends() })
        }


        bottomTabsBinding.apply {
            tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    pager.setCurrentItem(tab.position, true)

                    when (tab.position) {
                        0 -> {
                            if (myvideoCount > 0) {
                                bind.createPopupLayout.visibility = View.GONE
                            } else {
                                bind.createPopupLayout.visibility = View.VISIBLE
                                val aniRotate =
                                    AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation)
                                bind.createPopupLayout.startAnimation(aniRotate)
                            }
                        }

                        1, 2, 3, 4 -> {
                            bind.createPopupLayout.clearAnimation()
                            bind.createPopupLayout.visibility = View.GONE
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })


            pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tabs.getTabAt(position)?.select()
                }
            })
        }


        mNewVideoReceiver = NewVideoBroadCast()
        requireActivity().registerReceiver(mNewVideoReceiver, IntentFilter("newVideo"))

        mNotificationReceiver = NotificationBroadCast()
        requireActivity().registerReceiver(mNotificationReceiver, IntentFilter("NotificationHit"))

        return bind.root
    }


    private fun setupViewPager(viewPager: ViewPager2) {
        myVideosTab = UserVideoF.newInstance(
            true,
            Functions.getSharedPreference(context).getString(Variables.U_ID, ""),
            Functions.getSharedPreference(context).getString(Variables.U_NAME, ""),
            ""
        )

        val likedVidFrag = LikedVideoF.newInstance(
            true,
            Functions.getSharedPreference(context).getString(Variables.U_ID, ""),
            Functions.getSharedPreference(context).getString(Variables.U_NAME, ""),
            true,
            ""
        )

        val repostFrag = RepostVideoF.newInstance(
            true,
            Functions.getSharedPreference(context).getString(Variables.U_ID, ""),
            Functions.getSharedPreference(context).getString(Variables.U_NAME, ""),
            ""
        )

        val adapter2 = ViewPagerAdapter(requireActivity())
        adapter2.addFragment(myVideosTab!!, getString(R.string.my_videos))
        adapter2.addFragment(likedVidFrag, getString(R.string.liked_videos))
        adapter2.addFragment(repostFrag, getString(R.string.repost))
        adapter2.addFragment(FavouriteTabF.newInstance(), getString(R.string.favourite))
        adapter2.addFragment(PrivateVideoF.newInstance(), getString(R.string.private_))
        viewPager.adapter = adapter2
    }
    private fun setupTabIcons() {
        bottomTabsBinding.apply {
            tabs.getTabAt(0)?.setIcon(tabIcons[0])
            tabs.getTabAt(1)?.setIcon(tabIcons[1])
            tabs.getTabAt(2)?.setIcon(tabIcons[2])
            tabs.getTabAt(3)?.setIcon(tabIcons[3])
            tabs.getTabAt(4)?.setIcon(tabIcons[4])
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


    private fun openProfileViewHistory() {
        val intent = Intent(context, ViewProfileHistoryA::class.java)
        resultUserDetailCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private fun openInboxF() {
        val intent = Intent(context, InboxA::class.java)
        resultUserDetailCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private fun openStoryDetail() {
        val myIntent = Intent(context, ViewStoryA::class.java)
        myIntent.putExtra("storyList", storyDataList) // Optional parameters
        myIntent.putExtra("position", 0) // Optional parameters
        startActivity(myIntent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private val liveStreamingId: Unit
        get() {
            val parameters = JSONObject()
            try {
                parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"))
                parameters.put("started_at", Functions.getCurrentDate("yyyy-MM-dd HH:mm:ss"))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Functions.showLoader(activity, false, false)
            VolleyRequest.JsonPostRequest(activity, ApiLinks.liveStream, parameters, Functions.getHeaders(context)) { resp ->
                Functions.checkStatus(activity, resp)
                Functions.cancelLoader()
                try {
                    val jsonObject = JSONObject(resp)
                    val code = jsonObject.optString("code")
                    if (code == "200") {
                        val msgObj = jsonObject.getJSONObject("msg")
                        val streamingObj = msgObj.getJSONObject("LiveStreaming")
                        streamingId = streamingObj.optString("id")
                        goLive()
                    }
                } catch (e: Exception) {
                    Log.d(TAG_, "Exception : $e")
                }
            }
        }

    private fun goLive() {
        val intent = Intent(activity, ConcertSelectionA::class.java)
        intent.putExtra(
            "userId",
            Functions.getSharedPreference(context).getString(Variables.U_ID, "")
        )
        intent.putExtra(
            "userName",
            Functions.getSharedPreference(context).getString(Variables.U_NAME, "")
        )
        intent.putExtra(
            "userPicture",
            Functions.getSharedPreference(context).getString(Variables.U_PIC, "")
        )
        intent.putExtra("userRole", Constants.CLIENT_ROLE_BROADCASTER)
        intent.putExtra("streamingId", streamingId)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private fun openInviteFriends() {
        val intent = Intent(context, InviteFriendsA::class.java)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openNotifications() {
        val intent = Intent(context, NotificationA::class.java)
        resultUserDetailCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private fun openProfileDetail() {
        var isGif = false
        val mediaURL: String?

        if (profileGif != null && profileGif != com.uni.astro.Constants.BASE_URL) {
            isGif = true
            mediaURL = profileGif
        } else {
            isGif = false
            mediaURL = picUrl
        }

        val fragment = ShareAndViewProfileF(isGif, mediaURL, Functions.getSharedPreference(context).getString(Variables.U_ID, "")) { bundle ->
            if (bundle.getString("action") == "profileShareMessage") {
                if (Functions.checkLoginUser(activity)) {
                    // firebase sharing
                }
            }
        }

        fragment.show(childFragmentManager, "")
    }

    private fun showMyLikesCounts() {
        val dialog = Dialog(requireContext())
        val dialogBinding = ShowLikesAlertPopupDialogBinding.inflate(layoutInflater)
        dialog.setCancelable(false)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.tvMessage.text = bind.username.text.toString() + " " + getString(R.string.received_a_total_of) + " " + totalLikes + " " + getString(R.string.likes_across_all_video)
        dialogBinding.tvDone.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(context, WebviewA::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openManageMultipleAccounts() {
        val f = ManageAccountsF { bundle: Bundle ->
            if (bundle.getBoolean("isShow", false)) {
                Functions.hideSoftKeyboard(activity)
                val intent = Intent(activity, LoginA::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            }
        }

        f.show(childFragmentManager, "")
    }

    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        if (visible) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
                    updateProfile()
                    callApiForUserDetail()
                    getCloseFriends()
                }
            }, 200)
        }
    }

    override fun onResume() {
        super.onResume()
        showDraftCount()
    }

    private fun getCloseFriends() {
        Log.d(TAG_, "getCloseFriends: fired")
        val parameters = JSONObject()
        val closeFriendList = ArrayList<CloseFriendModel>()
        val closeFriend = CloseFriendModel()

        try {
            parameters.put("user_id", Functions.getSharedPreference(requireActivity()).getString(Variables.U_ID, "0"))
        } catch (e: Exception) {
            Log.d(TAG_, "getCloseFriends: $e")
        }

        VolleyRequest.JsonPostRequest(requireActivity(), ApiLinks.showCloseFriends, parameters, Functions.getHeaders(requireContext())) { resp: String? ->
            Functions.checkStatus(requireActivity(), resp)

            try {
                val response = JSONObject(resp)
                if (response.optString("code") == "200") {
                    val closeFriendsArray = response.optJSONArray("msg")

                    if (closeFriendsArray != null && closeFriendsArray.length() != 0) {
                        for (i in 0 until closeFriendsArray.length()) {
                            val item = closeFriendsArray.getJSONObject(i)
                            closeFriend.profile_pic = item.optString("profile_pic_small")
                            closeFriend.fb_id = item.optString("user_id")
                            Glide.with(requireContext()).load(closeFriend.profile_pic)
                                .circleCrop()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.baseline_person_24)
                                .into(closeFriendsPicList!![i])

                            Log.d(TAG_, "getCloseFriends: # of close frirends: $i")

                            closeFriendsPicList!![i].setOnClickListener { view: View? ->
                                val intent = Intent(requireActivity(), ProfileA::class.java)
                                intent.putExtra("user_id", closeFriend.fb_id)
                                startActivity(intent)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG_, "getCloseFriends: $e")
            }
        }
    }

    private fun showDraftCount() {
        try {
            val path = Functions.getAppFolder(activity) + Variables.DRAFT_APP_FOLDER
            val directory = File(path)
            val files = directory.listFiles()

            if (files.isEmpty()) {
                //draf gone
            } else {
                //draf visible
            }
        } catch (_: Exception) { }
    }

    // place the profile data
    private fun updateProfile() {
        bind.username2Txt.text = Functions.showUsername(Functions.getSharedPreference(context).getString(Variables.U_NAME, ""))
        val firstName = Functions.getSharedPreference(context).getString(Variables.F_NAME, "")
        val lastName = Functions.getSharedPreference(context).getString(Variables.L_NAME, "")

        if (firstName.equals("", ignoreCase = true) && lastName.equals("", ignoreCase = true)) {
            bind.username.text = Functions.getSharedPreference(context).getString(Variables.U_NAME, "")
        } else {
            bind.username.text = "$firstName $lastName"
        }

        if (TextUtils.isEmpty(Functions.getSharedPreference(context).getString(Variables.U_BIO, ""))) {
            bind.tvBio.visibility = View.GONE

        } else {
            bind.tvBio.visibility = View.VISIBLE
            bind.tvBio.text = Functions.getSharedPreference(context).getString(Variables.U_BIO, "")
        }

        if (TextUtils.isEmpty(Functions.getSharedPreference(context).getString(Variables.U_LINK, ""))) {
            bind.tabLink.visibility = View.GONE

        } else {
            bind.tabLink.visibility = View.VISIBLE
            bind.tvLink.text = Functions.getSharedPreference(context).getString(Variables.U_LINK, "")
        }

        picUrl = Functions.getSharedPreference(context).getString(Variables.U_PIC, "")
        profileGif = Functions.getSharedPreference(context).getString(Variables.U_GIF, "")

        if (profileGif == com.uni.astro.Constants.BASE_URL) {
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


    // change the icons of the tab
    /*
    private fun setupTabIcons(bottomTabsBind: LayoutProfileBottomTabsBinding) {
        val tabBinding = ItemTabsProfileMenuBinding.inflate(layoutInflater)


        tabBinding.image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_video_select))
        tabBinding.image.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black), PorterDuff.Mode.SRC_IN)
        bottomTabsBind.tabs.getTabAt(0)?.setCustomView(tabBinding.root)


        tabBinding.image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_liked_video_gray))
        tabBinding.image.setColorFilter(ContextCompat.getColor(requireContext(), R.color.darkgray), PorterDuff.Mode.SRC_IN)
        bottomTabsBind.tabs.getTabAt(1)?.setCustomView(tabBinding.root)


        tabBinding.image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_repost_gray))
        tabBinding.image.setColorFilter(ContextCompat.getColor(requireContext(), R.color.darkgray), PorterDuff.Mode.SRC_IN)
        bottomTabsBind.tabs.getTabAt(2)?.setCustomView(tabBinding.root)


        tabBinding.image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fav_gray))
        tabBinding.image.setColorFilter(ContextCompat.getColor(requireContext(), R.color.darkgray), PorterDuff.Mode.SRC_IN)
        bottomTabsBind.tabs.getTabAt(3)?.setCustomView(tabBinding.root)


        tabBinding.image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock_gray))
        tabBinding.image.setColorFilter(ContextCompat.getColor(requireContext(), R.color.darkgray), PorterDuff.Mode.SRC_IN)
        bottomTabsBind.tabs.getTabAt(4)?.setCustomView(tabBinding.root)


        bottomTabsBind.tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                bottomTabsBind.pager.setCurrentItem(tab.position, true)

                when (tab.position) {
                    0 -> {
                        if (myvideoCount > 0) {
                            bind.createPopupLayout.visibility = View.GONE
                        } else {
                            bind.createPopupLayout.visibility = View.VISIBLE
                            val aniRotate =
                                AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation)
                            bind.createPopupLayout.startAnimation(aniRotate)
                        }

                        tabBinding.image.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_my_video_select
                            )
                        )

                        tabBinding.image.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.black),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    1 -> {
                        bind.createPopupLayout.clearAnimation()
                        bind.createPopupLayout.visibility = View.GONE

                        tabBinding.image.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_liked_video_color
                            )
                        )
                        tabBinding.image.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.black),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    2 -> {
                        bind.createPopupLayout.clearAnimation()
                        bind.createPopupLayout.visibility = View.GONE

                        tabBinding.image.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_repost_gray
                            )
                        )
                        tabBinding.image.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.black),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    3 -> {
                        bind.createPopupLayout.clearAnimation()
                        bind.createPopupLayout.visibility = View.GONE

                        tabBinding.image.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_fav_black
                            )
                        )
                        tabBinding.image.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.black),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    4 -> {
                        bind.createPopupLayout.clearAnimation()
                        bind.createPopupLayout.visibility = View.GONE

                        tabBinding.image.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_lock_black
                            )
                        )
                        tabBinding.image.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.black),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }

                tab.setCustomView(tabBinding.root)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        tabBinding.image.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_my_video_gray
                            )
                        )
                        tabBinding.image.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.darkgray),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    1 -> {
                        tabBinding.image.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_liked_video_gray
                            )
                        )
                        tabBinding.image.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.darkgray),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    2 -> {
                        tabBinding.image.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_repost_gray
                            )
                        )
                        tabBinding.image.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.darkgray),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    3 -> {
                        tabBinding.image.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_fav_gray
                            )
                        )
                        tabBinding.image.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.darkgray),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    4 -> {
                        tabBinding.image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock_gray))
                        tabBinding.image.setColorFilter(ContextCompat.getColor(requireContext(), R.color.darkgray), PorterDuff.Mode.SRC_IN)
                    }
                }

                tab.setCustomView(tabBinding.root)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    */


    //this will get the all videos data of user and then parse the data
    private fun callApiForUserDetail() {
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id",
                Functions.getSharedPreference(context).getString(Variables.U_ID, "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.showUserDetail, parameters, Functions.getHeaders(
                activity
            )
        ) { resp ->
            Functions.checkStatus(activity, resp)
            parseData(resp)
        }
    }

    fun parseData(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val code = jsonObject.optString("code")

            if (code == "200") {
                val msg = jsonObject.optJSONObject("msg")
                val pushNotificationSetting = msg.optJSONObject("PushNotification")
                val privacyPolicySetting = msg.optJSONObject("PrivacySetting")
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
                        bind.circleStatusBar.visibility = View.VISIBLE
                        bind.circleStatusBar.counts = storyVideoList.size
                    } else {
                        bind.circleStatusBar.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.d(TAG_, "Exception: $e")
                }


                pushNotificationSettingModel = PushNotificationSettingModel()
                if (pushNotificationSetting != null) {
                    pushNotificationSettingModel!!.comments =
                        pushNotificationSetting.optString("comments")
                    pushNotificationSettingModel!!.likes =
                        pushNotificationSetting.optString("likes")
                    pushNotificationSettingModel!!.newfollowers =
                        pushNotificationSetting.optString("new_followers")
                    pushNotificationSettingModel!!.mentions =
                        pushNotificationSetting.optString("mentions")
                    pushNotificationSettingModel!!.directmessage =
                        pushNotificationSetting.optString("direct_messages")
                    pushNotificationSettingModel!!.videoupdates =
                        pushNotificationSetting.optString("video_updates")
                }

                privacyPolicySettingModel = PrivacyPolicySettingModel()
                if (privacyPolicySetting != null) {
                    privacyPolicySettingModel!!.videos_download =
                        privacyPolicySetting.optString("videos_download")
                    privacyPolicySettingModel!!.direct_message =
                        privacyPolicySetting.optString("direct_message")
                    privacyPolicySettingModel!!.duet = privacyPolicySetting.optString("duet")
                    privacyPolicySettingModel!!.liked_videos =
                        privacyPolicySetting.optString("liked_videos")
                    privacyPolicySettingModel!!.video_comment =
                        privacyPolicySetting.optString("video_comment")
                }

                Paper.book(Variables.PrivacySetting).write(Variables.PushSettingModel, pushNotificationSettingModel)
                Paper.book(Variables.PrivacySetting).write(Variables.PrivacySettingModel, privacyPolicySettingModel)

                bind.apply {
                    username2Txt.text = Functions.showUsername(userDetailModel.username)
                    if (TextUtils.isEmpty(userDetailModel.bio)) {
                        tvBio.visibility = View.GONE
                    } else {
                        tvBio.visibility = View.VISIBLE
                        tvBio.text = userDetailModel.bio
                    }

                    if (TextUtils.isEmpty(userDetailModel.website)) {
                        tabLink.visibility = View.GONE
                    } else {
                        tabLink.visibility = View.VISIBLE
                        tvLink.text = userDetailModel.website
                    }

                    val firstName = userDetailModel.firstName
                    val lastName = userDetailModel.lastName
                    if (firstName.equals("", ignoreCase = true) && lastName.equals("", ignoreCase = true)) {
                        username.text = userDetailModel.username
                    } else {
                        username.text = "$firstName $lastName"
                    }

                    picUrl = userDetailModel.profilePic
                    profileGif = userDetailModel.profileGif

                    if (profileGif == com.uni.astro.Constants.BASE_URL) {
                        userImage.controller = Functions.frescoImageLoad(picUrl, userImage, false)

                    } else {
                        userImage.controller = Functions.frescoImageLoad(
                            profileGif, R.drawable.ic_user_icon, userImage, true
                        )
                    }
                }

                val editor = Functions.getSharedPreference(context).edit()
                editor.putString(Variables.U_PIC, picUrl)
                editor.putString(Variables.U_GIF, userDetailModel.profileGif)
                editor.putString(Variables.U_PROFILE_VIEW, userDetailModel.profileView)
                editor.putString(Variables.U_WALLET, userDetailModel.wallet.toString())
                editor.putString(Variables.U_total_coins_all_time, userDetailModel.total_all_time_coins.toString())
                editor.putString(Variables.REFERAL_CODE, userDetailModel.referalCode)
                editor.putString(Variables.IS_VERIFIED, userDetailModel.verified)
                editor.apply()

                followingCount = userDetailModel.followingCount
                followerCount = userDetailModel.followersCount
                totalLikes = userDetailModel.likesCount

                bind.followCountTxt.text = Functions.getSuffix(followingCount)
                bind.fanCountTxt.text = Functions.getSuffix(followerCount)
                bind.heartCountTxt.text = Functions.getSuffix(totalLikes)
                myvideoCount = Functions.parseInterger(userDetailModel.videoCount)

                if (myvideoCount != 0) {
                    bind.createPopupLayout.visibility = View.GONE
                    bind.createPopupLayout.clearAnimation()
                } else {
                    bind.createPopupLayout.visibility = View.VISIBLE
                    val aniRotate =
                        AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation)
                    bind.createPopupLayout.startAnimation(aniRotate)
                }

                val verified = userDetailModel.verified
                if (verified != null && verified.equals("1", ignoreCase = true)) {
                    bind.varifiedBtn.visibility = View.VISIBLE
                }

                if (myVideosTab != null) {
                    myVideosTab!!.updatePlaylistCreate()
                    if (msg.has("Playlist")) {
                        val playlistArray = msg.getJSONArray("Playlist")
                        myVideosTab!!.updateUserPlaylist(
                            playlistArray,
                            userDetailModel.verified
                        ) { bundle ->
                            if (bundle.getBoolean("isShow", false)) {
                                callApiForUserDetail()
                                getCloseFriends()
                            }
                        }
                    }
                }

                updateProfileNotificationCount(userDetailModel)
                updateProfileVisitorCount(userDetailModel)

            } else {
                Functions.showToast(activity, jsonObject.optString("msg"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inboxList()
        }
    }

    private fun updateProfileNotificationCount(userDetailModel: UserModel) {
        bind.apply {
            if (userDetailModel.notificationCount > 0) {
                tabNotificationCount.visibility = View.VISIBLE
                if (userDetailModel.notificationCount > 99) {
                    tvNotificationPlus.visibility = View.VISIBLE
                    tvNotificationCount.text = "99"
                } else {
                    tvNotificationPlus.visibility = View.GONE
                    tvNotificationCount.text = userDetailModel.notificationCount.toString()
                }
            } else {
                tabNotificationCount.visibility = View.GONE
            }
        }
    }

    private fun updateProfileVisitorCount(userDetailModel: UserModel) {
        bind.apply {
            if (userDetailModel.visitorCount > 0) {
                tabVisitorCount.visibility = View.VISIBLE
                if (userDetailModel.visitorCount > 99) {
                    tvVisitorPlus.visibility = View.VISIBLE
                    tvVisitorCount.text = "99"
                } else {
                    tvVisitorPlus.visibility = View.GONE
                    tvVisitorCount.text = userDetailModel.visitorCount.toString()
                }
            } else {
                tabVisitorCount.visibility = View.GONE
            }
        }
    }

    private fun openEditProfile() {
        val intent = Intent(context, EditProfileA::class.java)
        resultCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    // open the following fragment
    private fun openFollowing() {
        val intent = Intent(context, FollowsMainTabA::class.java)
        intent.putExtra("id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""))
        intent.putExtra("from_where", "following")
        intent.putExtra(
            "userName",
            Functions.getSharedPreference(context).getString(Variables.U_NAME, "")
        )
        intent.putExtra("followingCount", followingCount)
        intent.putExtra("followerCount", followerCount)
        resultUserDetailCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // open the followers fragment
    private fun openFollowers() {
        val intent = Intent(context, FollowsMainTabA::class.java)
        intent.putExtra("id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""))
        intent.putExtra("from_where", "fan")
        intent.putExtra(
            "userName",
            Functions.getSharedPreference(context).getString(Variables.U_NAME, "")
        )
        intent.putExtra("followingCount", followingCount)
        intent.putExtra("followerCount", followerCount)
        resultUserDetailCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun inboxList() {
        val inboxQuery = FirebaseDatabase.getInstance().reference.child("Inbox")
            .child(Functions.getSharedPreference(context).getString(Variables.U_ID, "0")!!)
            .orderByChild("date")

        inboxQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    notReadInboxlist.clear()
                    for (ds in dataSnapshot.children) {
                        val model = ds.getValue(InboxModel::class.java)
                        model!!.id = ds.key
                        if (model.status != null && model.status == "0") {
                            notReadInboxlist.add(model)
                        }
                    }
                }
                updateInboxCounts()
            }

            override fun onCancelled(error: DatabaseError) {
                updateInboxCounts()
            }
        })
    }

    private fun updateInboxCounts() {
        bind.apply {
            if (notReadInboxlist.size > 0) {
                tabInboxCount.visibility = View.VISIBLE
                if (notReadInboxlist.size > 99) {
                    tvInboxPlus.visibility = View.VISIBLE
                    tvInboxCount.text = "99"

                } else {
                    tvInboxPlus.visibility = View.GONE
                    tvInboxCount.text = notReadInboxlist.size.toString()
                }
            } else {
                tabInboxCount.visibility = View.GONE
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mPermissionResult.unregister()

        if (mNewVideoReceiver != null) {
            requireActivity().unregisterReceiver(mNewVideoReceiver)
            mNewVideoReceiver = null
        }
        if (mNotificationReceiver != null) {
            requireActivity().unregisterReceiver(mNotificationReceiver)
            mNotificationReceiver = null
        }
    }

    inner class NewVideoBroadCast : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            callApiForUserDetail()
        }
    }

    inner class NotificationBroadCast : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            callApiForUserDetail()
        }
    }

    companion object {
        fun newInstance(): ProfileTabF {
            val fragment = ProfileTabF()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
