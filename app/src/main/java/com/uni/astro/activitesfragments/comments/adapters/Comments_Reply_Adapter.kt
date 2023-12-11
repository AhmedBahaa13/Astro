package com.uni.astro.activitesfragments.comments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.hendraanggrian.appcompat.widget.SocialTextView
import com.hendraanggrian.appcompat.widget.SocialView
import com.uni.astro.R
import com.uni.astro.models.CommentModel
import com.uni.astro.simpleclasses.Functions


class Comments_Reply_Adapter(var context: Context, private val dataList: ArrayList<CommentModel>) :
    RecyclerView.Adapter<CustomViewHolder>() {

    private val linkClickListener: LinkClickListener? = null

    interface OnRelyItemCLickListener {
        fun onItemClick(arrayList: ArrayList<CommentModel>, postion: Int, view: View)
        fun onItemLongPress(arrayList: ArrayList<CommentModel>, postion: Int, view: View)
    }

    interface LinkClickListener {
        fun onLinkClicked(view: SocialView?, matchedText: String?)
    }

    interface OnLongClickListener {
        fun onLongclick(item: CommentModel?, view: View?)
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_comment_reply_layout, viewGroup, false)

        return CustomViewHolder(view, object : OnRelyItemCLickListener {
            override fun onItemClick(arrayList: java.util.ArrayList<CommentModel>, postion: Int, view: View) { }

            override fun onItemLongPress(arrayList: java.util.ArrayList<CommentModel>, postion: Int, view: View) { }
        })
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]
        holder.username.text = item.replay_user_name
        holder.user_pic.controller = Functions.frescoImageLoad(item.replay_user_url, holder.user_pic, false)
        holder.message.text = item.comment_reply
        val date = Functions.changeDateLatterFormat(
            "yyyy-MM-dd hh:mm:ssZZ",
            context,
            item.created + "+0000"
        )

        holder.tvMessageData.text = date
        if (item.comment_reply_liked != null && item.comment_reply_liked != "") {
            if (item.comment_reply_liked == "1") {
                holder.reply_like_image.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_like_fill
                    )
                )
            } else {
                holder.reply_like_image.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_heart_gray_out
                    )
                )
            }
        }

        if (item.userId == item.videoOwnerId) {
            holder.tabCreator.visibility = View.VISIBLE
        } else {
            holder.tabCreator.visibility = View.GONE
        }

        if (item.isLikedByOwner == "1") {
            holder.tabLikedByCreator.visibility = View.VISIBLE
        } else {
            holder.tabLikedByCreator.visibility = View.GONE
        }

        if (item.isVerified == "1") {
            holder.ivVarified.visibility = View.VISIBLE
        } else {
            holder.ivVarified.visibility = View.GONE
        }

        holder.like_txt.text = Functions.getSuffix(item.reply_liked_count)
        holder.message.setOnMentionClickListener { view, text ->
            linkClickListener?.onLinkClicked(view, text.toString())
        }

        holder.bind(i, dataList, holder.replyListener)
    }

    inner class CustocfmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var username: TextView
        var like_txt: TextView
        var tvMessageData: TextView
        var message: SocialTextView
        var user_pic: SimpleDraweeView
        var reply_like_image: ImageView
        var ivVarified: ImageView
        var reply_layout: LinearLayout
        var like_layout: LinearLayout
        var tabLikedByCreator: LinearLayout
        var tabMessageReply: LinearLayout
        var tabCreator: LinearLayout

        init {
            ivVarified = view.findViewById(R.id.ivVarified)
            tvMessageData = view.findViewById(R.id.tvMessageData)
            tabMessageReply = view.findViewById(R.id.tabMessageReply)
            tabLikedByCreator = view.findViewById(R.id.tabLikedByCreator)
            username = view.findViewById(R.id.username)
            user_pic = view.findViewById(R.id.user_pic)
            tabCreator = view.findViewById(R.id.tabCreator)
            message = view.findViewById(R.id.message)
            reply_layout = view.findViewById(R.id.reply_layout)
            reply_like_image = view.findViewById(R.id.reply_like_image)
            like_layout = view.findViewById(R.id.like_layout)
            like_txt = view.findViewById(R.id.like_txt)
        }

        fun bind(postion: Int, datalist: ArrayList<CommentModel>, listener: OnRelyItemCLickListener) {
            itemView.setOnClickListener { v: View ->
                listener.onItemClick(datalist, postion, v)
            }

            user_pic.setOnClickListener { v: View ->
                listener.onItemClick(datalist, postion, v)
            }

            username.setOnClickListener { v: View ->
                listener.onItemClick(datalist, postion, v)
            }

            tabMessageReply.setOnClickListener { v: View ->
                listener.onItemClick(datalist, postion, v)
            }

            like_layout.setOnClickListener { v: View ->
                listener.onItemClick(datalist, postion, v)
            }

            reply_layout.setOnLongClickListener { view ->
                listener.onItemLongPress(datalist, postion, view)
                false
            }
        }
    }
}