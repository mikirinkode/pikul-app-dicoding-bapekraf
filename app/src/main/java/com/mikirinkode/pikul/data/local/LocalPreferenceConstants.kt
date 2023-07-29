package com.mikirinkode.pikul.data.local

object LocalPreferenceConstants {
    const val USER = "user"
    const val IS_LOGGED_IN: String = "is_logged_in"
    const val SELECTED_MAIN_VIEW: String = "selected_main_view"

    const val CONVERSATION_ID_LIST = "conversation_id_list"
    const val USER_LAST_LATITUDE = "user_latitude"
    const val USER_LAST_LONGITUDE = "user_longitude"

    // order
    const val TRANSACTION_ORDER = "transaction_order"
}

enum class MAIN_VIEW{
    CUSTOMER_VIEW,
    BUSINESS_VIEW
}