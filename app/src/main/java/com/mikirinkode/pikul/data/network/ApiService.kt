package com.mikirinkode.pikul.data.network

import com.mikirinkode.pikul.data.model.SnapTransactionRequest
import com.mikirinkode.pikul.data.network.responses.CreateTransactionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("transactions")
    suspend fun makeTransaction(
        @Header("Authorization") authorization: String,
        @Body request: SnapTransactionRequest
    ): Response<CreateTransactionResponse>

    // TODO: FINISH CHECK TRANSACTION STATUS
    fun checkTransactionStatus()
}