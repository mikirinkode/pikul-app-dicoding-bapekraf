package com.mikirinkode.pikul.utils

object FireStoreUtils {

    // FireStore Table / Collection Name
    const val TABLE_USER = "users"
    const val TABLE_ROLE_REQUEST = "roleRequests"
    const val TABLE_EVENTS = "events"
    const val TABLE_ATTENDANCES = "attendances"
    const val TABLE_NOTES = "notes"

    // User Column Name
    const val TABLE_USER_ID = "userId"
    const val TABLE_USER_EMAIL = "email"
    const val TABLE_USER_NAME = "name"
    const val TABLE_USER_AVATAR_URL = "avatarUrl"
    const val TABLE_USER_CREATED_AT = "createdAt"
    const val TABLE_USER_LAST_LOGIN = "lastLoginAt"
    const val TABLE_USER_UPDATED_AT = "updatedAt"
    const val TABLE_USER_DEVICE_TOKEN = "oneSignalToken"
}