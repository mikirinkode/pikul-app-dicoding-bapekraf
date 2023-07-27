package com.mikirinkode.pikul.data.model

import com.google.gson.annotations.SerializedName

data class SnapTransactionRequest(
    @SerializedName("transaction_details") val transactionDetails: TransactionDetails,
    @SerializedName("credit_card") val creditCard: CreditCard
)

data class TransactionDetails(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("gross_amount") val grossAmount: Long
)

data class CreditCard(
    @SerializedName("secure") val secure: Boolean
)