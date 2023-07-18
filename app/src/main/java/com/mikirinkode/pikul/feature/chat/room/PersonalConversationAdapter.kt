package com.mikirinkode.pikul.feature.chat.room

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.data.model.chat.ChatMessage
import com.mikirinkode.pikul.data.model.chat.Conversation
import com.mikirinkode.pikul.databinding.ItemMessageBinding
import com.mikirinkode.pikul.utils.DateHelper
import java.util.*

class PersonalConversationAdapter : RecyclerView.Adapter<PersonalConversationAdapter.ViewHolder>() {

    private var conversation: Conversation? = null // TODO: NEED TO BE SET, i think we can change to total unread message
    private var interlocutorData: UserAccount? = null // TODO: NEED TO BE SET
    private var loggedUserId: String = ""
    private val messages: ArrayList<ChatMessage> = ArrayList()

    private val listIndexOfSelectedMessages = ArrayList<Int>()
    private var currentSelectedMessage: ChatMessage? = null

    var chatClickListener: ChatClickListener? = null

    inner class ViewHolder(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage, position: Int) {
            binding.apply {

                // handle message type
                when (chat.type.uppercase()) {
                    MessageType.TEXT.toString() -> {
                        ivInterlocutorExtraImage.visibility = View.GONE
                        ivloggedUserExtraImage.visibility = View.GONE
                    }
                    MessageType.IMAGE.toString() -> {
                        if (chat.imageUrl != "") {
                            if (chat.senderId == loggedUserId) {
                                ivloggedUserExtraImage.visibility = View.VISIBLE
                                Glide.with(itemView.context)
                                    .load(chat.imageUrl)
                                    .placeholder(R.drawable.progress_animation)
                                    .into(ivloggedUserExtraImage)

                            } else {
                                ivInterlocutorExtraImage.visibility = View.VISIBLE
                                Glide.with(itemView.context)
                                    .load(chat.imageUrl)
                                    .into(ivInterlocutorExtraImage)
                            }
                        }
                    }
                    MessageType.VIDEO.toString() -> {}
                    MessageType.AUDIO.toString() -> {}
                }

                // show message
                if (chat.senderId == loggedUserId) {
                    layoutLoggedUserMessage.visibility = View.VISIBLE
                    layoutInterlocutorMessage.visibility = View.GONE

                    tvloggedUserMessage.text = chat.message
                    tvloggedUserTimestamp.text =
                        DateHelper.getTimeFromTimestamp(chat.sendTimestamp)

                    // if the logged user is the sender
                    // then show the message read status
                    updateMessageStatus(chat, tvloggedUserMessageStatus, itemView.resources)
                } else {
                    layoutLoggedUserMessage.visibility = View.GONE
                    layoutInterlocutorMessage.visibility = View.VISIBLE

                    tvInterlocutorMessage.text = chat.message
                    tvInterlocutorTimestamp.text =
                        DateHelper.getTimeFromTimestamp(chat.sendTimestamp)
                }

                if (conversation != null) {
                    val totalUnreadMessage =
                        conversation!!.unreadMessageEachParticipant[loggedUserId]

                    if (totalUnreadMessage != null) {

                        if (position == messages.size - totalUnreadMessage) {
                            cardUnreadHeader.visibility = View.VISIBLE
                            tvUnreadHeader.text = "$totalUnreadMessage Unread Messages"
                        } else {
                            cardUnreadHeader.visibility = View.GONE
                        }
                    }
                } else {
                    cardUnreadHeader.visibility = View.GONE
                }


                // handle date header
                setupDateHeader(binding, position, itemView.context)

                // Handle on click events
                onCLickAction(binding, chat, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position], position)
    }

    private fun setupDateHeader(
        binding: ItemMessageBinding,
        position: Int,
        context: Context
    ) {
        binding.apply {
            val message = messages[position]

            var headerTimestamp: Long? = null

            if (position == 0) {
                headerTimestamp = message.sendTimestamp
            }

            if (position > 1 && position + 1 < messages.size - 1) {
                val prevMessage = messages[position - 1]

                val calendar = GregorianCalendar.getInstance()

                calendar.time = DateHelper.formatTimestampToDate(message.sendTimestamp)
                val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

                calendar.time = DateHelper.formatTimestampToDate(prevMessage.sendTimestamp)
                val prevDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

                // if day in this message and day in previous message is different
                // then show the header
                if (prevDayOfYear != dayOfYear) {
                    headerTimestamp = message.sendTimestamp
                }
            }
            if (headerTimestamp != null) {
                cardDateHeader.visibility = View.VISIBLE

                val todayDate = DateHelper.getCurrentDate()
                val startDate = DateHelper.getThisWeekStartDate()
                val endDate = DateHelper.getThisWeekEndDate()
                val timestampDate = DateHelper.getDateFromTimestamp(headerTimestamp)

                if (todayDate.equals(timestampDate, ignoreCase = true)) { // today
                    tvDateHeader.text =
                        context.getString(R.string.txt_today)
                } else if (DateHelper.isYesterdayDate(headerTimestamp)) { // yesterday
                    tvDateHeader.text =
                        context.getString(R.string.txt_yesterday)
                } else if (timestampDate in startDate..endDate) { // still in this week
                    tvDateHeader.text =
                        DateHelper.getDayNameFromTimestamp(headerTimestamp)
                } else {
                    tvDateHeader.text = DateHelper.regularFormat(headerTimestamp)
                }
            } else {
                cardDateHeader.visibility = View.GONE
            }
        }
    }

    private fun updateMessageStatus(
        chat: ChatMessage,
        tvloggedUserMessageStatus: TextView,
        resources: Resources
    ) {
        // check if message is been read by all user or not yet
        val isBeenRead = chat.beenReadBy.size == 1
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

    private fun onCLickAction(
        binding: ItemMessageBinding,
        chat: ChatMessage,
        position: Int
    ) {
        binding.apply {
            // update selected status
            if (chat.isSelected) {
                if (chat.senderId == loggedUserId) {
                    layoutLoggedUserOnSelected.visibility = View.VISIBLE
                } else {
                    layoutIntercolucatorOnSelected.visibility = View.VISIBLE
                }
            } else {
                if (chat.senderId == loggedUserId) {
                    layoutLoggedUserOnSelected.visibility = View.GONE
                } else {
                    layoutIntercolucatorOnSelected.visibility = View.GONE
                }
            }

            /**
             * Interlocutor On click Listener
             */
            ivInterlocutorExtraImage.setOnClickListener {
                chatClickListener?.onImageClick(chat)
            }
            layoutInterlocutorMessage.setOnClickListener {
                // is chat selected is more than one
                if (listIndexOfSelectedMessages.size >= 1){
                    if (!chat.isSelected) {
                        chat.isSelected = true
                        currentSelectedMessage = chat
                        notifyItemChanged(position)
                        listIndexOfSelectedMessages.add(position)
                        chatClickListener?.onMessageSelected()
                    } else if (chat.isSelected) {
                        chat.isSelected = false
                        listIndexOfSelectedMessages.remove(position)
                        notifyItemChanged(position)
                        chatClickListener?.onMessageDeselect()
                    }
                }
            }
            layoutInterlocutorMessage.setOnLongClickListener {
                if (!chat.isSelected) {
                    chat.isSelected = true
                    currentSelectedMessage = chat
                    notifyItemChanged(position)
                    listIndexOfSelectedMessages.add(position)
                    chatClickListener?.onMessageSelected()
                }else if (chat.isSelected) {
                    chat.isSelected = false
                    listIndexOfSelectedMessages.remove(position)
                    notifyItemChanged(position)
                    chatClickListener?.onMessageDeselect()
                }
                true
            }


            /**
             * Logged User On Click Listener
             */
            ivloggedUserExtraImage.setOnClickListener {
                chatClickListener?.onImageClick(chat)
            }

            layoutLoggedUserMessage.setOnClickListener {
                // is chat selected is more than one
                if (listIndexOfSelectedMessages.size >= 1){
                    if (!chat.isSelected) {
                        chat.isSelected = true
                        currentSelectedMessage = chat
                        listIndexOfSelectedMessages.add(position)
                        notifyItemChanged(position)
                        chatClickListener?.onMessageSelected()
                    } else if (chat.isSelected) {
                        chat.isSelected = false
                        listIndexOfSelectedMessages.remove(position)
                        notifyItemChanged(position)
                        chatClickListener?.onMessageDeselect()
                    }
                }
            }
            layoutLoggedUserMessage.setOnLongClickListener {
                if (!chat.isSelected) {
                    chat.isSelected = true
                    currentSelectedMessage = chat
                    listIndexOfSelectedMessages.add(position)
                    notifyItemChanged(position)
                    chatClickListener?.onMessageSelected()
                } else if (chat.isSelected) {
                    chat.isSelected = false
                    listIndexOfSelectedMessages.remove(position)
                    notifyItemChanged(position)
                    chatClickListener?.onMessageDeselect()
                }
                true
            }
        }
    }

    fun getTotalUnreadMessageLoggedUser(): Int {
        return conversation?.unreadMessageEachParticipant?.get(loggedUserId) ?: 0
    }

    fun setLoggedUserId(userId: String) {
        loggedUserId = userId
    }


    fun setConversation(conversation: Conversation) { // TODO
        this.conversation = conversation
    }

    fun getConversation() = conversation

    fun setMessages(newList: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }


    fun onDeselectAllMessage() {
        listIndexOfSelectedMessages.forEach { index ->
            val item: ChatMessage? = messages[index]
            if (item != null) {
                item.isSelected = false
                notifyItemChanged(index)
            }
        }
        listIndexOfSelectedMessages.clear()
    }

    fun getReceiverDeviceToken(): ArrayList<String> {
        val tokenList = ArrayList<String>()
        if (interlocutorData != null && interlocutorData?.oneSignalToken != null) {
            tokenList.add(interlocutorData!!.oneSignalToken!!)
        }

        return tokenList
    }

    fun getTotalSelectedMessages() = listIndexOfSelectedMessages.size

    fun getCurrentSelectedMessage() = currentSelectedMessage

    fun isChatEmpty() = messages.isEmpty()

    interface ChatClickListener {

        fun onImageClick(chat: ChatMessage)

        fun onMessageSelected()
        fun onMessageDeselect()
    }
}