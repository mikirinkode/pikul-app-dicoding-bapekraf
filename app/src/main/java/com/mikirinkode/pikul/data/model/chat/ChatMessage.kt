package com.mikirinkode.pikul.data.model.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(
    val messageId: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val sendTimestamp: Long = 0L,
    val type: String = "",
    val senderId: String = "",
    val senderName: String = "",
//    val deliveredTimestamp: Long = 0L, // TODO: Remove
    val beenDeliveredTo: Map<String, Long> = mapOf(), // TODO: is it need to initialize when sending message
    val beenReadBy: Map<String, Long> = mapOf(),
    val businessInvitationData: Map<String, String>? = null,

    var isSelected: Boolean = false // used to show the message is selected or not
): Parcelable
