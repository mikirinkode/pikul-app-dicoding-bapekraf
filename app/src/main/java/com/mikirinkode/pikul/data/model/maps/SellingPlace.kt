package com.mikirinkode.pikul.data.model.maps

data class SellingPlace (
    var merchantId: String? = null,
    var placeId: String? = null,
    var placeName: String? = null,
    var placeDescription: String? = null, // TODO
    var coordinate: String? = null,
    var province: String? = null,
    var visibility: Boolean? = null,

    var startTime: String? = null,
    var endTime: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,

    )