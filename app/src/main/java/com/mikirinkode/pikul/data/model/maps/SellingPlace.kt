package com.mikirinkode.pikul.data.model.maps

import com.mikirinkode.pikul.data.model.Business

data class SellingPlace(
    var merchantId: String? = null,
    var businessId: String? = null,
    var placeId: String? = null,
    var placeNoteForCustomer: String? = null,
    var coordinate: String? = null,
    var placeAddress: String? = null,
    var visibility: Boolean? = null,

    var startTime: String? = null,
    var endTime: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,

    // lokal data
    var businessName: String? = null,
    var businessPhotoUrl: String? = null,
    var merchantName: String? = null,
    var merchantPhotoUrl: String? = null,
)