package com.uni.astro.activitesfragments.comments.adapters

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.uni.astro.Constants
import com.uni.astro.R
import com.uni.astro.activitesfragments.comments.CommentF.Companion.mediaPlayer
import com.uni.astro.activitesfragments.comments.CommentF.Companion.mediaPlayerProgress
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
        Log.d(Constants.TAG_, "onBindViewHolder: " + item.comment_type)

        val date = Functions.changeDateLatterFormat("yyyy-MM-dd hh:mm:ssZZ", context, item.created + "+0000")


        cbind.username.text = item.user_name
        cbind.userPic.controller = Functions.frescoImageLoad(
            item.profile_pic,
            cbind.userPic,
            false
        )

        if (item.liked != null && !item.equals("")) {
            if (item.liked == "1") {
                cbind.likeImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, R.drawable.ic_like_fill
                    )
                )
            } else {
                cbind.likeImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, R.drawable.ic_heart_gray_out
                    )
                )
            }
        }

        if (item.isVerified == "1") {
            cbind.ivVarified.visibility = View.VISIBLE
        } else {
            cbind.ivVarified.visibility = View.GONE
        }

        cbind.likeTxt.text = Functions.getSuffix(item.like_count)
        cbind.tvMessageData.text = date
        cbind.message.text = item.comments

        FriendsTagHelper.Creator.create(ContextCompat.getColor(context, R.color.whiteColor), ContextCompat.getColor(context, R.color.appColor)) { friendsTags ->
            var friendsTag = friendsTags
            if (friendsTag.contains("@")) {
                Log.d(Constants.TAG_, "Friends $friendsTag")
                if (friendsTag[0] == '@') {
                    friendsTag = friendsTag.substring(1)
                    openUserProfile(friendsTag)
                }
            }
        }.handle(cbind.message)

        if (item.isExpand) {
            cbind.lessLayout.visibility = View.VISIBLE
        } else {
            cbind.lessLayout.visibility = View.GONE
        }

        if (item.arrayList != null && item.arrayList.size > 0) {
            cbind.replyCount.visibility = View.VISIBLE
            cbind.replyCount.text = "${context.getString(R.string.view_replies)} (${item.arrayList.size})"

        } else {
            cbind.replyCount.visibility = View.GONE
        }

        if (item.userId == item.videoOwnerId) {
            cbind.tabCreator.visibility = View.VISIBLE
        } else {
            cbind.tabCreator.visibility = View.GONE
        }

        if (item.pin_comment_id == item.comment_id) {
            cbind.tabPinned.visibility = View.VISIBLE
        } else {
            cbind.tabPinned.visibility = View.GONE
        }

        if (item.isLikedByOwner == "1") {
            cbind.tabLikedByCreator.visibility = View.VISIBLE
        } else {
            cbind.tabLikedByCreator.visibility = View.GONE
        }

        commentsReplyAdapter = Comments_Reply_Adapter(context, item.arrayList)
        cbind.replyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        cbind.replyRecyclerView.adapter = commentsReplyAdapter
        cbind.replyRecyclerView.setHasFixedSize(false)
        holder.bind(position, item, listener)


        /*
        when (item.comment_type) {
            "text" -> {
                val textCommentViewHolder = holder as TextCommentViewHolder
                textCommentViewHolder.username.text = item.user_name
                textCommentViewHolder.userPic.controller = Functions.frescoImageLoad(
                    item.profile_pic,
                    textCommentViewHolder.userPic,
                    false
                )

                if (item.liked != null && !item.equals("")) {
                    if (item.liked == "1") {
                        textCommentViewHolder.likeImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                context, R.drawable.ic_like_fill
                            )
                        )
                    } else {
                        textCommentViewHolder.likeImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                context, R.drawable.ic_heart_gray_out
                            )
                        )
                    }
                }

                if (item.isVerified == "1") {
                    textCommentViewHolder.isVerified.visibility = View.VISIBLE
                } else {
                    textCommentViewHolder.isVerified.visibility = View.GONE
                }

                textCommentViewHolder.likeTxt.text = Functions.getSuffix(item.like_count)
                textCommentViewHolder.tvMessageData.text = date
                textCommentViewHolder.message.text = item.comments

                FriendsTagHelper.Creator.create(
                    ContextCompat.getColor(
                        textCommentViewHolder.itemView.context,
                        R.color.whiteColor
                    ),
                    ContextCompat.getColor(textCommentViewHolder.itemView.context, R.color.appColor)
                ) { friendsTag ->
                    var friendsTag = friendsTag
                    if (friendsTag.contains("@")) {
                        Log.d(Constants.TAG_, "Friends $friendsTag")
                        if (friendsTag[0] == '@') {
                            friendsTag = friendsTag.substring(1)
                            openUserProfile(friendsTag)
                        }
                    }
                }.handle(textCommentViewHolder.message)

                if (item.isExpand) {
                    textCommentViewHolder.lessLayout.visibility = View.VISIBLE
                } else {
                    textCommentViewHolder.lessLayout.visibility = View.GONE
                }

                if (item.arrayList != null && item.arrayList.size > 0) {
                    textCommentViewHolder.replyCount.visibility = View.VISIBLE
                    textCommentViewHolder.replyCount.text =
                        context.getString(R.string.view_replies) + " (" + item.arrayList.size + ")"
                } else {
                    textCommentViewHolder.replyCount.visibility = View.GONE
                }

                if (item.userId == item.videoOwnerId) {
                    textCommentViewHolder.tabCreator.visibility = View.VISIBLE
                } else {
                    textCommentViewHolder.tabCreator.visibility = View.GONE
                }

                if (item.pin_comment_id == item.comment_id) {
                    textCommentViewHolder.tabPinned.visibility = View.VISIBLE
                } else {
                    textCommentViewHolder.tabPinned.visibility = View.GONE
                }

                if (item.isLikedByOwner == "1") {
                    textCommentViewHolder.tabLikedByCreator.visibility = View.VISIBLE
                } else {
                    textCommentViewHolder.tabLikedByCreator.visibility = View.GONE
                }

                commentsReplyAdapter = Comments_Reply_Adapter(context, item.arrayList)
                textCommentViewHolder.replyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                textCommentViewHolder.replyRecyclerView.adapter = commentsReplyAdapter
                textCommentViewHolder.replyRecyclerView.setHasFixedSize(false)
                textCommentViewHolder.bind(position, item, listener)
            }


            "audio" -> {
                // TextCommentViewHolder textCommentViewHolder = (TextCommentViewHolder) holder;
                val audioCommentViewHolder = holder as CommentAudioViewHolder
                Log.d(Constants.TAG_, "onBindViewHolder: " + audioCommentViewHolder.username)
                audioCommentViewHolder.username.text = item.user_name
                Log.d(Constants.TAG_, "onBindViewHolder: " + item.comments)
                audioCommentViewHolder.userPic.controller = Functions.frescoImageLoad(
                    item.profile_pic,
                    audioCommentViewHolder.userPic,
                    false
                )

                if (item.liked != null && !item.equals("")) {
                    if (item.liked == "1") {
                        audioCommentViewHolder.likeImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                context, R.drawable.ic_like_fill
                            )
                        )
                    } else {
                        audioCommentViewHolder.likeImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                context, R.drawable.ic_heart_gray_out
                            )
                        )
                    }
                }

                if (item.isVerified == "1") {
                    audioCommentViewHolder.isVerified.visibility = View.VISIBLE
                } else {
                    audioCommentViewHolder.isVerified.visibility = View.GONE
                }

                audioCommentViewHolder.likeTxt.text = Functions.getSuffix(item.like_count)
                audioCommentViewHolder.tvMessageData.text = date

                if (item.isPlaying && mediaPlayer != null) {
                    audioCommentViewHolder.playBtn.setImageDrawable(
                        ContextCompat.getDrawable(
                            context, R.drawable.ic_pause_icon
                        )
                    )
                    audioCommentViewHolder.seekBar.progress = mediaPlayerProgress

                } else {
                    audioCommentViewHolder.playBtn.setImageDrawable(
                        ContextCompat.getDrawable(
                            context, R.drawable.ic_play_icon
                        )
                    )
                    audioCommentViewHolder.seekBar.progress = 0
                }

                val fullpath = File(
                    Functions.getAppFolder(
                        context
                    ) + item.comment_id + ".mp3"
                )

                if (fullpath.exists()) {
                    audioCommentViewHolder.totalTime.text =
                        getFileDuration(Uri.parse(fullpath.absolutePath))
                } else {
                    audioCommentViewHolder.totalTime.text = null
                }
                audioCommentViewHolder.bind(dataList[position], position, listener, long_listener)
            }

            else -> {}
        }
        */
    }

    inner class CommentsViewHolder(val cBinder: ItemCommentLayoutBinding) : RecyclerView.ViewHolder(cBinder.root) {
        fun bind(position: Int, item: CommentModel, listener: OnItemClickListener) {
            cBinder.apply {
                itemView.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                tabUserPic.setOnClickListener { view: View ->
                    listener.onItemClick(position, item, view)
                }

                userPic.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                username.setOnClickListener { v: View -> listener.onItemClick(position, item, v) }
                commentPanel.setOnLongClickListener { view ->
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
    }


    private fun getFileDuration(uri: Uri): String? {
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val file_duration = Functions.parseInterger(durationStr)
            val second = (file_duration / 1000 % 60).toLong()
            val minute = (file_duration / (1000 * 60) % 60).toLong()
            return String.format("%02d:%02d", minute, second)
        } catch (e: Exception) {
        }
        return null
    }

    override fun getItemViewType(position: Int): Int {
        Log.d(Constants.TAG_, "getItemViewTypePosition: $position")
        return getCommentType(position)
    }

    private fun getCommentType(position: Int): Int {
        Log.d(Constants.TAG_, "getCommentTypeXXXXXXXXX: ${dataList[position].comment_type}")
        return when (dataList[position].comment_type) {
            "audio" -> {
                Log.d(
                    Constants.TAG_,
                    "getCommentType111111111: " + dataList[position].comment_type
                )
                AUDIO_COMMENT
            }

            "text" -> {
                Log.d(
                    Constants.TAG_,
                    "getCommentType222222222: " + dataList[position].comment_type
                )
                TEXT_COMMENT
            }

            else -> {
                ALERT_MESSAGE
            }
        }
    }

    private fun openUserProfile(friendsTag: String) {
        val bundle = Bundle()
        bundle.putBoolean("isShow", true)
        bundle.putString("name", friendsTag)
        callBack.onResponce(bundle)
    }

    interface OnItemClickListener {
        fun onItemClick(positon: Int, item: CommentModel, view: View)
        fun onItemLongPress(positon: Int, item: CommentModel, view: View)
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