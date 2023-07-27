package com.mikirinkode.pikul.data.model

import com.mikirinkode.pikul.constants.PikulRole


data class Business(
    var ownerId: String? = null,
    var businessId: String? = null,
    var businessName: String? = null,
    var businessDescription: String? = null,
    var businessProductCategory: String? = null,
    var businessEmail: String? = null,
    var businessPhoneNumber: String? = null,
    var businessProvince: String? = null,
    var businessAddress: String? = null,
    var businessPhoto: String? = null,
    var businessRating: Double? = null,
    var sellingMode: Boolean? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
//    var product: List<Product>
//    var reviews
)


// TODO: REMOVE UNUSED CLASS
data class Brand(
    // owner properties
    val ownerId: String,

    // Brand properties
    val brandId: String,
    val name: String,
    val photoUrl: String,
    val lowestPrice: Double,
    val highestPrice: Double,
    val rating: String,
    val items: List<Item>,

    // merchant properties
    val merchantList: List<Merchant>
)


data class Owner(
    var ownerId: String,
    var email: String,
    var name: String,
    var avatarUrl: String,
    var createdAt: String,
    var lastLoginAt: String,
    var updatedAt: String,
    val oneSignalToken: String,
    val role: String,


    )

data class Merchant(
    var merchantId: String,
    var email: String,
    var name: String,
    var avatarUrl: String,
    var createdAt: String,
    var lastLoginAt: String,
    var updatedAt: String,
    val oneSignalToken: String,
    val role: String,

    val coordinatePosition: String,
    val movingStatus: String,
    val type: String, // Motor, mobile, gerobak, stan
//    val merchantRating: String,
)


data class Item(
    val itemId: String,
    val name: String,
    val price: String,
    val description: String,

    )

// TODO: REMOVE
object DummyData {
    fun getDummyMerchant(): List<Merchant> {
        val list = listOf<Merchant>(
            Merchant(
                "1",
                "merchant@mail.com",
                "Pedagang 1",
                "",
                "",
                "",
                "",
                "",
                PikulRole.MERCHANT.toString(),
                "4241.434, 13141.24",
                "Keliling",
                "",
            )
        )
        return list
    }

    fun getDummyBrand(): List<Brand> {
        val list = listOf<Brand>(
            Brand(
                "1",
                "1",
                "Siomay Kang Pikul",
                "",
                2000.0,
                5000.0,
                "4.5",
                listOf<Item>(
                    Item(
                        "1",
                        "Siomay Kentang",
                        "2000",
                        "-"
                    ),
                    Item(
                        "2",
                        "Siomay Telur",
                        "3000",
                        "-"
                    ),
                    Item(
                        "3",
                        "Siomay Tahu",
                        "2000",
                        "-"
                    ),
                    Item(
                        "4",
                        "Siomay Kol",
                        "2000",
                        "-"
                    ),
                ),
                getDummyMerchant()
            )
        )

        return list
    }
}

