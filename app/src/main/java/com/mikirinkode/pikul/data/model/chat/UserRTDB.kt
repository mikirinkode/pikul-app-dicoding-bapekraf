package com.mikirinkode.pikul.data.model.chat

data class UserRTDB(
    //    val userId: String = "",
//    val online: Boolean = false,
//    val typing: Boolean = false,
//    val currentlyTypingFor: String = "",
//    val lastOnlineTimestamp: Long = 0L,
    val onlineStatus: OnlineStatus? = null,
    val conversationIdList: Map<String, Any> = mapOf()
)
