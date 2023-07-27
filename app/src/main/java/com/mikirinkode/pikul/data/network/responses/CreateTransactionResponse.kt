package com.mikirinkode.pikul.data.network.responses

import com.google.gson.annotations.SerializedName

data class CreateTransactionResponse(
    @field:SerializedName("error_messages")
    val errorMessages: List<String?>? = null,

    @field:SerializedName("redirect_url")
    val redirectUrl: String? = null,

    @field:SerializedName("token")
    val token: String? = null
    )
