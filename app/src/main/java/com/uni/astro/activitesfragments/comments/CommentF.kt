package com.uni.astro.activitesfragments.comments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.record_view.OnRecordListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hendraanggrian.appcompat.widget.SocialView
import com.uni.astro.Constants
import com.uni.astro.Constants.TAG_
import com.uni.astro.R
import com.uni.astro.activitesfragments.comments.adapters.CommentsAdapter
import com.uni.astro.activitesfragments.comments.adapters.Comments_Reply_Adapter.OnRelyItemCLickListener
import com.uni.astro.activitesfragments.comments.adapters.Comments_Reply_Adapter.LinkClickListener
import com.uni.astro.activitesfragments.comments.adapters.Comments_Reply_Adapter.OnLongClickListener
import com.uni.astro.activitesfragments.comments.voice.VoiceCommentManager
import com.uni.astro.activitesfragments.profile.ProfileA
import com.uni.astro.apiclasses.ApiLinks
import com.uni.astro.databinding.FragmentCommentBinding
import com.uni.astro.interfaces.FragmentDataSend
import com.uni.astro.models.CommentModel
import com.uni.astro.models.HomeModel
import com.uni.astro.models.UsersModel
import com.uni.astro.simpleclasses.DataParsing
import com.uni.astro.simpleclasses.DebounceClickHandler
import com.uni.astro.simpleclasses.Functions
import com.uni.astro.simpleclasses.Functions.deleteDir
import com.uni.astro.simpleclasses.PermissionUtils
import com.uni.astro.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import org.json.JSONObject
import java.io.File

class CommentF(count: Int, var fragmentDataSend: FragmentDataSend?) : BottomSheetDialogFragment() {
    private lateinit var bind: FragmentCommentBinding


    private var homeModel: HomeModel = HomeModel()
    var commentsAdapter: CommentsAdapter? = null

    var dataList: ArrayList<CommentModel> = ArrayList()
    private var taggedUserList: ArrayList<UsersModel>? = ArrayList()

    var userId: String? = null
    var videoId: String? = null
    var replyStatus: String? = null
    private var commentType = "OwnComment"

    var ispostFinsh = false
    private var isSendAllow = true

    var pageCount = 0
    var selectedCommentPosition = 0
    var selectedReplyCommentPosition = 0

    var selectedComment: CommentModel? = null
    var selectedReplyComment: CommentModel? = null

    var dialog: BottomSheetDialog? = null

    private lateinit var mBehavior: BottomSheetBehavior<View>


    // Voice Comment Variables
    var audioPermissionCheck = "player"
    private var sendAudio: VoiceCommentManager? = null
    private var permissionUtils: PermissionUtils? = null
    var takePermissionUtils: PermissionUtils? = null


    companion object {
        @JvmStatic
        private var commentsCount = 0
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val dialogView = FragmentCommentBinding.inflate(layoutInflater)

        dialog?.setContentView(dialogView.root)
        mBehavior = BottomSheetBehavior.from(dialogView.root.parent as View)

        mBehavior.isHideable = false
        mBehavior.isDraggable = false
        mBehavior.setPeekHeight(resources.getDimension(R.dimen._450sdp).toInt(), true)

        mBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState != BottomSheetBehavior.STATE_EXPANDED) {
                    mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) { }
        })

        return dialog as BottomSheetDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FragmentCommentBinding.inflate(inflater, container, false)

        bind.apply {
            tvComment.setOnClickListener(DebounceClickHandler {
                replyStatus = null
                hitComment()
            })

            goBack.setOnClickListener(DebounceClickHandler {
                dismiss()
            })


            val bundle = arguments
            if (bundle != null) {
                videoId = bundle.getString("video_id")
                userId = bundle.getString("user_id")
                homeModel = bundle.getSerializable("data") as HomeModel
            }

            isSendAllow = if (Functions.isShowContentPrivacy(context, homeModel.apply_privacy_model.video_comment,
                    homeModel.follow_status_button.equals("friends", ignoreCase = true))) {
                writeLayout.visibility = View.VISIBLE
                true

            } else {
                writeLayout.visibility = View.GONE
                false
            }


            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.orientation = RecyclerView.VERTICAL
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.setHasFixedSize(true)

            commentsAdapter = CommentsAdapter(requireContext(), dataList,
                object : CommentsAdapter.OnItemClickListener {
                    override fun onItemClick(positon: Int, itemUpdate: CommentModel, view: View) {
                        selectedCommentPosition = positon
                        selectedComment = dataList[selectedCommentPosition]

                        when (view.id) {
                            R.id.tabUserPic, R.id.user_pic, R.id.username -> {
                                openProfile(selectedComment)
                            }

                            R.id.tabMessageReply -> {
                                if (Functions.checkLoginUser(activity)) {
                                    replyStatus = "reply"
                                    selectedReplyComment = null
                                    hitComment()
                                }
                            }

                            R.id.like_layout -> {
                                if (Functions.checkLoginUser(activity)) {
                                    likeComment(selectedCommentPosition, selectedComment)
                                }
                            }

                            R.id.reply_count -> {
                                selectedComment!!.isExpand = !selectedComment!!.isExpand
                                dataList[selectedCommentPosition] = selectedComment as CommentModel
                                commentsAdapter?.notifyDataSetChanged()
                            }

                            R.id.show_less_txt -> {
                                selectedComment!!.isExpand = false
                                dataList[selectedCommentPosition] = selectedComment as CommentModel
                                commentsAdapter?.notifyDataSetChanged()
                            }

                            R.id.voiceCommentPanel -> {
                                /*
                                Log.d(Constants.TAG_, "onItemC1lick: " + view.id)
                                Toast.makeText(requireContext(), "audio bubble clicked", Toast.LENGTH_SHORT).show()

                                selectedComment?.isPlaying = true
                                selectedAudioView = view
                                selectedAudioPosition = selectedCommentPosition

                                takePermissionUtils = PermissionUtils(requireActivity(), permissionStorageRecordingResult)

                                audioPermissionCheck = "playing"
                                playingId = selectedComment!!.comment_id
                                if (takePermissionUtils!!.isStorageRecordingPermissionGranted) {
                                    audioPlaying(selectedAudioView, selectedComment, selectedAudioPosition)

                                } else {
                                    takePermissionUtils!!.showStorageRecordingPermissionDailog(
                                        getString(R.string.we_need_recording_permission_for_upload_sound)
                                    )
                                }
                                */
                            }
                        }
                    }

                    override fun onItemLongPress(positon: Int, itemUpdate: CommentModel, view: View) {
                        selectedCommentPosition = positon
                        selectedComment = dataList[selectedCommentPosition]
                        if (view.id == R.id.commentPanel) {
                            openCommentSetting(selectedComment, selectedCommentPosition)
                        }
                    }
                },

                object : OnRelyItemCLickListener {
                    override fun onItemClick(arrayList: ArrayList<CommentModel>, postion: Int, view: View) {
                        selectedReplyCommentPosition = postion
                        selectedReplyComment = arrayList[selectedReplyCommentPosition]
                        when (view.id) {
                            R.id.user_pic, R.id.username -> openProfile(
                                arrayList[selectedReplyCommentPosition]
                            )

                            R.id.tabMessageReply -> {
                                replyStatus = "commentReply"
                                hitComment()
                            }

                            R.id.like_layout -> if (Functions.checkLoginUser(activity)) {
                                likeCommentReply()
                            }
                        }
                    }

                    override fun onItemLongPress(arrayList: ArrayList<CommentModel>, postion: Int, view: View) {
                        selectedReplyCommentPosition = postion
                        selectedReplyComment = arrayList[selectedReplyCommentPosition]
                        if (view.id == R.id.reply_layout) {
                            Functions.copyCode(
                                view.context,
                                selectedReplyComment!!.comment_reply
                            )
                        }
                    }

                },

                object : LinkClickListener {
                    override fun onLinkClicked(view: SocialView?, matchedText: String?) {
                        openProfileByUsername(matchedText ?: "")
                    }
                },

                { bundle1 ->
                    if (bundle1.getBoolean("isShow")) {
                        openTagUser(bundle1.getString("name"))
                    }
                },

                object : OnLongClickListener {
                    override fun onLongclick(item: CommentModel?, view: View?) {
                        openCommentSetting(item, selectedCommentPosition)
                    }
                }
            )

            recyclerView.adapter = commentsAdapter
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var userScrolled = false
                var scrollOutitems = 0

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        userScrolled = true
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    scrollOutitems = linearLayoutManager.findLastVisibleItemPosition()

                    if (userScrolled && scrollOutitems == dataList.size - 1) {
                        userScrolled = false
                        if (loadMoreProgress.visibility != View.VISIBLE && !ispostFinsh) {
                            loadMoreProgress.visibility = View.VISIBLE
                            pageCount += 1
                            getComments()
                        }
                    }
                }
            })


            sendAudio = VoiceCommentManager(videoId!!, requireActivity())
            takePermissionUtils = PermissionUtils(requireActivity(), permissionStorageRecordingResult)


            if (homeModel.apply_privacy_model.video_comment.equals("everyone", ignoreCase = true)
                || homeModel.apply_privacy_model.video_comment.equals("friend", ignoreCase = true)
                && homeModel.follow_status_button.equals("friends", ignoreCase = true)) {

                writeLayout.visibility = View.VISIBLE
                getComments()

            } else {
                noDataLoader.visibility = View.GONE
                writeLayout.visibility = View.GONE

                tvNoCommentData.text = getString(R.string.comments_are_turned_off)
                commentCount.text = "0 ${getString(R.string.comments)}"
                tvNoCommentData.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }


            // Init Voice Comment
            bind.btnMic.setRecordView(recordView)
            recordView.setSoundEnabled(true)


            recordView.setOnRecordListener(object : OnRecordListener {
                override fun onStart() {
                    permissionUtils = PermissionUtils(activity, permissionStorageRecordingResult)
                    audioPermissionCheck = "recording"
                    if (takePermissionUtils!!.isStorageRecordingPermissionGranted) {
                        requireActivity().runOnUiThread {
                            recordPanel.visibility = View.VISIBLE
                            recordView.visibility = View.VISIBLE
                            tvComment.visibility = View.GONE
                        }

                        sendAudio?.startVoiceRecording(requireActivity())

                    } else {
                        takePermissionUtils?.showStorageRecordingPermissionDailog(getString(R.string.we_need_recording_permission_for_upload_sound))
                    }
                }

                override fun onCancel() {
                    resetRecordViews()
                    sendAudio?.stopTimer()
                }

                override fun onFinish(recordDuration: Long) {
                    requireActivity().runOnUiThread {
                        recordPanel.visibility = View.VISIBLE
                        playRecordPanel.visibility = View.VISIBLE
                        recordView.visibility = View.GONE
                        tvComment.visibility = View.GONE
                        btnMic.visibility = View.GONE
                    }

                    val recordFile = sendAudio?.finishRecording(requireActivity())
                    val mediaPlayer = MediaPlayer.create(context, Uri.parse(recordFile))

                    requireActivity().runOnUiThread {
                        recordTime.text = "${Functions.getfileduration(requireContext(), Uri.parse(recordFile))}"
                    }

                    btnSendVoice.setOnClickListener {
                        btnSendVoice.visibility = View.GONE
                        btnDeleteRecord.visibility = View.GONE
                        voiceProgress.visibility = View.VISIBLE

                        sendAudio?.uploadAudio(requireActivity()) { resx ->
                            if (resx == "200") {
                                Toast.makeText(requireContext(), "Voice Comment Added.", Toast.LENGTH_SHORT).show()
                                resetRecordViews()
                                getComments()

                            } else {
                                Toast.makeText(requireContext(), "Voice Comment Upload Failed.", Toast.LENGTH_SHORT).show()
                                resetRecordViews()
                            }
                        }
                    }

                    btnDeleteRecord.setOnClickListener {
                        Toast.makeText(requireContext(), "Record Deleted Successfully.", Toast.LENGTH_SHORT).show()
                        deleteDir(File(recordFile))
                        resetRecordViews()
                    }

                    var togglePlay = false
                    btnPlay.setOnClickListener(DebounceClickHandler {
                        if (!togglePlay) {
                            mediaPlayer.start()
                            btnPlay.setImageResource(R.drawable.ic_pause_icon)

                            val timr = object : CountDownTimer(recordDuration, 100) {
                                override fun onTick(millisUntilFinished: Long) {
                                    seekBar.progress = mediaPlayer.currentPosition
                                }

                                override fun onFinish() {
                                    btnPlay.setImageResource(R.drawable.ic_play_icon)
                                    seekBar.progress = 0
                                }
                            }
                            timr.start()

                            togglePlay = true

                        } else {
                            togglePlay = false
                            mediaPlayer.pause()
                            btnPlay.setImageResource(R.drawable.ic_play_icon)
                        }
                    })

                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                mediaPlayer.seekTo(progress)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) { }
                        override fun onStopTrackingTouch(seekBar: SeekBar) { }
                    })
                }

                override fun onLessThanSecond() {
                    Toast.makeText(requireContext(), "Record Is Too Short...", Toast.LENGTH_SHORT).show()
                    resetRecordViews()
                }
            })


            recordView.setSlideToCancelText(getString(R.string.slide_to_cancel))
            btnMic.isListenForRecord = true
            recordView.setLessThanSecondAllowed(false)
        }

        return bind.root
    }

    private fun openTagUser(tag: String?) {
        if (Functions.checkProfileOpenValidationByUserName(tag)) {
            val intent = Intent(requireContext(), ProfileA::class.java)
            intent.putExtra("user_name", tag)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }

    @SuppressLint("SetTextI18n")
    fun resetRecordViews() {
        bind.apply {
            requireActivity().runOnUiThread {
                recordPanel.visibility = View.GONE
                sendProgress.visibility = View.GONE
                voiceProgress.visibility = View.GONE
                playRecordPanel.visibility = View.GONE
                btnDeleteRecord.visibility = View.GONE
                btnMic.visibility = View.VISIBLE
                tvComment.visibility = View.VISIBLE
                recordView.visibility = View.VISIBLE
                btnSendVoice.visibility = View.VISIBLE
                btnPlay.setImageResource(R.drawable.ic_play_icon)
                seekBar.progress = 0
                recordTime.text = "00:00"
            }
        }
    }

    private fun hitComment() {
        var replyStr = ""
        if (replyStatus == null) {
            commentType = "OwnComment"

        } else if (replyStatus == "commentReply") {
            replyStr = "${getString(R.string.reply_to)} ${selectedReplyComment!!.replay_user_name}"
            commentType = "replyComment"

        } else {
            replyStr = "${getString(R.string.reply_to)} ${selectedComment!!.user_name}"
            commentType = "replyComment"
        }

        val fragment = EditTextSheetF(commentType, taggedUserList) { bundle: Bundle ->
            if (bundle.getBoolean("isShow", false)) {
                if (bundle.getString("action") == "sendComment") {
                    taggedUserList =
                        bundle.getSerializable("taggedUserList") as ArrayList<UsersModel>?
                    val message = bundle.getString("message")
                    bind.tvComment.text = message
                    sendComment("$message")
                }
            }
        }

        val bundle = Bundle()
        bundle.putString("replyStr", replyStr)
        fragment.arguments = bundle
        fragment.show(childFragmentManager, "EditTextSheetF")
    }

    private fun sendComment(message: String) {
        var message = message
        if (!TextUtils.isEmpty(message)) {
            if (Functions.checkLoginUser(activity)) {
                if (replyStatus == null) {
                    sendComments(videoId, message)

                } else if (replyStatus == "commentReply") {
                    message = "${getString(R.string.replied_to)} @${selectedReplyComment!!.replay_user_name} $message"
                    sendCommentsReply(
                        selectedReplyComment!!.parent_comment_id,
                        message, videoId, selectedReplyComment!!.videoOwnerId
                    )

                } else {
                    Log.d(Constants.TAG_, "HitAPI here comment_id " + selectedComment!!.comment_id)
                    sendCommentsReply(
                        selectedComment!!.comment_id,
                        message, videoId, selectedComment!!.videoOwnerId
                    )
                }

                bind.tvComment.text = getString(R.string.leave_a_comment)
            }
        }
    }

    private fun openCommentSetting(commentModel: CommentModel?, position: Int) {
        val fragment = CommentSettingF(commentModel) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                if (bundle.getString("action") == "copyText") {
                    Functions.copyCode(requireContext(), commentModel?.commentText)

                } else if (bundle.getString("action") == "pinComment") {
                    if (Integer.valueOf(commentModel?.pin_comment_id) > 0) {
                        if (commentModel?.pin_comment_id == commentModel?.comment_id) {
                            hitApiPinComment(commentModel, "unpin")

                        } else {
                            replacePreviousPinned(commentModel)
                        }

                    } else {
                        hitApiPinComment(commentModel, "pin")
                    }

                } else if (bundle.getString("action") == "deleteComment") {
                    hitApiCommentDelete(commentModel, position)
                }
            }
        }

        fragment.show(parentFragmentManager, "CommentSettingF")
    }

    private fun replacePreviousPinned(commentModel: CommentModel?) {
        Functions.showDoubleButtonAlert(
           requireContext(),
            getString(R.string.pin_this_comment),
            getString(R.string.pinning_description),
            getString(R.string.cancel_),
            getString(R.string.pin_and_replace),
            false) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                hitApiPinComment(commentModel, "pin")
            }
        }
    }

    private fun hitApiPinComment(commentModel: CommentModel?, pinHitStatus: String) {
        val parameters = JSONObject()
        try {
            parameters.put("video_id", commentModel!!.video_id)
            var commentPin: String? = ""
            commentPin = if (pinHitStatus == "unpin") {
                "0"
            } else {
                commentModel.comment_id
            }
            parameters.put("pin_comment_id", commentPin)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Functions.showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(activity, ApiLinks.pinComment, parameters, Functions.getHeaders(activity)) { resp ->

            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    if (pinHitStatus == "pin") {
                        val msgObj = response.getJSONObject("msg")
                        val videoObj = msgObj.getJSONObject("Video")
                        val pinnedCommentId = videoObj.optString("pin_comment_id")
                        for (itemDataUpdate in dataList) {
                            itemDataUpdate.pin_comment_id = pinnedCommentId
                            dataList[dataList.indexOf(itemDataUpdate)] = itemDataUpdate
                        }
                    } else {
                        for (itemDataUpdate in dataList) {
                            itemDataUpdate.pin_comment_id = "0"
                            dataList[dataList.indexOf(itemDataUpdate)] = itemDataUpdate
                        }
                    }
                    commentsAdapter?.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.d(Constants.TAG_, "Exception: $e")
            }
        }
    }

    private fun hitApiCommentDelete(commentModel: CommentModel?, position: Int) {
        val parameters = JSONObject()
        try {
            parameters.put("id", commentModel?.comment_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Functions.showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(activity, ApiLinks.deleteVideoComment, parameters, Functions.getHeaders(activity)) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()

            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    if (commentModel!!.comment_id == commentModel.pin_comment_id) {
                        for (itemDataUpdate in dataList) {
                            itemDataUpdate.pin_comment_id = "0"
                            dataList[dataList.indexOf(itemDataUpdate)] = itemDataUpdate
                        }
                        dataList.removeAt(position)
                    } else {
                        dataList.removeAt(position)
                    }

                    commentsAdapter?.notifyDataSetChanged()
                    commentsCount = dataList.size
                    bind.commentCount.text = "$commentsCount ${getString(R.string.comments)}"
                    if (fragmentDataSend != null) fragmentDataSend!!.onDataSent("" + commentsCount)
                }
            } catch (e: Exception) {
                Log.d(Constants.TAG_, "Exception: $e")
            }
        }
    }

    private fun likeCommentReply() {
        val itemUpdate = dataList[selectedCommentPosition]
        val replyList = itemUpdate.arrayList
        val itemReplyUpdate = replyList[selectedReplyCommentPosition]
        var action = itemReplyUpdate.comment_reply_liked

        if (action != null) {
            if (action == "1") {
                action = "0"
                itemReplyUpdate.reply_liked_count =
                    "" + (Functions.parseInterger(itemReplyUpdate.reply_liked_count) - 1)
            } else {
                action = "1"
                itemReplyUpdate.reply_liked_count =
                    "" + (Functions.parseInterger(itemReplyUpdate.reply_liked_count) + 1)
            }
        }

        itemReplyUpdate.comment_reply_liked = action
        Functions.callApiForLikeCommentReply(activity, itemReplyUpdate.comment_reply_id, videoId, object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {}
            override fun onSuccess(responce: String) {
                try {
                    val jsonObject = JSONObject(responce)
                    if (jsonObject.optString("code") == "200") {
                        if (jsonObject.optString("msg") == "unfavourite") {
                            itemReplyUpdate.isLikedByOwner = "0"
                            replyList[selectedReplyCommentPosition] = itemReplyUpdate
                            itemUpdate.arrayList = replyList
                            dataList[selectedCommentPosition] = itemUpdate
                            commentsAdapter?.notifyDataSetChanged()

                        } else {
                            val msgObj = jsonObject.getJSONObject("msg")
                            val videoLikeComment = msgObj.getJSONObject("VideoCommentReplyLike")
                            itemReplyUpdate.isLikedByOwner =
                                videoLikeComment.optString("owner_like")
                            replyList[selectedReplyCommentPosition] = itemReplyUpdate
                            itemUpdate.arrayList = replyList
                            dataList[selectedCommentPosition] = itemUpdate
                            commentsAdapter?.notifyDataSetChanged()
                        }
                    }
                } catch (e: Exception) {
                    Log.d(Constants.TAG_, "Exception: $e")
                }
            }

            override fun onFail(responce: String) {}
        })
    }

    private fun likeComment(position: Int, commentModel: CommentModel?) {
        var action = commentModel?.liked
        if (action != null) {
            if (action == "1") {
                action = "0"
                commentModel?.like_count = "" + (Functions.parseInterger(commentModel?.like_count) - 1)
            } else {
                action = "1"
                commentModel?.like_count = "" + (Functions.parseInterger(commentModel?.like_count) + 1)
            }

            Log.d(
                Constants.TAG_,
                "Check UserId and Owner Id" + commentModel?.userId + "      " + commentModel?.videoOwnerId
            )

            if (userId == commentModel?.videoOwnerId) {
                if (commentModel?.userId == commentModel?.videoOwnerId) {
                    commentModel?.isLikedByOwner = "1"
                } else {
                    commentModel?.isLikedByOwner = "0"
                }
            }

            commentModel?.liked = action

            Functions.callApiForLikeComment(activity, commentModel?.comment_id, object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>) {
                    Log.d(Constants.TAG_, "DataCheck: " + arrayList.size)
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onSuccess(responce: String) {
                    try {
                        val jsonObject = JSONObject(responce)
                        if (jsonObject.optString("code") == "200") {
                            if (jsonObject.optString("msg") == "unfavourite") {
                                if (userId == commentModel?.videoOwnerId) {
                                    commentModel?.isLikedByOwner = "0"
                                }
                                dataList[position] = commentModel as CommentModel
                                commentsAdapter?.notifyDataSetChanged()

                            } else {
                                val msgObj = jsonObject.getJSONObject("msg")
                                val videoLikeComment = msgObj.getJSONObject("VideoCommentLike")
                                if (userId == commentModel?.videoOwnerId) {
                                    commentModel?.isLikedByOwner = videoLikeComment.optString("owner_like")
                                }

                                dataList[position] = commentModel as CommentModel
                                commentsAdapter?.notifyDataSetChanged()
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(Constants.TAG_, "Exception: $e")
                    }
                }

                override fun onFail(responce: String) {}
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getComments() {
        bind.apply {
            if (dataList.isEmpty()) {
                noDataLoader.visibility = View.VISIBLE
            }

            val parameters = JSONObject()
            try {
                parameters.put("video_id", videoId)
                if (Functions.getSharedPreference(requireContext()).getBoolean(Variables.IS_LOGIN, false)) {
                    parameters.put(
                        "user_id", Functions.getSharedPreference(requireContext()).getString(Variables.U_ID, "0")
                    )
                }
                parameters.put("starting_point", "$pageCount")
            } catch (e: Exception) {
                e.printStackTrace()
            }


            VolleyRequest.JsonPostRequest(activity, ApiLinks.showVideoComments, parameters, Functions.getHeaders(activity)) { resp ->
                Functions.checkStatus(activity, resp)
                noDataLoader.visibility = View.GONE
                var pinnedCommentModel: CommentModel? = null
                val tempList = ArrayList<CommentModel>()

                try {
                    val response = JSONObject(resp)
                    val code = response.optString("code")
                    if (code == "200") {
                        val msgArray = response.getJSONArray("msg")

                        for (i in 0 until msgArray.length()) {
                            val itemdata = msgArray.optJSONObject(i)
                            val videoComment = itemdata.optJSONObject("VideoComment")
                            val videoObj = itemdata.optJSONObject("Video")
                            val userDetailModel = DataParsing.getUserDataModel(itemdata.optJSONObject("User"))
                            val videoCommentReply = itemdata.optJSONArray("VideoCommentReply")
                            val replyList = ArrayList<CommentModel>()

                            if (videoCommentReply.length() > 0) {
                                for (j in 0 until videoCommentReply.length()) {
                                    val jsonObject = videoCommentReply.getJSONObject(j)
                                    val userDetailModelReply = DataParsing.getUserDataModel(jsonObject.optJSONObject("User"))

                                    val commntModel = CommentModel()
                                    commntModel.comment_reply_id = jsonObject.optString("id")
                                    commntModel.type = jsonObject.optString("type")
                                    commntModel.reply_liked_count = jsonObject.optString("like_count")
                                    commntModel.comment_reply_liked = jsonObject.optString("like")
                                    commntModel.comment_reply = jsonObject.optString("comment")
                                    commntModel.created = jsonObject.optString("created")
                                    commntModel.videoOwnerId = videoObj.optString("user_id")
                                    commntModel.replay_user_name = userDetailModelReply.username
                                    commntModel.replay_user_url = userDetailModelReply.profilePic
                                    commntModel.userId = userDetailModelReply.id
                                    commntModel.isVerified = userDetailModelReply.verified
                                    commntModel.parent_comment_id = videoComment.optString("id")
                                    commntModel.isLikedByOwner = jsonObject.optString("owner_like")
                                    replyList.add(commntModel)
                                }
                            }

                            val commsModel = CommentModel()
                            commsModel.isLikedByOwner = videoComment.optString("owner_like")
                            commsModel.videoOwnerId = videoObj.optString("user_id")
                            commsModel.pin_comment_id = videoObj.optString("pin_comment_id", "0")
                            commsModel.userId = userDetailModel.id
                            commsModel.isVerified = userDetailModel.verified
                            commsModel.user_name = userDetailModel.username
                            commsModel.first_name = userDetailModel.firstName
                            commsModel.last_name = userDetailModel.lastName
                            commsModel.arraylist_size = videoCommentReply.length().toString()
                            commsModel.profile_pic = userDetailModel.profilePic
                            commsModel.arrayList = replyList
                            commsModel.video_id = videoComment.optString("video_id")
                            commsModel.type = videoComment.optString("type")
                            commsModel.commentText = videoComment.optString("comment")
                            commsModel.liked = videoComment.optString("like")
                            commsModel.like_count = videoComment.optString("like_count")
                            commsModel.comment_id = videoComment.optString("id")
                            commsModel.created = videoComment.optString("created")

                            if (commsModel.comment_id == commsModel.pin_comment_id) {
                                pinnedCommentModel = commsModel
                            } else {
                                tempList.add(commsModel)
                            }
                        }

                        if (pageCount == 0) {
                            dataList.clear()
                            dataList.addAll(tempList)
                        } else {
                            dataList.addAll(tempList)
                        }

                        if (pinnedCommentModel != null) {
                            dataList.add(0, pinnedCommentModel)
                        }

                        commentsAdapter?.notifyDataSetChanged()
                    }

                    if (dataList.isEmpty()) {
                        tvNoCommentData.visibility = View.VISIBLE
                    } else {
                        tvNoCommentData.visibility = View.GONE
                    }

                } catch (e: Exception) {
                    Log.d(Constants.TAG_, "Comment Exception:\n$e")
                } finally {
                    loadMoreProgress.visibility = View.GONE
                }
            }
        }
    }


    // this function will call an api to upload your comment reply
    private fun sendCommentsReply(commentId: String, message: String, videoId: String?, videoOwnerId: String) {
        Functions.callApiForSendCommentReply(activity, "text", commentId, message, videoId, videoOwnerId, taggedUserList, object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>) {
                bind.tvComment.text = context!!.getString(R.string.leave_a_comment)
                val itemUpdate = dataList[selectedCommentPosition]
                val replyList = itemUpdate!!.arrayList

                for (itemReply in arrayList as ArrayList<CommentModel>) {
                    replyList.add(0, itemReply)
                }

                itemUpdate.arrayList = replyList
                itemUpdate.item_count_replies = "" + itemUpdate.arrayList.size
                dataList[selectedCommentPosition] = itemUpdate

                commentsAdapter?.notifyDataSetChanged()
                replyStatus = null
                selectedComment = null
                selectedReplyComment = null
            }

            override fun onSuccess(response: String) { }

            override fun onFail(response: String) { }
        })
    }


    // this function will call an api to upload your comment
    private fun sendComments(video_id: String?, comment: String?) {
        bind.apply {
            btnMic.visibility = View.GONE
            sendProgress.visibility = View.VISIBLE

            Functions.callApiForSendComment(activity, "text", video_id, comment, taggedUserList, object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>) {
                    btnMic.visibility = View.VISIBLE
                    sendProgress.visibility = View.GONE
                    tvNoCommentData.visibility = View.GONE

                    for (item in arrayList as ArrayList<CommentModel?>) {
                        dataList.add(0, item!!)
                        commentsCount++
                        commentCount.text = commentsCount.toString() + " " + getString(R.string.comments)
                        if (fragmentDataSend != null) fragmentDataSend!!.onDataSent("" + commentsCount)
                    }

                    commentsAdapter?.notifyDataSetChanged()
                    selectedComment = null
                }

                override fun onSuccess(response: String) {
                    btnMic.visibility = View.VISIBLE
                    sendProgress.visibility = View.GONE
                }

                override fun onFail(response: String) {
                    btnMic.visibility = View.VISIBLE
                    sendProgress.visibility = View.GONE
                }
            })
        }
    }


    // get the profile data by sending the username instead of id
    private fun openProfileByUsername(username: String) {
        if (Functions.checkProfileOpenValidationByUserName(username)) {
            val intent = Intent(requireContext(), ProfileA::class.java)
            intent.putExtra("user_name", username)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }


    // this will open the profile of user which have uploaded the currently running video
    private fun openProfile(commentModel: CommentModel?) {
        if (Functions.checkProfileOpenValidation(commentModel!!.userId)) {
            val intent = Intent(requireContext(), ProfileA::class.java)
            intent.putExtra("user_id", commentModel.userId)
            intent.putExtra("user_name", commentModel.user_name)
            intent.putExtra("user_pic", commentModel.profile_pic)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }


    private val permissionStorageRecordingResult = registerForActivityResult<Array<String>, Map<String, Boolean>>(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        var allPermissionClear = true
        val blockPermissionCheck: MutableList<String> = java.util.ArrayList()

        for (key in result.keys) {
            if (java.lang.Boolean.FALSE == result[key]) {
                allPermissionClear = false
                blockPermissionCheck.add(Functions.getPermissionStatus(activity, key))
            }
        }

        if (blockPermissionCheck.contains("blocked")) {
            Functions.showPermissionSetting(
                activity,
                getString(R.string.we_need_recording_permission_for_upload_sound)
            )

        } else if (allPermissionClear) {
            if (audioPermissionCheck.equals("playing", ignoreCase = true)) {
                //audioPlaying(selectedAudioView, selectedComment, selectedAudioPosition)

            } else {
                sendAudio?.finishRecording(requireActivity())
            }
        }
    }

    init {
        commentsCount = count
    }
}
