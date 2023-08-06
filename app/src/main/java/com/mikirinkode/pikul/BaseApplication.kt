package com.mikirinkode.pikul

import android.app.Application
import android.content.Intent
import com.mikirinkode.pikul.constants.Constants
import com.mikirinkode.pikul.constants.NOTIFICATION_TYPE
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity
import com.mikirinkode.pikul.feature.customer.transaction.detail.DetailTransactionActivity
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(Constants.ONE_SIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler { result: OSNotificationOpenedResult? ->
            val payload = result?.notification?.additionalData
            val notificationType = payload?.optString("notificationType")

            when (notificationType.toString()) {
                NOTIFICATION_TYPE.CHATTING.toString() -> {
                    val conversationId = payload?.optString("conversationId")
                    val interlocutorId = payload?.optString("interlocutorId")
                    // Handle the chat ID
                    if (conversationId != null && !conversationId.isEmpty()) {
                        // Start the chat room activity and pass the chat ID
                        val openIntent = Intent(this, ChatRoomActivity::class.java)
                        openIntent.putExtra(
                            ChatRoomActivity.EXTRA_INTENT_CONVERSATION_ID,
                            conversationId
                        )
                        openIntent.putExtra(
                            ChatRoomActivity.EXTRA_INTENT_INTERLOCUTOR_ID,
                            interlocutorId
                        )
                        openIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(openIntent)
                    }
                }

                NOTIFICATION_TYPE.BOOKING_DETAIL_MERCHANT.toString() -> {}

                NOTIFICATION_TYPE.BOOKING_DETAIL_CUSTOMER.toString() -> {
                    val transactionId = payload?.optString("transactionId")
                    if (transactionId != null && transactionId != ""){
                        val openIntent = Intent(this, DetailTransactionActivity::class.java)
                        openIntent.putExtra(
                            DetailTransactionActivity.EXTRA_INTENT_TRANSACTION_ID,
                            transactionId
                        )
                        openIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(openIntent)
                    }
                }
                else -> {}
            }
        }
    }
}