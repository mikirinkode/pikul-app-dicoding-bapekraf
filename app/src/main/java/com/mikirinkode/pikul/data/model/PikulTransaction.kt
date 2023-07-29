package com.mikirinkode.pikul.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PikulTransaction(
    // transaction data
    var transactionId: String? = null,
    var alreadyPaid: Boolean? = null,
    var paymentStatus: String? = null,
    var paymentUrl: String? = null,
    var paidAt: String? = null,
    var transactionStatus: String? = null,
    var totalItem: Int? = null,
    var totalBilling: Float? = null,
    var pickupAddress: String? = null,
    var pickupCoordinates: String? = null,

    // product data
    var productNames: Map<String, String>? = null,
    var productPrices: Map<String, Float>? = null,
    var productAmounts: Map<String, Int>? = null,

    // customer data:
    var customerId: String? = null,
    var customerName: String? = null,

    // business data
    var businessId: String? = null,
    var businessName: String? = null,

    // merchant data
    var merchantId: String? = null,
    var merchantName: String? = null,

    var createdTimestamp: Long? = null,
    var createdAt: String? = null,
    var updatedAtTimestamp: Long? = null,
    var updatedAt: String? = null
): Parcelable