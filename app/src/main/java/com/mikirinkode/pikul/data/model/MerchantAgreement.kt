package com.mikirinkode.pikul.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MerchantAgreement(
    var agreementId: String? = null,
    var merchantId: String? = null,
    var businessPartnerId: String? = null,
    var active: Boolean? = null,
    var partnerSince: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
): Parcelable
