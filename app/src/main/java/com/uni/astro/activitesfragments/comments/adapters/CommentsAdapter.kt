package com.uni.astro.activitesfragments.comments.adapters

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.uni.astro.Constants
import com.uni.astro.R
import com.uni.astro.databinding.ItemCommentLayoutBinding
import com.uni.astro.interfaces.FragmentCallBack
import com.uni.astro.models.CommentModel
import com.uni.astro.simpleclasses.FriendsTagHelper
import com.uni.astro.simpleclasses.Functions


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

    private val activity = context as Activity

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var currentPlayingPosition: Int = -1

    init {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener { }
    }

    var mediaPlayerProgress = 0


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CommentsViewHolder {
        val cBind = ItemCommentLayoutBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return CommentsViewHolder(cBind)
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, pox: Int) {
        val position = holder.bindingAdapterPosition
        val cbind = holder.cBinder
        val item = dataList[position]


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
//                if (mediaPlayer.isPlaying) {
//                    btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause_icon))
//                    seekBar.progress = mediaPlayerProgress
//
//                } else {
//                    btnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_icon))
//                    seekBar.progress = 0
//                }

                voiceTime.text = item.duration

                message.visibility = View.GONE
                voiceCommentPanel.visibility = View.VISIBLE
            }


            likeTxt.text = Functions.getSuffix(item.like_count)
            tvMessageData.text = Functions.changeDateLatterFormat(
                "yyyy-MM-dd hh:mm:ssZZ",
                context,
                item.created + "+0000"
            )


            if (item.liked != null && !item.equals("")) {
                if (item.liked == "1") {
                    likeImage.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.love_comment_clicked
                        )
                    )
                    likeTxt.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.love_comment_clicked
                        )
                    )
                } else {
                    likeImage.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.love_comment_default
                        )
                    )

                    likeTxt.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.love_comment_default
                        )
                    )
                }
            }

            if (item.isVerified == "1") {
                ivVarified.visibility = View.VISIBLE
            } else {
                ivVarified.visibility = View.GONE
            }

            FriendsTagHelper.Creator.create(
                ContextCompat.getColor(context, R.color.whiteColor),
                ContextCompat.getColor(context, R.color.appColor)
            ) { friendsTags ->
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
                replyCount.text =
                    "${context.getString(R.string.view_replies)} (${item.arrayList.size})"

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
            replyRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            replyRecyclerView.adapter = commentsReplyAdapter
            replyRecyclerView.setHasFixedSize(false)
            holder.bind(position, item, listener)


            // ============================= Click Listeners =============================
            btnPlay.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    holder.stopPlaying()

                } else {
                    holder.playAudio(item.commentText, position, item)
                }
            }

            voiceCommentPanel.setOnLongClickListener {
                holder.downloadAudio(item.commentText, item.comment_id)
                Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    inner class CommentsViewHolder(val cBinder: ItemCommentLayoutBinding) :
        RecyclerView.ViewHolder(cBinder.root) {
        // ============================= Voice Comment Functions =============================
        private var cTimer: CountDownTimer = object : CountDownTimer(0, 0) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {}
        }

        fun playAudio(vID: String, position: Int, item: CommentModel) {
            //mediaPlayer = MediaPlayer.create(context, Uri.parse(vID))
            mediaPlayer = MediaPlayer().apply {
                Log.d("CommentsViewHolder", "playAudio: $vID")
                setDataSource(vID)
                prepare()
            }


            cBinder.apply {
                seekBar.max = getRecordDuration(dataList[position].duration)

                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    btnPlay.setImageResource(R.drawable.pause_audio_ic)

                    cTimer = object : CountDownTimer(mediaPlayer.duration.toLong(), 100) {
                        override fun onTick(millisUntilFinished: Long) {
                            seekBar.progress = mediaPlayer.currentPosition
                        }

                        override fun onFinish() {
                            btnPlay.setImageResource(R.drawable.play_audio_icon)
                            seekBar.progress = 0
                        }
                    }
                    cTimer.start()

                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            cBinder.voiceTime.text = String.format(
                                "%02d:%02d",
                                mediaPlayer.currentPosition / (60 * 1000),
                                (mediaPlayer.currentPosition / 1000) % 60
                            )

                            if (fromUser) {
                                mediaPlayer.seekTo(progress)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {
                            btnPlay.setImageResource(R.drawable.play_audio_icon)
                            mediaPlayer.pause()
                            cTimer.cancel()
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar) {
                            btnPlay.setImageResource(R.drawable.pause_audio_ic)
                            mediaPlayer.seekTo(seekBar.progress)
                            mediaPlayer.start()
                            cTimer.start()
                        }
                    })

                    mediaPlayer.setOnCompletionListener { stopPlaying() }
                }
            }
        }

        fun stopPlaying() {
            if (mediaPlayer.isPlaying) {
                cBinder.btnPlay.setImageResource(R.drawable.play_audio_icon)
                mediaPlayer.pause()
                cTimer.cancel()
            }
        }

        fun downloadAudio(vUrl: String, vID: String) {
            PRDownloader.download(vUrl, Functions.getAppFolder(context), "$vID.mp3").build()
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: Error) {
                        Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        fun getRecordDuration(durationString: String): Int {
            Log.d("CommentsAdapter", "getRecordDuration: String :: $durationString")
            val parts = durationString.split(":")
            val minutes = parts[0].toIntOrNull() ?: 0
            val seconds = parts[1].toIntOrNull() ?: 0
            Log.d(
                "CommentsAdapter",
                "getRecordDuration: Duration :: ${minutes * 60 * 1000 + seconds * 1000}"
            )

            return minutes * 60 * 1000 + seconds * 1000
        }


        fun bind(position: Int, item: CommentModel, listener: OnItemClickListener) {
            cBinder.apply {
                itemView.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                userPic.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                tabUserPic.setOnClickListener { view: View ->
                    listener.onItemClick(
                        position,
                        item,
                        view
                    )
                }
                username.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }

                tabMessageReply.setOnClickListener { v: View ->
                    listener.onItemClick(
                        position,
                        item,
                        v
                    )
                }
                likeLayout.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                replyCount.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                showLessTxt.setOnClickListener { v: View ->
                    listener.onItemClick(
                        position,
                        item,
                        v
                    )
                }

                commentPanel.setOnLongClickListener { view ->
                    listener.onItemLongPress(position, item, view)
                    false
                }

//                seekBar.setOnTouchListener { v: View?, event: MotionEvent -> true }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size


    interface OnItemClickListener {
        fun onItemClick(positon: Int, item: CommentModel, view: View)
        fun onItemLongPress(positon: Int, item: CommentModel, view: View)
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