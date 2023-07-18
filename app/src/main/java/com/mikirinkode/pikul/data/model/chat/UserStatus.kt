package com.mikirinkode.pikul.data.model.chat

data class UserStatus (
    var onlineStatus: OnlineStatus? = null,
    var typingStatus: TypingStatus? = null,
        )