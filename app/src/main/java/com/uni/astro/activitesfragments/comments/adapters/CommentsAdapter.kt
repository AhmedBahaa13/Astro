package com.uni.astro.activitesfragments.comments.adapters

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.uni.astro.Constants
import com.uni.astro.Constants.TAG_
import com.uni.astro.R
import com.uni.astro.activitesfragments.chat.ChatA
import com.uni.astro.databinding.ItemCommentLayoutBinding
import com.uni.astro.interfaces.FragmentCallBack
import com.uni.astro.models.CommentModel
import com.uni.astro.simpleclasses.FriendsTagHelper
import com.uni.astro.simpleclasses.Functions
import java.io.File


// make the onItemClick listener interface and this interface is implement in Chat inbox activity
// for to do action when user click on item

class CommentsAdapter(
    var context: Context,
    private val dataList: ArrayList<CommentModel>,
    var listener: OnItemClickListener,
    var relyItemCLickListener: Comments_Reply_Adapter.OnRelyItemCLickListener,
    var linkClickListener: Comments_Reply_Adapter.LinkClickListener,
    var callBack: FragmentCallBack,
    private val long_listener: Comments_Reply_Adapter.OnLongClickListener

) : RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {

    private var commentsReplyAdapter: Comments_Reply_Adapter? = null


    var mediaPlayer: MediaPlayer = MediaPlayer()

    var mediaPlayerProgress = 0


    companion object {
        const val TEXT_COMMENT = 0
        const val AUDIO_COMMENT = 1
        const val ALERT_MESSAGE = 2
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CommentsViewHolder {
        val cBind = ItemCommentLayoutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return CommentsViewHolder(cBind)

        //val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_comment_layout, viewGroup, false)

        /*
        val view: View
        return when (viewtype) {
            TEXT_COMMENT -> {
                Log.d(Constants.TAG_, "onCreateViewHolder: $TEXT_COMMENT$viewtype")
                view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_comment_layout, viewGroup, false)
                TextCommentViewHolder(view)
            }

            AUDIO_COMMENT -> {
                Log.d(Constants.TAG_, "onCreateViewHolder: $AUDIO_COMMENT$viewtype")
                view = LayoutInflater.from(viewGroup.context).inflate(R.layout.audio_comment_item, viewGroup, false)
                CommentAudioViewHolder(view)
            }

            else -> {
                Log.d(Constants.TAG_, "ELSE HAPPENED: $TEXT_COMMENT$viewtype")
                view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_comment_layout, viewGroup, false)
                TextCommentViewHolder(view)
            }
        }
        */
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: CommentsViewHolder, pox: Int) {
        val position = holder.bindingAdapterPosition
        val cbind = holder.cBinder
        val item = dataList[position]
        Log.d(Constants.TAG_, "onBindViewHolder: " + item.type)


        cbind.apply {
            username.text = item.user_name
            userPic.controller = Functions.frescoImageLoad(
                item.profile_pic, userPic, false
            )

            if (item.type == "text") { // Normal Text Comment
                message.text = item.commentText
                message.visibility = View.VISIBLE
                voiceCommentPanel.visibility = View.GONE


            } else if (item.type == "audio") { // Voice Comment
                if (mediaPlayer.isPlaying) {
                    btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause_icon))
                    seekBar.progress = mediaPlayerProgress

                } else {
                    btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_icon))
                    seekBar.progress = 0
                }

                voiceTime.text = getFileDuration(Uri.parse(item.commentText))

                message.visibility = View.GONE
                voiceCommentPanel.visibility = View.VISIBLE
            }


            likeTxt.text = Functions.getSuffix(item.like_count)
            tvMessageData.text = Functions.changeDateLatterFormat("yyyy-MM-dd hh:mm:ssZZ", context, item.created + "+0000")


            if (item.liked != null && !item.equals("")) {
                if (item.liked == "1") {
                    likeImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_like_fill))
                } else {
                    likeImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_heart_gray_out))
                }
            }

            if (item.isVerified == "1") {
                ivVarified.visibility = View.VISIBLE
            } else {
                ivVarified.visibility = View.GONE
            }

            FriendsTagHelper.Creator.create(ContextCompat.getColor(context, R.color.whiteColor), ContextCompat.getColor(context, R.color.appColor)) { friendsTags ->
                var friendsTag = friendsTags
                if (friendsTag.contains("@")) {
                    Log.d(Constants.TAG_, "Friends $friendsTag")
                    if (friendsTag[0] == '@') {
                        friendsTag = friendsTag.substring(1)
                        openUserProfile(friendsTag)
                    }
                }
            }.handle(message)

            if (item.isExpand) {
                lessLayout.visibility = View.VISIBLE
            } else {
                lessLayout.visibility = View.GONE
            }

            if (item.arrayList != null && item.arrayList.size > 0) {
                replyCount.visibility = View.VISIBLE
                replyCount.text = "${context.getString(R.string.view_replies)} (${item.arrayList.size})"

            } else {
                replyCount.visibility = View.GONE
            }

            if (item.userId == item.videoOwnerId) {
                tabCreator.visibility = View.VISIBLE
            } else {
                tabCreator.visibility = View.GONE
            }

            if (item.pin_comment_id == item.comment_id) {
                tabPinned.visibility = View.VISIBLE
            } else {
                tabPinned.visibility = View.GONE
            }

            if (item.isLikedByOwner == "1") {
                tabLikedByCreator.visibility = View.VISIBLE
            } else {
                tabLikedByCreator.visibility = View.GONE
            }

            commentsReplyAdapter = Comments_Reply_Adapter(context, item.arrayList)
            replyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            replyRecyclerView.adapter = commentsReplyAdapter
            replyRecyclerView.setHasFixedSize(false)
            holder.bind(position, item, listener)



            // ============================= Click Listeners =============================
            voiceCommentPanel.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    holder.stopPlaying(position)

                } else {
                    holder.playAudio(position, item.commentText)
                }
            }

            voiceCommentPanel.setOnLongClickListener {
                holder.downloadAudio(item.commentText, item.comment_id)
                Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    inner class CommentsViewHolder(val cBinder: ItemCommentLayoutBinding) : RecyclerView.ViewHolder(cBinder.root) {
        // ============================= Voice Comment Functions =============================
        fun playAudio(position: Int, vID: String) {
            mediaPlayer = MediaPlayer.create(context, Uri.parse(vID))

            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()

                countdownTimer(position, true)

                cBinder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause_icon))
                cBinder.seekBar.progress = mediaPlayerProgress

                mediaPlayer.setOnCompletionListener { stopPlaying(position) }
                //notifyItemChanged(position)
            }
        }

        fun stopPlaying(position: Int) {
            if (mediaPlayer.isPlaying) {
                countdownTimer(position, false)

                mediaPlayer.reset()
                mediaPlayer.release()

                cBinder.btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_icon))
                cBinder.seekBar.progress = 0
                //notifyItemChanged(position)
            }
        }

        fun countdownTimer(position: Int, startTimer: Boolean) {
            if (startTimer && mediaPlayer.isPlaying) {
                val countDownTimer: CountDownTimer = object : CountDownTimer(mediaPlayer.duration.toLong(), 300) {
                    override fun onTick(millisUntilFinished: Long) {
                        mediaPlayerProgress = mediaPlayer.currentPosition * 100 / mediaPlayer.duration

                        if (mediaPlayerProgress > 98) {
                            countdownTimer(position, false)
                            mediaPlayerProgress = 0
                        }

                        //notifyItemChanged(position)
                    }

                    override fun onFinish() {
                        mediaPlayerProgress = 0
                        countdownTimer(position, false)
                        //notifyItemChanged(position)
                    }
                }

                countDownTimer.start()
            }
        }

        fun downloadAudio(vUrl: String, vID: String) {
            Log.d(Constants.TAG_, "downloadAudio: " + Functions.getAppFolder(context))

            PRDownloader.download(vUrl, Functions.getAppFolder(context), "$vID.mp3")
                .build().start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: Error) {
                        Log.d(Constants.TAG_, "onError: " + error.serverErrorMessage)
                        Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show()
                    }
                })
        }



        fun bind(position: Int, item: CommentModel, listener: OnItemClickListener) {
            cBinder.apply {
                itemView.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                userPic.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                tabUserPic.setOnClickListener { view: View -> listener.onItemClick(position, item, view) }
                username.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }

                tabMessageReply.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                likeLayout.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                replyCount.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                showLessTxt.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }

                commentPanel.setOnLongClickListener { view ->
                    listener.onItemLongPress(position, item, view)
                    false
                }

                seekBar.setOnTouchListener { v: View?, event: MotionEvent -> true }
            }
        }
    }


    interface OnItemClickListener {
        fun onItemClick(positon: Int, item: CommentModel, view: View)
        fun onItemLongPress(positon: Int, item: CommentModel, view: View)
    }

    private fun getFileDuration(uri: Uri): String {
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val file_duration = Functions.parseInterger(durationStr)
            val second = (file_duration / 1000 % 60).toLong()
            val minute = (file_duration / (1000 * 60) % 60).toLong()
            return String.format("%02d:%02d", minute, second)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG_, "getFileDurationERROR: " + e.message)
        }

        return "00:00"
    }

    private fun openUserProfile(friendsTag: String) {
        val bundle = Bundle()
        bundle.putBoolean("isShow", true)
        bundle.putString("name", friendsTag)
        callBack.onResponce(bundle)
    }


    /*
    internal inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var username: TextView
        var message: TextView
        var replyCount: TextView
        var likeTxt: TextView
        var showLessTxt: TextView
        var tvMessageData: TextView
        var userPic: SimpleDraweeView
        var tabUserPic: FrameLayout
        var likeImage: ImageView
        var ivVarified: ImageView
        var messageLayout: LinearLayout
        var lessLayout: LinearLayout
        var likeLayout: LinearLayout
        var tabCreator: LinearLayout
        var tabMessageReply: LinearLayout
        var tabPinned: LinearLayout
        var tabLikedByCreator: LinearLayout
        var replyRecyclerView: RecyclerView

        init {
            ivVarified = view.findViewById(R.id.ivVarified)
            tabLikedByCreator = view.findViewById(R.id.tabLikedByCreator)
            tvMessageData = view.findViewById(R.id.tvMessageData)
            tabMessageReply = view.findViewById(R.id.tabMessageReply)
            tabPinned = view.findViewById(R.id.tabPinned)
            tabUserPic = view.findViewById(R.id.tabUserPic)
            username = view.findViewById(R.id.username)
            userPic = view.findViewById(R.id.user_pic)
            message = view.findViewById(R.id.message)
            replyCount = view.findViewById(R.id.reply_count)
            likeImage = view.findViewById(R.id.like_image)
            messageLayout = view.findViewById(R.id.message_layout)
            likeTxt = view.findViewById(R.id.like_txt)
            tabCreator = view.findViewById(R.id.tabCreator)
            replyRecyclerView = view.findViewById(R.id.reply_recycler_view)
            lessLayout = view.findViewById(R.id.less_layout)
            showLessTxt = view.findViewById(R.id.show_less_txt)
            likeLayout = view.findViewById(R.id.like_layout)
        }

        fun bind(position: Int, item: CommentModel, listener: OnItemClickListener) {
            itemView.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
            tabUserPic.setOnClickListener { view: View ->
                listener.onItemClick(position, item, view)
            }

            userPic.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
            username.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
            messageLayout.setOnLongClickListener { view ->
                listener.onItemLongPress(position, item, view)
                false
            }

            likeLayout.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
            tabMessageReply.setOnClickListener { v: View ->
                listener.onItemClick(position, item, v)
            }

            replyCount.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
            showLessTxt.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
        }
    }
    */
}