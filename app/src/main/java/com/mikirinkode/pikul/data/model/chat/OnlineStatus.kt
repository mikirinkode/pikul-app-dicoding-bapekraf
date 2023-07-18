package com.mikirinkode.pikul.data.model.chat

data class OnlineStatus(
    val online: Boolean = false,
    val lastOnlineTimestamp: Long = 0L,
)