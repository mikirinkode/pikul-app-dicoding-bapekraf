package com.mikirinkode.pikul.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    var productId: String? = null,
    var ownerId: String? = null,
    var productName: String? = null,
    var productCategory: String? = null,
    var productPrice: Float? = null,
    var productThumbnailUrl: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
): Parcelable
