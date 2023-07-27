package com.mikirinkode.pikul.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductWithAmount(
    var product: Product,
    var amount: Int
): Parcelable
