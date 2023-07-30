package com.mikirinkode.pikul.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class UserAccount(
    var avatarUrl: String? = null,
//    val conversationIdList: List<String>? = null,
    var createdAt: String? = null,
    var email: String? = null,
    var lastLoginAt: String? = null,
    var name: String? = null,
    val oneSignalToken: String? = null,
    val province: String? = null,
    val coordinates: String? = null,
    val role: String? = null,
//    val roles: List<String>? = null,
    var updatedAt: String? = null,
    var userId: String? = null,
    var haveBusinessAgreement: Boolean? = null,
)