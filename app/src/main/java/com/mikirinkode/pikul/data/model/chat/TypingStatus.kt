package com.mikirinkode.pikul.data.model.chat

data class TypingStatus(
    val typing: Boolean = false,
    val typingFor: String? = null,
)
