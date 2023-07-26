package com.mikirinkode.pikul.data.model

data class Product(
    var productId: String? = null,
    var ownerId: String? = null,
    var productName: String? = null,
    var productCategory: String? = null,
    var productPrice: Float? = null,
    var productThumbnailUrl: String? = null,
)
