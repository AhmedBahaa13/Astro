package com.uni.astro.activitesfragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.facebook.FacebookSdk.getApplicationContext
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoSize
import com.like.LikeButton
import com.like.OnLikeListener
import com.uni.astro.Constants
import com.uni.astro.R
import com.uni.astro.activitesfragments.comments.CommentF
import com.uni.astro.activitesfragments.profile.ProfileA
import com.uni.astro.activitesfragments.profile.ReportTypeA
import com.uni.astro.activitesfragments.profile.videopromotion.VideoPromoteStepsA
import com.uni.astro.activitesfragments.soundlists.VideoSoundA
import com.uni.astro.activitesfragments.videorecording.VideoRecoderDuetA
import com.uni.astro.adapters.ViewPagerStatAdapter
import com.uni.astro.apiclasses.ApiLinks
import com.uni.astro.databinding.AlertLabelEditorBinding
import com.uni.astro.databinding.ItemHomeHeightedLayoutBinding
import com.uni.astro.interfaces.FragmentCallBack
import com.uni.astro.interfaces.FragmentDataSend
import com.uni.astro.models.HomeModel
import com.uni.astro.simpleclasses.DataParsing
import com.uni.astro.simpleclasses.DebounceClickHandler
import com.uni.astro.simpleclasses.FriendsTagHelper
import com.uni.astro.simpleclasses.Functions
import com.uni.astro.simpleclasses.OnSwipeTouchListener
import com.uni.astro.simpleclasses.PermissionUtils
import com.uni.astro.simpleclasses.ShowMoreLess
import com.uni.astro.simpleclasses.Variables
import com.uni.astro.simpleclasses.VerticalViewPager
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import io.paperdb.Paper
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale


// this is the main view which is show all the video in list
class VideosListF : Fragment, Player.Listener, FragmentDataSend {
    private lateinit var bind: ItemHomeHeightedLayoutBinding

    var menuPager: VerticalViewPager? = null

    @JvmField
    var item: HomeModel? = null
    var fragmentCallBack: FragmentCallBack? = null

    private var showad = false
    private var fragmentContainerId = 0
    var takePermissionUtils: PermissionUtils? = null
    var handler: Handler? = null
    var runnable: Runnable? = null
    var animationRunning = false
    private var currentPinStatus = "0"

    @JvmField
    var exoplayer: ExoPlayer? = null

    // show a video as a ad
    private var isAddAlreadyShow = false
    private var countDownTimer: CountDownTimer? = null
    private var isVisibleToUser = false


    private var resultVideoSettingCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                callApiForSingleVideos()
            }
        }
    }

    var resultCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result?.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                callApiForSingleVideos()
            }
        }
    }

    private val mPermissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        var allPermissionClear = true
        val blockPermissionCheck: MutableList<String> = ArrayList()
        for (key: String? in result?.keys!!) {
            if (!(result[key])!!) {
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
                requireContext(), getString(R.string.we_need_camera_and_recording_permission_for_make_video_on_sound)
            )
        } else if (allPermissionClear) {
            openSoundByScreen()
        }
    }


    constructor(showad: Boolean, item: HomeModel?, menuPager: VerticalViewPager?, fragmentCallBack: FragmentCallBack?, fragmentContainerId: Int) {
        this.showad = showad
        this.item = item
        this.menuPager = menuPager
        this.fragmentCallBack = fragmentCallBack
        this.fragmentContainerId = fragmentContainerId
    }

    constructor()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (fragmentContainerId == R.id.mainMenuFragment) {
            // Inflate the layout for this fragment adjust 32dp height for transparent layout
            bind = ItemHomeHeightedLayoutBinding.inflate(inflater, container, false)
            updateImmediateViewChange()

        } else {
            // Inflate the layout for this fragment
            // bind = ItemHomeLayoutBinding.inflate(inflater, container, false)
            bind = ItemHomeHeightedLayoutBinding.inflate(inflater, container, false)
            //view = inflater.inflate(R.layout.item_home_layout, container, false)
            updateImmediateViewChange()
        }

        initializePlayer()
        initView()

        return bind.root
    }


    private fun initView() {
        bind.apply {
            ivAddFollow.setOnClickListener(DebounceClickHandler {
                if (Functions.checkLoginUser(getActivity())) {
                    followUnFollowUser()
                }
            })

            duetOpenVideo.setOnClickListener(DebounceClickHandler {
                openDuetVideo(item)
            })

            userPic.setOnClickListener(DebounceClickHandler {
                onPause()
                openProfile(item, false)
            })

            animateRlt.setOnClickListener(DebounceClickHandler {
                if (Functions.checkLoginUser(activity)) {
                    animateRlt.visibility = View.GONE
                    likeVideo(item)
                }
            })

            username.setOnClickListener(DebounceClickHandler {
                onPause()
                openProfile(item, false)
            })

            commentLayout.setOnClickListener(DebounceClickHandler {
                if (Functions.checkLoginUser(activity)) {
                    openComment(item)
                }
            })

            sharedLayout.setOnClickListener(DebounceClickHandler { openShareVideoView() })

            soundImageLayout.setOnClickListener(DebounceClickHandler(View.OnClickListener { view: View ->
                if (item == null || item!!.user_id == null) {
                    return@OnClickListener
                }

                if (item!!.promotionModel != null && item!!.promotionModel.id != null) {
                    Functions.showToastOnTop(
                        activity,
                        view,
                        getString(R.string.video_ads_do_not_support_this_feature)
                    )
                    return@OnClickListener
                }

                takePermissionUtils = PermissionUtils(activity, mPermissionResult)
                if (takePermissionUtils!!.isCameraRecordingPermissionGranted) {
                    openSoundByScreen()
                } else {
                    takePermissionUtils!!.showCameraRecordingPermissionDailog(
                        getString(R.string.we_need_camera_and_recording_permission_for_make_video_on_sound)
                    )
                }
            }))

            skipBtn.setOnClickListener(DebounceClickHandler { hideAd() })

            likebtn.setOnLikeListener(object : OnLikeListener {
                override fun liked(likeButton: LikeButton) {
                    likeVideo(item)
                }

                override fun unLiked(likeButton: LikeButton) {
                    likeVideo(item)
                }
            })

            tabFavourite.setOnLikeListener(object : OnLikeListener {
                override fun liked(likeButton: LikeButton) {
                    favouriteVideo(item)
                }

                override fun unLiked(likeButton: LikeButton) {
                    favouriteVideo(item)
                }
            })
        }

        Handler(Looper.getMainLooper()).postDelayed({ setData() }, 200)
    }


    private fun updateImmediateViewChange() {
        bind.ViewForPlaylist.visibility = try {
            if ((item!!.playlistId == "0")) {
                View.GONE
            } else {
                View.VISIBLE
            }
        } catch (e: Exception) {
            View.GONE
        }

        try {
            if (item!!.promotionModel != null && item!!.promotionModel.id != null) {
                val destination = item!!.promotionModel.destination
                bind.tabPromotionText.visibility = View.VISIBLE
                if ((destination == "website")) {
                    updatePromotionSiteAction(bind.btnWebsiteMove)
                } else if ((destination == "follower")) {
                    updatePromotionFollowAction(bind.btnWebsiteMove)
                } else {
                    updatePromotionVideoViewAction(bind.btnWebsiteMove)
                }
            } else {
                bind.tabPromotionText.visibility = View.GONE
                bind.btnWebsiteMove.visibility = View.GONE
            }
        } catch (e: Exception) {
            bind.tabPromotionText.visibility = View.GONE
            bind.btnWebsiteMove.visibility = View.GONE
        }
    }

    private fun updatePromotionVideoViewAction(btnPromote: Button) {
        btnPromote.visibility = View.GONE
        btnPromote.setOnClickListener { }
    }

    private fun updatePromotionSiteAction(btnPromote: Button) {
        btnPromote.visibility = View.VISIBLE
        val actionButton = "" + item!!.promotionModel.action_button

        if (actionButton.equals("Shop now", ignoreCase = true)) {
            btnPromote.text = getString(R.string.shop_now)
        } else if (actionButton.equals("Sign up", ignoreCase = true)) {
            btnPromote.text = getString(R.string.sign_up)
        } else if (actionButton.equals("Contact us", ignoreCase = true)) {
            btnPromote.text = getString(R.string.contact_us)
        } else if (actionButton.equals("Apply now", ignoreCase = true)) {
            btnPromote.text = getString(R.string.apply_now)
        } else if (actionButton.equals("Book now", ignoreCase = true)) {
            btnPromote.text = getString(R.string.book_now)
        } else {
            btnPromote.text = getString(R.string.learn_more)
        }

        btnPromote.setOnClickListener { hitPromotedVideoWebsite() }
    }

    private fun hitPromotedVideoWebsite() {
        val parameters = JSONObject()
        try {
            parameters.put("promotion_id", item!!.promotionModel.id)
        } catch (e: Exception) {
            Log.d(Constants.TAG_, "Exception: $e")
        }

        Functions.showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(activity, ApiLinks.destinationTap, parameters, Functions.getHeaders(activity)) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if ((code == "200")) {
                    openWebUrl(
                        "" + item!!.promotionModel.action_button,
                        item!!.promotionModel.website_url
                    )
                }
            } catch (e: Exception) {
                Log.d(Constants.TAG_, "Exception: $e")
            }
        }
    }

    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(requireContext(), WebviewA::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun updatePromotionFollowAction(btnPromote: Button) {
        var followStatus = item!!.follow_status_button
        if (followStatus == null) {
            followStatus = ""
        }

        if (followStatus.equals("following", ignoreCase = true)) {
            btnPromote.visibility = View.GONE
        } else if (followStatus.equals("friends", ignoreCase = true)) {
            btnPromote.visibility = View.GONE
        } else if (followStatus.equals("follow back", ignoreCase = true)) {
            if (Variables.followMapList.containsKey(item!!.user_id)) {
                btnPromote.visibility = View.GONE
            } else {
                btnPromote.visibility = View.VISIBLE
            }
        } else {
            if (Variables.followMapList.containsKey(item!!.user_id)) {
                btnPromote.visibility = View.GONE
            } else {
                btnPromote.visibility = View.VISIBLE
            }
        }

        btnPromote.setOnClickListener { }
    }


    private fun followUnFollowUser() {
        Functions.callApiForFollowUnFollow(
            activity,
            Functions.getSharedPreference(requireContext()).getString(Variables.U_ID, ""),
            item!!.user_id,
            object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>?) {}

                override fun onSuccess(response: String) {
                    try {
                        val `object` = JSONObject(response)
                        if (`object`.optString("code") == "200") {
                            val msg = `object`.optJSONObject("msg")
                            val receiverDetailModel =
                                DataParsing.getUserDataModel(msg?.optJSONObject("User") ?: JSONObject())
                            val followStatus =
                                receiverDetailModel.button.lowercase(Locale.getDefault())
                            item!!.follow_status_button =
                                receiverDetailModel.button.lowercase(Locale.getDefault())
                            setFollowBtnStatus(receiverDetailModel.id, followStatus)
                        }
                    } catch (e: Exception) {
                        Functions.printLog(Constants.TAG_, "Exception : $e")
                    }
                }

                override fun onFail(response: String) {}
            })
    }

    private fun setFollowBtnStatus(id: String, followStatus: String) {
        var flwStatus: String? = followStatus
        if (Functions.getSharedPreference(getContext()).getBoolean(Variables.IS_LOGIN, false)) {
            if (!id.equals(
                    Functions.getSharedPreference(getContext())
                        .getString(Variables.U_ID, ""), ignoreCase = true)) {
                if (flwStatus == null) {
                    flwStatus = ""
                }

                if (flwStatus.equals("following", ignoreCase = true)) {
                    bind.ivAddFollow.visibility = View.GONE
                } else if (flwStatus.equals("friends", ignoreCase = true)) {
                    bind.ivAddFollow.visibility = View.GONE
                } else if (flwStatus.equals("follow back", ignoreCase = true)) {
                    if (Variables.followMapList.containsKey(item!!.user_id)) {
                        bind.ivAddFollow.visibility = View.GONE
                    } else {
                        bind.ivAddFollow.visibility = View.VISIBLE
                    }
                } else {
                    if (Variables.followMapList.containsKey(item!!.user_id)) {
                        bind.ivAddFollow.visibility = View.GONE
                    } else {
                        bind.ivAddFollow.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun openShareVideoView() {
        if (item == null || item!!.user_id == null) {
            return
        }

        val fragment = VideoActionF(item!!.video_id) { bundle ->
            if ((bundle.getString("action") == "save")) {
                Functions.createAppNameVideoDirectory(getContext())
                Handler(Looper.getMainLooper()).postDelayed({
                    requireActivity().runOnUiThread { saveVideo(item!!) }
                }, 500)

            } else if ((bundle.getString("action") == "repost")) {
                if (Functions.checkLoginUser(activity)) {
                    repostVideo(item!!)
                }

            } else if ((bundle.getString("action") == "duet")) {
                if (Functions.checkLoginUser(activity)) {
                    duetVideo(item!!)
                }

            } else if ((bundle.getString("action") == "privacy")) {
                onPause()
                if (Functions.checkLoginUser(activity)) {
                    openVideoSetting(item!!)
                }

            } else if ((bundle.getString("action") == "delete")) {
                if (Functions.checkLoginUser(activity)) {
                    deleteListVideo(item)
                }
            } else if ((bundle.getString("action") == "favourite")) {
                if (Functions.checkLoginUser(activity)) {
                    favouriteVideo(item)
                }

            } else if ((bundle.getString("action") == "not_intrested")) {
                if (Functions.checkLoginUser(activity)) {
                    notInterestVideo(item)
                }

            } else if ((bundle.getString("action") == "report")) {
                if (Functions.checkLoginUser(activity)) {
                    openVideoReport(item)
                }

            } else if ((bundle.getString("action") == "promotion")) {
                if (Functions.checkLoginUser(activity)) {
                    openVideoPromotion(item!!)
                }

            } else if ((bundle.getString("action") == "pinned")) {
                if (Functions.checkLoginUser(activity)) {
                    var pinnedVideo: HashMap<String?, HomeModel?> = Paper.book("PinnedVideo").read("pinnedVideo")
                    if (pinnedVideo.containsKey(item!!.video_id)) {
                        hitPinedVideo(item!!)
                    } else {
                        if (pinnedVideo.keys.size < 3) {
                            hitPinedVideo(item!!)
                        } else {
                            Toast.makeText(
                                requireContext(), getString(R.string.only_three_video_pinned_is_allow),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else if ((bundle.getString("action") == "videoShare")) {
                if (Functions.checkLoginUser(activity)) {
                    updateVideoView()
                }
            }
        }

        val bundle = Bundle()
        bundle.putString("videoId", item!!.video_id)
        bundle.putString("userId", item!!.user_id)
        bundle.putString("userName", item!!.username)
        bundle.putString("userPic", item!!.profile_pic)
        bundle.putString("fullName", item!!.first_name + " " + item!!.last_name)
        bundle.putSerializable("data", item)
        fragment.arguments = bundle
        fragment.show(childFragmentManager, "VideoActionF")
    }

    fun setData() {
        if (item != null) {
            bind.thumbImage.controller = Functions.frescoImageLoad(
                item!!.thum, bind.thumbImage, false
            )
            bind.username.text = Functions.showUsernameOnVideoSection(item)
            bind.userPic.controller = Functions.frescoImageLoad(
                item!!.profile_pic, bind.userPic, false
            )

            if (item!!.repost == "1") {
                bind.tabRepost.visibility = View.VISIBLE
                bind.ivRepostUser.controller = Functions.frescoImageLoad(
                    Functions.getSharedPreference(getContext()).getString(Variables.U_PIC, "null"),
                    bind.ivRepostUser,
                    false
                )
            } else {
                bind.tabRepost.visibility = View.GONE
            }

            if (item!!.promotionModel != null && item!!.promotionModel.id != null) {
                bind.soundName.text = getString(R.string.promoted_music)
                bind.soundName.isSelected = false

            } else {
                if (item!!.sound_name == null || item!!.sound_name == "" || item!!.sound_name == "null") {
                    bind.soundName.text = "${getString(R.string.orignal_sound_)} ${item!!.username}"
                    item!!.sound_pic = item!!.profile_pic

                } else {
                    bind.soundName.text = item!!.sound_name
                }
                bind.soundName.isSelected = true
            }

            setFollowBtnStatus(item!!.user_id, item!!.follow_status_button)

            if (item!!.sound_pic == Constants.BASE_URL) {
                bind.soundImage.controller = Functions.frescoImageLoad(
                    item!!.profile_pic, R.drawable.ic_round_music, bind.soundImage, false
                )
            } else {
                bind.soundImage.controller = Functions.frescoImageLoad(
                    item!!.sound_pic, R.drawable.ic_round_music, bind.soundImage, false
                )
            }

            //  descTxt.setText(item.video_description);
            FriendsTagHelper.Creator.create(ContextCompat.getColor(
                requireContext(),
                R.color.whiteColor
            ),
                ContextCompat.getColor(requireContext(), R.color.whiteColor)) { friendsTag ->
                var friendsTag = friendsTag
                onPause()

                if (friendsTag.contains("#")) {
                    if (friendsTag[0] == '#') {
                        friendsTag = friendsTag.substring(1)
                        openHashtag(friendsTag)
                    }
                } else if (friendsTag.contains("@")) {
                    if (friendsTag[0] == '@') {
                        friendsTag = friendsTag.substring(1)
                        openUserProfile(friendsTag)
                    }
                }
            }.handle(bind.descTxt)

            val builder = ShowMoreLess.Builder(requireContext())
                .textLengthAndLengthType(2, ShowMoreLess.TYPE_LINE)
                .showMoreLabel(getString(R.string.show_more))
                .showLessLabel(getString(R.string.show_less))
                .showMoreLabelColor(Color.parseColor("#ffffff"))
                .showLessLabelColor(Color.parseColor("#ffffff"))
                .labelUnderLine(false)
                .expandAnimation(true)
                .enableLinkify(true)
                .textClickable(false, false).build()

            builder.addShowMoreLess(bind.descTxt, item!!.video_description, false)
            bind.descTxt.setOnClickListener {
                builder.addShowMoreLess(
                    bind.descTxt,
                    item!!.video_description,
                    !builder.getContentExpandStatus()
                )
            }

            setLikeData()
            setFavouriteData()
            bind.tvShare.text = Functions.getSuffix(item!!.share)
            bind.commentLayout.visibility = if (item!!.allow_comments != null && item!!.allow_comments.equals(
                    "false", ignoreCase = true)) {
                View.GONE

            } else {
                View.VISIBLE
            }

            bind.commentTxt.text = Functions.getSuffix(item!!.video_comment_count)

            bind.varifiedBtn.visibility = if (item!!.verified != null && item!!.verified.equals("1", ignoreCase = true)) {
               View.VISIBLE
            } else {
                View.GONE
            }

            if ((item!!.duet_video_id != null) && item!!.duet_video_id != "" && item!!.duet_video_id != "0") {
                bind.duetLayoutUsername.visibility = View.VISIBLE
                bind.duetUsername.text = item!!.duet_username
            }

            if (Functions.getSharedPreference(getContext()).getBoolean(Variables.IS_LOGIN, false)) {
                bind.animateRlt.visibility = View.GONE
            }
        }
    }

    private fun setLikeData() {
        try {
            if (item!!.liked == "1") {
                bind.likebtn.animate().start()
                bind.likebtn.setLikeDrawable(
                    ContextCompat.getDrawable(requireContext(),
                        R.drawable.ic_heart_gradient
                    )
                )
                bind.likebtn.isLiked = true

            } else {
                bind.likebtn.setLikeDrawable(
                    ContextCompat.getDrawable(requireContext(),
                        R.drawable.ic_unliked
                    )
                )
                bind.likebtn.isLiked = false
                bind.likebtn.animate().cancel()
            }

        } catch (e: Exception) {
            Log.d(Constants.TAG_, "Exception: $e")
        }

        bind.likeTxt.text = Functions.getSuffix(item!!.like_count)
    }

    private fun setFavouriteData() {
        try {
            if (item!!.favourite == "1") {
                bind.tabFavourite.animate().start()
                bind.tabFavourite.setLikeDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_favourite))
                bind.tabFavourite.isLiked = true

            } else {
                bind.tabFavourite.setLikeDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_unfavourite))
                bind.tabFavourite.isLiked = false
                bind.tabFavourite.animate().cancel()
            }
        } catch (e: Exception) {
            Log.d(Constants.TAG_, "Exception: $e")
        }

       bind.tvFavourite.text = Functions.getSuffix(item!!.favourite_count)
    }

    private fun openVideoPromotion(item: HomeModel) {
        onPause()
        val intent = Intent(requireContext(), VideoPromoteStepsA::class.java)
        intent.putExtra("modelData", item)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private fun hitPinedVideo(item: HomeModel) {
        val parameters = JSONObject()
        try {
            parameters.put("video_id", item.video_id)
            currentPinStatus = if (item.pin == "1") {
                "0"
            } else {
                "1"
            }
            parameters.put("pin", currentPinStatus)

        } catch (e: Exception) {
            Log.d(Constants.TAG_, "Exception: $e")
        }

        Functions.showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.pinVideo, parameters, Functions.getHeaders(activity)) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    item.pin = currentPinStatus
                    val bundle = Bundle()
                    bundle.putString("action", "pinned")
                    bundle.putInt("position", menuPager!!.currentItem)
                    bundle.putString("pin", currentPinStatus)
                    fragmentCallBack!!.onResponce(bundle)
                    var pinnedVideo: HashMap<String?, HomeModel?> = Paper.book("PinnedVideo").read("pinnedVideo")
                    val itemUpdate = pinnedVideo[item.video_id]
                    pinnedVideo[itemUpdate!!.video_id] = itemUpdate

                    Paper.book("PinnedVideo").write("pinnedVideo", pinnedVideo)
                    val pagerAdapter = menuPager!!.adapter as ViewPagerStatAdapter
                    pagerAdapter.refreshStateSet(true)
                    pagerAdapter.notifyDataSetChanged()
                    pagerAdapter.refreshStateSet(false)
                }
            } catch (e: Exception) {
                Log.d(Constants.TAG_, "Exception: $e")
            }
        }
    }

    private fun openSoundByScreen() {
        if (item!!.sound_id == null || item!!.sound_id == "0" || item!!.sound_id == "null") {
            return
        }
        val intent = Intent(requireContext(), VideoSoundA::class.java)
        intent.putExtra("data", item)
        startActivity(intent)
    }

    private fun deleteListVideo(item: HomeModel?) {
        Functions.showLoader(activity, false, false)
        Functions.callApiForDeleteVideo(activity, item!!.video_id, object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {
                //return data in case of array list
            }

            override fun onSuccess(response: String) {
                val pagerAdapter = menuPager!!.adapter as ViewPagerStatAdapter
                val bundle = Bundle()
                bundle.putString("action", "deleteVideo")
                bundle.putInt("position", menuPager!!.currentItem)
                fragmentCallBack!!.onResponce(bundle)
                pagerAdapter.refreshStateSet(true)
                pagerAdapter.removeFragment(menuPager!!.currentItem)
                pagerAdapter.refreshStateSet(false)
            }

            override fun onFail(response: String) {}
        })
    }

    private fun openVideoSetting(item: HomeModel) {
        val intent = Intent(requireContext(), PrivacyVideoSettingA::class.java)
        intent.putExtra("video_id", item.video_id)
        intent.putExtra("privacy_value", item.privacy_type)
        intent.putExtra("duet_value", item.allow_duet)
        intent.putExtra("comment_value", item.allow_comments)
        intent.putExtra("duet_video_id", item.duet_video_id)
        resultVideoSettingCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    // initialize the player for play video
    private fun initializePlayer() {
        if (exoplayer == null && item != null) {
            try {
                exoplayer = ExoPlayer.Builder(requireContext()).setTrackSelector(
                    DefaultTrackSelector(requireContext())
                ).setLoadControl(Functions.getExoControler()).build()

                val videoURI = Uri.parse(item!!.video_url)
                val mediaItem = MediaItem.fromUri(videoURI)
                exoplayer!!.setMediaItem(mediaItem)
                exoplayer!!.prepare()

                if (item!!.promotionModel != null && item!!.promotionModel.id != null) {
                    exoplayer!!.repeatMode = Player.REPEAT_MODE_OFF
                } else {
                    exoplayer!!.repeatMode = Player.REPEAT_MODE_ALL
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build()

                exoplayer!!.setAudioAttributes(audioAttributes, true)
                exoplayer!!.addListener(this@VideosListF)

                requireActivity().runOnUiThread {
                    bind.playerview.findViewById<View>(R.id.exo_play).visibility = View.GONE
                    if (exoplayer != null) {
                        bind.playerview.player = exoplayer
                    }
                }
            } catch (e: Exception) {
                Log.d(Constants.TAG_, "Exception : $e")
            }
        }
    }

    fun setPlayer(isVisibleToUser: Boolean) {
        if (exoplayer != null) {
            if (exoplayer != null) {
                if (isVisibleToUser) {
                    exoplayer!!.playWhenReady = true

                } else {
                    exoplayer!!.playWhenReady = false
                    bind.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
                }
            }

            bind.playerview.setOnTouchListener(object : OnSwipeTouchListener(getContext()) {
                override fun onSwipeLeft() {
                    openProfile(item, true)
                }

                override fun onLongClick() {
                    if (isVisibleToUser) {
                        showVideoOption(item)
                    }
                }

                override fun onSingleClick() {
                    if (!exoplayer!!.playWhenReady) {
                        exoplayer!!.playWhenReady = true
                        bind.playerview.findViewById<View>(R.id.exo_play).alpha = 0f
                    } else {
                        exoplayer!!.playWhenReady = false
                        bind.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
                    }
                }

                override fun onDoubleClick(e: MotionEvent) {
                    if (!exoplayer!!.playWhenReady) {
                        exoplayer!!.playWhenReady = true
                    }

                    if (Functions.checkLoginUser(activity)) {
                        if (!animationRunning) {
                            if (handler != null && runnable != null) {
                                handler!!.removeCallbacks(runnable!!)
                            }

                            handler = Handler(Looper.getMainLooper())

                            runnable = Runnable {
                                if (!(item!!.liked.equals("1", ignoreCase = true))) {
                                    likeVideo(item)
                                }
                                showHeartOnDoubleTap(item, bind.mainlayout, e)
                            }

                            handler!!.postDelayed(runnable!!, 200)
                        }
                    }
                }
            })

            if (item!!.promote != null && item!!.promote == "1" && showad) {
                item!!.promote = "0"
                showAd()

            } else {
                hideAd()
            }
        }
    }

    private fun updateVideoView() {
        if (Functions.getSharedPreference(getContext()).getBoolean(Variables.IS_LOGIN, false)) {
            Functions.callApiForUpdateView(activity, item!!.video_id) { resp ->
                Functions.checkStatus(activity, resp)
                try {
                    val jsonObject = JSONObject(resp)
                    val code = jsonObject.optString("code")
                    if (code != null && code == "200") {
                        val msgObj = jsonObject.getJSONObject("msg")
                        val videoObj = msgObj.getJSONObject("Video")
                        val share = "0" + videoObj.optInt("share")
                        item!!.share = share
                        setData()
                    }
                } catch (e: Exception) {
                    Log.d(Constants.TAG_, "Exception: $e")
                }
            }
        }
        // callApiForSingleVideos();
    }

    private fun showAd() {
        bind.soundImageLayout.animation = null
        bind.sideMenu.visibility = View.GONE
        bind.videoInfoLayout.visibility = View.GONE
        bind.soundImageLayout.visibility = View.GONE
        bind.skipBtn.visibility = View.VISIBLE

        val bundle = Bundle()
        bundle.putString("action", "showad")
        fragmentCallBack!!.onResponce(bundle)
        countdownTimer(true)
    }

    fun countdownTimer(isStart: Boolean) {
        if (isStart) {
            if (countDownTimer == null) {
                countDownTimer = object : CountDownTimer((7 * 1000).toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) { }
                    override fun onFinish() {
                        if (activity != null) {
                            activity!!.runOnUiThread {
                                hideAd()
                                countdownTimer(false)
                            }
                        }
                    }
                }

                countDownTimer?.start()
            }

        } else {
            if (countDownTimer != null) {
                countDownTimer!!.cancel()
                countDownTimer = null
            }
        }
    }

    // when we swipe for another video this will release the previous player
    // hide the ad of video after some time
    fun hideAd() {
        isAddAlreadyShow = true
        bind.sideMenu.visibility = View.VISIBLE
        bind.videoInfoLayout.visibility = View.VISIBLE
        bind.soundImageLayout.visibility = View.VISIBLE
        val aniRotate = AnimationUtils.loadAnimation(getContext(), R.anim.d_clockwise_rotation)
        bind.soundImageLayout.startAnimation(aniRotate)
        bind.skipBtn.visibility = View.GONE
        val bundle = Bundle()
        bundle.putString("action", "hidead")
        fragmentCallBack!!.onResponce(bundle)
    }

    override fun setMenuVisibility(visible: Boolean) {
        isVisibleToUser = visible
        Handler(Looper.getMainLooper()).postDelayed({
            if (exoplayer != null && visible) {
                setPlayer(isVisibleToUser)
                updateVideoView()
            }
        }, 200)

        if (visible) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (view != null && activity != null) {
                    requireActivity().runOnUiThread {
                        if (item != null && item!!.user_id != null) {
                            setLikeData()
                            setFavouriteData()
                            if (item!!.block == "1") {
                                bind.tvBlockVideoMessage.text = item!!.aws_label
                                bind.tabBlockVideo.visibility = View.VISIBLE
                                onStop()

                            } else {
                                bind.tabBlockVideo.visibility = View.GONE
                            }

                            try {
                                if (item!!.storyDataList != null && item!!.storyDataList.size > 0) {
                                    bind.circleStatusBar.counts = item!!.storyDataList.size
                                    bind.circleStatusBar.visibility = View.VISIBLE
                                } else {
                                    bind.circleStatusBar.visibility = View.GONE
                                }
                            } catch (e: Exception) {
                                Log.d(Constants.TAG_, "Exception: $e")
                            }
                        }
                    }
                }
            }, 200)
        }
    }

    fun mainMenuVisibility(isvisible: Boolean) {
        if (exoplayer != null && isvisible) {
            exoplayer!!.playWhenReady = true
        } else if (exoplayer != null && !isvisible) {
            exoplayer!!.playWhenReady = false
            bind.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
        }
    }

    private fun releasePreviousPlayer() {
        if (exoplayer != null) {
            exoplayer!!.removeListener(this)
            exoplayer!!.release()
            exoplayer = null
        }
    }

    override fun onDestroy() {
        releasePreviousPlayer()
        super.onDestroy()
    }

    private fun openDuetVideo(item: HomeModel?) {
        val intent = Intent(requireContext(), WatchVideosA::class.java)
        intent.putExtra("video_id", item!!.duet_video_id)
        intent.putExtra("position", 0)
        intent.putExtra("pageCount", 0)
        intent.putExtra(
            "userId",
            Functions.getSharedPreference(requireContext()).getString(Variables.U_ID, "")
        )
        intent.putExtra("whereFrom", "IdVideo")
        startActivity(intent)
    }

    // this will open the profile of user which have uploaded the currently running video
    private fun openHashtag(tag: String) {
        val intent = Intent(requireContext(), TagedVideosA::class.java)
        intent.putExtra("tag", tag)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    // this will open the profile of user which have uploaded the currently running video
    private fun openUserProfile(tag: String) {
        if (Functions.checkProfileOpenValidationByUserName(tag)) {
            val intent = Intent(requireContext(), ProfileA::class.java)
            intent.putExtra("user_name", tag)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }

    override fun onPause() {
        super.onPause()
        if (exoplayer != null) {
            exoplayer!!.playWhenReady = false
            bind.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
        }
    }

    override fun onStop() {
        super.onStop()
        if (exoplayer != null) {
            exoplayer!!.playWhenReady = false
            bind.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Log.d(Constants.TAG_, "Exception player: " + error.message)
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        if (videoSize.width > videoSize.height) {
            bind.playerview.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        } else {
            bind.playerview.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
            bind.pBar.visibility = View.VISIBLE

        } else if (playbackState == Player.STATE_READY) {
            bind.thumbImage.visibility = View.GONE
            bind.pBar.visibility = View.GONE

        } else if (playbackState == Player.STATE_ENDED) {
            if (item!!.promotionModel != null && item!!.promotionModel.id != null) {
                bind.tabPromotionEndView.visibility = View.VISIBLE
                updatePromotionSiteAction(bind.btnWebsiteMoveSecond)
                bind.tabInnerPromotionEndView.visibility = View.VISIBLE

                bind.tvReplay.setOnClickListener {
                    exoplayer!!.seekTo(0)
                    exoplayer!!.playWhenReady = true
                    bind.tabPromotionEndView.visibility = View.GONE
                }
            }
        }
    }

    // show a heart animation on double tap
    fun showHeartOnDoubleTap(item: HomeModel?, mainLayout: RelativeLayout?, e: MotionEvent): Boolean {
        try {
            requireActivity().runOnUiThread {
                val x = e.x.toInt()
                val y = e.y.toInt()
                val lp = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                val iv: ImageView = ImageView(getApplicationContext())
                lp.setMargins(x, y, 0, 0)
                iv.layoutParams = lp
                if (item!!.liked == "1") {
                    iv.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(),
                            R.drawable.ic_heart_gradient
                        )
                    )
                }
                mainLayout!!.addView(iv)
                iv.animate().alpha(0f).translationY(-200f).setDuration(500)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            if (iv != null && mainLayout != null) {
                                mainLayout.removeView(iv)
                            }
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            super.onAnimationCancel(animation)
                            if (iv != null && mainLayout != null) {
                                mainLayout.removeView(iv)
                            }
                        }
                    }).start()
            }
        } catch (excep: Exception) {
            Functions.printLog(Constants.TAG_, "Exception : $excep")
        }
        return true
    }

    // this function will call for like the video and Call an Api for like the video
    fun likeVideo(homeModel: HomeModel?) {
        if (homeModel?.liked == null || homeModel.like_count == null || homeModel.liked == "null" || homeModel.like_count == "null") {
            return
        }

        var action = homeModel.liked
        if ((action == "1")) {
            action = "0"
            homeModel.like_count = "" + (Functions.parseInterger(homeModel.like_count) - 1)

        } else {
            action = "1"
            homeModel.like_count = "" + (Functions.parseInterger(homeModel.like_count) + 1)
        }

        homeModel.liked = action
        setLikeData()
        Functions.callApiForLikeVideo(activity, homeModel.video_id, action, null)
    }

    // this will open the comment screen
    fun openComment(item: HomeModel?) {
        if (item?.user_id == null) {
            return
        }

        val commentCount = Functions.parseInterger(item.video_comment_count)
        val fragmentDataSend: FragmentDataSend = this
        val fragment = CommentF(commentCount, fragmentDataSend)
        val args = Bundle()
        args.putString("video_id", item.video_id)
        args.putString("user_id", item.user_id)
        args.putSerializable("data", item)
        fragment.arguments = args
        fragment.show(childFragmentManager, "CommentF")
    }

    // this will open the profile of user which have uploaded the currently running video
    private fun openProfile(item: HomeModel?, fromRightToLeft: Boolean) {
        if (item?.user_id == null) {
            return
        }

        if (Functions.checkProfileOpenValidation(item.user_id)) {
            videoListCallback = FragmentCallBack { bundle ->
                if (bundle.getBoolean("isShow")) {
                    callApiForSingleVideos()
                }
            }

            val intent = Intent(requireContext(), ProfileA::class.java)
            intent.putExtra("user_id", item.user_id)
            intent.putExtra("user_name", item.username)
            intent.putExtra("user_pic", item.profile_pic)
            resultCallback.launch(intent)
            if (fromRightToLeft) {
                requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            } else {
                requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            }
        }
    }

    // show the dialog of video options
    private fun showVideoOption(homeModel: HomeModel?) {
        val alertDialog = Dialog(requireContext())
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.alert_label_editor)
        alertDialog.window!!.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.d_round_white_background)
        )

        val dialogBind = AlertLabelEditorBinding.inflate(layoutInflater)

        dialogBind.apply {
            if (homeModel!!.favourite != null && (homeModel.favourite == "1"))
                favUnfavTxt.text = getString(R.string.added_to_favourite)
            else
                favUnfavTxt.text = getString(R.string.add_to_favourite)

            if (homeModel.user_id.equals(
                    Functions.getSharedPreference(getContext()).getString(Variables.U_ID, ""),
                    ignoreCase = true)) {
                btnReport.visibility = View.GONE
                btnNotInsterested.visibility = View.GONE
                btnDelete.visibility = View.VISIBLE
            }

            btnAddToFav.setOnClickListener {
                alertDialog.dismiss()
                if (Functions.checkLoginUser(activity)) {
                    favouriteVideo(item)
                }
            }

            btnNotInsterested.setOnClickListener {
                alertDialog.dismiss()
                if (Functions.checkLoginUser(activity)) {
                    notInterestVideo(item)
                }
            }

            btnReport.setOnClickListener {
                alertDialog.dismiss()
                if (Functions.checkLoginUser(activity)) {
                    openVideoReport(item)
                }
            }

            btnDelete.setOnClickListener {
                alertDialog.dismiss()
                if (Functions.checkLoginUser(activity)) {
                    deleteListVideo(item)
                }
            }
        }

        alertDialog.show()
    }

    // this method will be favourite the video
    fun favouriteVideo(item: HomeModel?) {
        if (item == null || (item.favourite == null) || (item.favourite_count == null) || (item.favourite == "null") || (item.favourite_count == "null")) {
            return
        }

        var action = item.favourite
        if ((action == "1")) {
            action = "0"
            item.favourite_count = "" + (Functions.parseInterger(item.favourite_count) - 1)
        } else {
            action = "1"
            item.favourite_count = "" + (Functions.parseInterger(item.favourite_count) + 1)
        }

        item.favourite = action
        setFavouriteData()
        Functions.callApiForFavouriteVideo(activity, item.video_id, action, null)
    }

    // call the api if a user is not interested the video then the video will not show again to him/her
    fun notInterestVideo(item: HomeModel?) {
        val params = JSONObject()
        try {
            params.put("video_id", item!!.video_id)
            params.put("user_id", Variables.sharedPreferences?.getString(Variables.U_ID, ""))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Functions.showLoader(activity, false, false)

        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.notInterestedVideo, params, Functions.getHeaders(activity)) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if ((code == "200")) {
                    val pagerAdapter = menuPager!!.adapter as ViewPagerStatAdapter
                    val bundle = Bundle()
                    bundle.putString("action", "removeList")
                    fragmentCallBack!!.onResponce(bundle)
                    pagerAdapter.refreshStateSet(true)
                    pagerAdapter.removeFragment(menuPager!!.currentItem)
                    pagerAdapter.refreshStateSet(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openVideoReport(homeModel: HomeModel?) {
        onPause()
        val intent = Intent(requireContext(), ReportTypeA::class.java)
        intent.putExtra("video_id", homeModel?.video_id)
        intent.putExtra("isFrom", false)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    // save the video in to local directory
    fun saveVideo(item: HomeModel) {
        val params = JSONObject()
        try {
            params.put("video_id", item.video_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Functions.showLoader(activity, false, false)

        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.downloadVideo, params, Functions.getHeaders(activity)) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if ((code == "200")) {
                    var download_url = response.optString("msg")
                    if (!(download_url.isEmpty())) {
                        download_url = Functions.correctDownloadURL(download_url)
                        var downloadDirectory = ""
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            downloadDirectory = Functions.getAppFolder(requireContext())

                        } else {
                            downloadDirectory =
                                Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DCIM
                                ).toString() + File.separator + getString(
                                    R.string.app_name
                                ) + File.separator + Variables.VideoDirectory + File.separator
                        }

                        Functions.showDeterminentLoader(
                            activity, false, false, true, getString(
                                R.string.download_
                            )
                        )

                        PRDownloader.initialize(requireContext())
                        val prDownloader = PRDownloader.download(
                            download_url,
                            downloadDirectory,
                            item.video_id + ".mp4"
                        )
                            .build()
                            .setOnProgressListener { progress ->
                                val prog =
                                    ((progress.currentBytes * 100) / progress.totalBytes).toInt()
                                Functions.showLoadingProgress(prog)
                            }
                        val finalDownloadDirectory = downloadDirectory
                        val finalDownload_url = download_url
                        prDownloader.start(object : OnDownloadListener {
                            override fun onDownloadComplete() {
                                Functions.cancelDeterminentLoader()
                                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                                    downloadAEVideo(
                                        finalDownloadDirectory,
                                        item.video_id + ".mp4"
                                    )
                                } else {
                                    deleteWaterMarkVideo(finalDownload_url)
                                    scanFile(finalDownloadDirectory)
                                }
                            }

                            override fun onError(error: Error) {
                                Functions.printLog(
                                    Constants.TAG_,
                                    "Error : " + error.connectionException
                                )
                                Functions.cancelDeterminentLoader()
                            }
                        })
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun downloadAEVideo(path: String, videoName: String) {
        val valuesvideos: ContentValues = ContentValues()
        valuesvideos.put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            Environment.DIRECTORY_DCIM + File.separator + getString(
                R.string.app_name
            ) + File.separator + Variables.VideoDirectory
        )
        valuesvideos.put(MediaStore.MediaColumns.TITLE, videoName)
        valuesvideos.put(MediaStore.MediaColumns.DISPLAY_NAME, videoName)
        valuesvideos.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        valuesvideos.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        valuesvideos.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
        valuesvideos.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver = requireActivity().contentResolver
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uriSavedVideo = resolver.insert(collection, valuesvideos)
        val pfd: ParcelFileDescriptor?

        try {
            pfd = requireActivity().contentResolver.openFileDescriptor((uriSavedVideo)!!, "w")
            val out = FileOutputStream(pfd!!.fileDescriptor)
            val imageFile = File(path + videoName)
            val `in` = FileInputStream(imageFile)
            val buf = ByteArray(1024)
            var len: Int
            while ((`in`.read(buf).also { len = it }) > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            `in`.close()
            pfd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        valuesvideos.clear()
        valuesvideos.put(MediaStore.MediaColumns.IS_PENDING, 0)
        requireContext().contentResolver.update(uriSavedVideo!!, valuesvideos, null, null)
    }

    fun deleteWaterMarkVideo(videoUrl: String?) {
        val params = JSONObject()
        try {
            params.put("video_url", videoUrl)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.deleteWaterMarkVideo, params, Functions.getHeaders(
                activity
            ), null
        )
    }

    fun scanFile(downloadDirectory: String) {
        MediaScannerConnection.scanFile(activity, arrayOf(downloadDirectory + item!!.video_id + ".mp4"), null) { _, _ -> }
    }

    // download the video for duet with
    fun duetVideo(item: HomeModel) {
        Functions.printLog(Constants.TAG_, item.video_url)
        if (item.video_url != null) {
            var downloadedFile = item.video_url
            if (downloadedFile.contains("file://")) {
                downloadedFile = item.video_url.replace("file://", "")
                val file = File(downloadedFile)
                if (file.exists()) {
                    val outputPath = Functions.getAppFolder(
                        activity
                    ) + item.video_id + ".mp4"
                    copyDirectoryOneLocationToAnotherLocation(file, File(outputPath))
                }
            }

            val deletePath = Functions.getAppFolder(activity) + item.video_id + ".mp4"
            val deleteFile = File(deletePath)
            if (deleteFile.exists()) {
                openDuetRecording(item)
                return
            }

            Functions.showDeterminentLoader(
                activity, false, false,
                true, getString(R.string.download_)
            )

            PRDownloader.initialize(requireContext())
            val prDownloader = PRDownloader.download(
                item.video_url, Functions.getAppFolder(activity), item.video_id + ".mp4"
            )
                .build()
                .setOnProgressListener { progress ->
                    val prog = ((progress.currentBytes * 100) / progress.totalBytes).toInt()
                    Functions.showLoadingProgress(prog)
                }

            prDownloader.start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Functions.cancelDeterminentLoader()
                    openDuetRecording(item)
                }

                override fun onError(error: Error) {
                    Functions.printLog(Constants.TAG_, "Error : " + error.connectionException)
                    Functions.cancelDeterminentLoader()
                }
            })
        }
    }

    private fun repostVideo(item: HomeModel) {
        val parameters = JSONObject()
        try {
            parameters.put("repost_user_id",
                Functions.getSharedPreference(getContext()).getString(Variables.U_ID, ""))
            parameters.put("video_id", item.video_id)
            parameters.put("repost_comment", "")

        } catch (e: Exception) {
            e.printStackTrace()
        }

        Functions.showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.repostVideo, parameters, Functions.getHeaders(activity)) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()

            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if ((code == "200")) {
                    Functions.showToast(context, "Successfully repost video!")
                    if (item.repost != null && (item.repost == "0")) {
                        item.repost = "1"
                        try {
                            val msg = response.getJSONObject("msg")
                            val video = msg.getJSONObject("Video")
                            item.repost_video_id = video.optString("repost_video_id", "0")
                            item.repost_user_id = video.optString("repost_user_id", "0")
                        } catch (e: Exception) {
                            Log.d(Constants.TAG_, "Exception: $e")
                        }
                    } else {
                        item.repost = "0"
                    }
                    setData()
                }
            } catch (e: Exception) {
                Log.d(Constants.TAG_, "Exception: $e")
            }
        }
    }

    private fun copyDirectoryOneLocationToAnotherLocation(sourceLocation: File, targetLocation: File) {
        try {
            if (sourceLocation.isDirectory) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdir()
                }
                val children = sourceLocation.list()
                for (i in sourceLocation.listFiles().indices) {
                    copyDirectoryOneLocationToAnotherLocation(
                        File(sourceLocation, children[i]),
                        File(targetLocation, children[i])
                    )
                }

            } else {
                val `in`: InputStream = FileInputStream(sourceLocation)
                val out: OutputStream = FileOutputStream(targetLocation)

                // Copy the bits from in-stream to out-stream
                val buf = ByteArray(1024)
                var len: Int
                while ((`in`.read(buf).also { len = it }) > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
            }
        } catch (e: Exception) {
            Log.d(Constants.TAG_, "Exception: $e")
        }
    }

    fun openDuetRecording(item: HomeModel?) {
        val isOpenGLSupported = Functions.isOpenGLVersionSupported(getContext(), 0x00030001)
        if (isOpenGLSupported) {
            val intent = Intent(activity, VideoRecoderDuetA::class.java)
            intent.putExtra("data", item)
            startActivity(intent)

        } else {
            Toast.makeText(
                getContext(), getString(R.string.your_device_opengl_verison_is_not_compatible_to_use_this_feature),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // call api to refresh the video details
    private fun callApiForSingleVideos() {
        val parameters = JSONObject()
        try {
            if (Variables.sharedPreferences?.getString(Variables.U_ID, null) != null) parameters.put(
                "user_id",
                Variables.sharedPreferences?.getString(Variables.U_ID, "0")
            )
            parameters.put("video_id", item!!.video_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.showVideoDetail, parameters, Functions.getHeaders(activity)) { resp ->
            Functions.checkStatus(activity, resp)
            signalVideoParseData(resp)
        }
    }

    // parse the data for a video
    private fun signalVideoParseData(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONObject("msg")
                val video = msg?.optJSONObject("Video")
                val user = msg?.optJSONObject("User")
                val sound = msg?.optJSONObject("Sound")
                val userPrivacy = user?.optJSONObject("PrivacySetting")
                val userPushNotification = user?.optJSONObject("PushNotification")

                item = Functions.parseVideoData(user, sound, video, userPrivacy, userPushNotification)
                setData()
            } else {
                Functions.showToast(activity, jsonObject.optString("msg"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDataSent(yourData: String) {
        val commentCount = Functions.parseInterger(yourData)
        item!!.video_comment_count = "$commentCount"
        bind.commentTxt.text = Functions.getSuffix(item!!.video_comment_count)
    }

    override fun onDetach() {
        super.onDetach()
        mPermissionResult.unregister()
    }

    companion object {
        @JvmField
        var videoListCallback: FragmentCallBack? = null
    }
}