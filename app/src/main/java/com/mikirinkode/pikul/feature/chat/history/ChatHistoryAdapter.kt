package com.mikirinkode.pikul.feature.chat.history

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.chat.ChatMessage
import com.mikirinkode.pikul.data.model.chat.Conversation
import com.mikirinkode.pikul.databinding.ItemChatHistoryBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity
import com.mikirinkode.pikul.utils.DateHelper


class ChatHistoryAdapter : RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder>() {

    private val conversations: ArrayList<Conversation> = ArrayList()
    private var loggedUserId: String = ""

    inner class ViewHolder(private val binding: ItemChatHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            binding.apply {
                val latestMessage: ChatMessage? = conversation.lastMessage
                setTimestamp(tvTimestamp, latestMessage?.sendTimestamp ?: 0)

                if (latestMessage?.senderId == loggedUserId) {
                    // the logged user is the sender
                    tvMessage.text = "${latestMessage.message}"
                    updateMessageStatus(
                        latestMessage,
                        conversation.participants.size,
                        tvMessageStatus,
                        itemView.resources
                    )
                    tvUnreadMessages.visibility = View.GONE
                } else {
                    tvMessage.text = latestMessage?.message
                    // the interlocutor is the sender and the logged user is the receiver
                    tvMessageStatus.visibility = View.GONE

                    val numberUnreadMessage =
                        conversation.unreadMessageEachParticipant[loggedUserId] ?: 0
                    if (numberUnreadMessage > 0) {
                        tvUnreadMessages.visibility = View.VISIBLE
                        tvUnreadMessages.text = numberUnreadMessage.toString()
                    } else {
                        tvUnreadMessages.visibility = View.GONE
                    }
                }

                tvUserName.text = conversation.interlocutor?.name

                if (conversation.interlocutor?.avatarUrl != null && conversation.interlocutor?.avatarUrl != "") {
                    Glide.with(itemView.context)
                        .load(conversation.interlocutor?.avatarUrl)
                        .into(binding.ivUserAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_default_user_avatar).into(binding.ivUserAvatar)
                }

            }
            itemView.setOnClickListener {
                itemView.context.startActivity(
                    Intent(
                        itemView.context,
                        ChatRoomActivity::class.java
                    ).putExtra(
                        ChatRoomActivity.EXTRA_INTENT_CONVERSATION_ID,
                        conversation.conversationId
                    ).putExtra(
                        ChatRoomActivity.EXTRA_INTENT_INTERLOCUTOR_ID,
                        conversation.interlocutor?.userId
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemChatHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    private fun updateMessageStatus(
        chat: ChatMessage,
        totalParticipants: Int,
        tvloggedUserMessageStatus: TextView,
        resources: Resources
    ) {
        // check if message is been read by all user or not yet
        val isBeenRead = chat.beenReadBy.size == totalParticipants - 1
        if (isBeenRead) {
            tvloggedUserMessageStatus.visibility = View.VISIBLE
            tvloggedUserMessageStatus.text = "✓✓"
            tvloggedUserMessageStatus.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.message_been_read_color,
                    null
                )
            )
        } else {
            // if chat hasn't been read
            // TODO delivered timestamp
            val isDelivered = chat.beenDeliveredTo.isNotEmpty()
            if (isDelivered) {
                tvloggedUserMessageStatus.visibility = View.VISIBLE
                tvloggedUserMessageStatus.text = "✓✓"
            } else if (chat.sendTimestamp != 0L) {
                tvloggedUserMessageStatus.visibility = View.VISIBLE
                tvloggedUserMessageStatus.text = "✓"
            }

            // update the status color
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    // The system is currently in night mode
                    tvloggedUserMessageStatus.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.night_theme_text_color,
                            null
                        )
                    )
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    // The system is currently in day mode
                    tvloggedUserMessageStatus.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.light_theme_text_color,
                            null
                        )
                    )
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    // We don't know what mode we're in, assume day mode
                    tvloggedUserMessageStatus.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.light_theme_text_color,
                            null
                        )
                    )
                }
            }
        }
    }

    private fun setTimestamp(tvTimestamp: TextView, timestamp: Long) {
        val todayDate = DateHelper.getCurrentDate()
        val startDate = DateHelper.getThisWeekStartDate()
        val endDate = DateHelper.getThisWeekEndDate()
        val timestampDate = DateHelper.getDateFromTimestamp(timestamp ?: 0)

        if (todayDate.equals(timestampDate, ignoreCase = true)) { // today
            tvTimestamp.text =
                DateHelper.getTimeFromTimestamp(timestamp ?: 0)
        } else if (DateHelper.isYesterdayDate(timestamp ?: 0)) {
            tvTimestamp.text = "Kemarin"
        } else if (timestampDate in startDate..endDate) { // still this week
            tvTimestamp.text =
                DateHelper.getDayNameFromTimestamp(timestamp ?: 0)
        } else {
            tvTimestamp.text =
                DateHelper.getFormattedDateFromTimestamp(timestamp ?: 0)
        }
    }

    fun setLoggedUserId(userId: String) {
        loggedUserId = userId
    }


    fun setData(newList: List<Conversation>) {
        conversations.clear()
        conversations.addAll(newList)
        notifyDataSetChanged()
    }
}