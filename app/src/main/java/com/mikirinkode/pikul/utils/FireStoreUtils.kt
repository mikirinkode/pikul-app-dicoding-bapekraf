package com.mikirinkode.pikul.utils

object FireStoreUtils {

    // FireStore Table / Collection Name
    const val TABLE_USER = "users"
    const val TABLE_BUSINESSES = "businesses"
    const val TABLE_SELLING_PLACES = "sellingPlaces"
    const val TABLE_PRODUCTS = "products"
    const val TABLE_TRANSACTIONS = "transactions"

    // User Column Name
    const val TABLE_USER_ID = "userId"
    const val TABLE_USER_EMAIL = "email"
    const val TABLE_USER_NAME = "name"
    const val TABLE_USER_AVATAR_URL = "avatarUrl"
    const val TABLE_USER_CREATED_AT = "createdAt"
    const val TABLE_USER_LAST_LOGIN = "lastLoginAt"
    const val TABLE_USER_UPDATED_AT = "updatedAt"
    const val TABLE_USER_DEVICE_TOKEN = "oneSignalToken"

    const val TABLE_USER_PROVINCE = "province"
    const val TABLE_USER_ROLE = "role"
}