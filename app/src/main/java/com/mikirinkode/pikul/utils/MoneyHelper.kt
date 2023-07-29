package com.mikirinkode.pikul.utils

import java.text.NumberFormat
import java.util.*

object MoneyHelper {

    fun getFormattedPrice(price: Float): String{
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")) // Use the appropriate locale for Indonesian Rupiah (IDR)
        formatter.maximumFractionDigits = 0
        return formatter.format(price)
    }
}