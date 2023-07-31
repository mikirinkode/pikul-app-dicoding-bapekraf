package com.mikirinkode.pikul.feature.customer.jobs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.constants.Constants
import com.mikirinkode.pikul.constants.MessageType
import com.mikirinkode.pikul.constants.NOTIFICATION_TYPE
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.data.model.chat.ChatMessage
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.Event
import com.mikirinkode.pikul.utils.FireStoreUtils
import com.onesignal.OneSignal
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class JobVacancyViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val database: FirebaseDatabase,
    private val preferences: LocalPreference
) : ViewModel() {

    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")

    fun getBusinessList(): MutableLiveData<PikulResult<List<Business>>> {
        val result = MutableLiveData<PikulResult<List<Business>>>()

        result.postValue(PikulResult.Loading)

        fireStore.collection(FireStoreUtils.TABLE_BUSINESSES)
            .whereEqualTo("openJobVacancy", true)
            .get()
            .addOnFailureListener {
                val errorMessage = it.message ?: "Gagal mengambil Data"
                result.postValue(PikulResult.Error(errorMessage))
            }
            .addOnSuccessListener { snapshots ->
                val list = ArrayList<Business>()
                for (doc in snapshots) {
                    val business = doc.toObject(Business::class.java)
                    list.add(business)
                }
                result.postValue(PikulResult.Success(list))
            }
        return result
    }

    fun sendBusinessApplication(
        businessOwnerId: String,
        merchantId: String,
        conversationId: String
    ) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val usersRef = database?.getReference("users")
            usersRef?.child(businessOwnerId)?.child("conversationIdList")?.child(conversationId)
                ?.setValue(mapOf(conversationId to true))
            usersRef?.child(merchantId)?.child("conversationIdList")?.child(conversationId)
                ?.setValue(mapOf(conversationId to true))
            // create conversation object on realtime database
            val timeStamp = System.currentTimeMillis()
            val participants = mapOf(
                merchantId to mapOf(
                    "joinedAt" to timeStamp
                ),
                businessOwnerId to mapOf(
                    "joinedAt" to timeStamp
                ),
            )
            val initialConversation = mapOf(
                "conversationId" to conversationId,
                "participants" to participants,
                "conversationType" to "PERSONAL",
                "createdAt" to timeStamp
            )
            conversationsRef?.child(conversationId)?.updateChildren(initialConversation)


            fireStore.collection(FireStoreUtils.TABLE_USER)
                .document(userId)
                .get()
                .addOnFailureListener { } // TODO
                .addOnSuccessListener {
                    val user = it.toObject(UserAccount::class.java)
                    if (user == null) {
                        // TODO
                    } else {
                        val ref = fireStore.collection(FireStoreUtils.TABLE_BUSINESS_APPLICATION)
                            .document()
                        val merchantName = user?.name ?: ""
                        val merchantAddress = user?.province ?: ""
                        val merchantPhotoUrl = user.avatarUrl ?: ""


                        val applicationData = mapOf<String, String>(
                            "applicationId" to ref.id,
                            "businessOwnerId" to businessOwnerId,
                            "merchantId" to merchantId,
                            "merchantName" to merchantName,
                            "merchantPhotoUrl" to merchantPhotoUrl,
                            "merchantAddress" to merchantAddress,
                            "createdAt" to DateHelper.getCurrentDateTime()
                        )

                        ref.set(applicationData) // TODO: add listener

                        // create the message
                        val timeStamp = System.currentTimeMillis()
                        val newMessageKey =
                            conversationsRef?.child(conversationId)?.child("messages")?.push()?.key

                        if (newMessageKey != null) {
                            val message = "Lamaran Pekerjaan"
                            val chatMessage = ChatMessage(
                                messageId = newMessageKey,
                                message = message, // TODO
                                sendTimestamp = timeStamp,
                                type = MessageType.BUSINESS_APPLICATION.toString(),
                                senderId = merchantId,
                                senderName = merchantName, // TODO
                                businessApplicationData = applicationData
                            )

                            val updateLastMessage = mapOf(
                                "lastMessage" to chatMessage
                            )

                            // update total unread messages
                            updateTotalUnreadMessages(conversationId, businessOwnerId)

                            // push last message
                            conversationsRef?.child(conversationId)
                                ?.updateChildren(updateLastMessage)

                            // push message
                            messagesRef?.child(conversationId)?.child(newMessageKey)
                                ?.setValue(chatMessage)

                            // post notification
                            fireStore.collection(FireStoreUtils.TABLE_USER)
                                .document(businessOwnerId)
                                .get()
                                .addOnSuccessListener {
                                    val user = it.toObject(UserAccount::class.java)
                                    if (user != null) {
                                        if (user.oneSignalToken != null || user.oneSignalToken != "") {
                                            val receiverDeviceTokenList =
                                                listOf<String>(user.oneSignalToken?:"")

                                            postNotification(
                                                conversationId,
                                                businessOwnerId,
                                                merchantName,
                                                message,
                                                receiverDeviceTokenList
                                            )
                                        }
                                    }
                                }

                            // reset total unread messages
                            resetTotalUnreadMessage(merchantId, conversationId)
                        }
                    }
                }
        }
    }

    private fun postNotification(
        conversationId: String,
        interlocutorId: String,
        senderName: String,
        message: String,
        receiverDeviceTokenList: List<String>
    ) {
        Log.e("ChatRoomVM", "postNotification called")
        Log.e("ChatRoomVM", "receiver device token list: " + receiverDeviceTokenList)
//        Log.e("ChatRoomVM", "receiver device token: ${receiverDeviceTokenList?.get(0)}")
        val receivers = JSONArray(receiverDeviceTokenList)
        val customData = JSONObject().apply {
            put("conversationId", conversationId)
            put("interlocutorId", interlocutorId)
            put("notificationType", NOTIFICATION_TYPE.CHATTING.toString())
        }
        val notificationJson = JSONObject().apply {
            put("app_id", Constants.ONE_SIGNAL_APP_ID)
            put("include_player_ids", receivers)
            put("contents", JSONObject().put("en", message))
            put("headings", JSONObject().put("en", senderName))
            put("data", customData)
        }

        OneSignal.postNotification(
            notificationJson,
            object : OneSignal.PostNotificationResponseHandler {
                override fun onSuccess(response: JSONObject?) {
                    // Notification sent successfully
                }

                override fun onFailure(response: JSONObject?) {
                    // Failed to send notification
                }
            })
    }

    // TODO: MULTIPLE PLACE
    // Reset the total unread message
    private fun resetTotalUnreadMessage(loggedUserId: String, conversationId: String) {
        loggedUserId?.let {
            conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")?.child(
                it
            )?.setValue(0)
        }
    }

    private fun updateTotalUnreadMessages(conversationId: String, interlocutorId: String) {
        conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")
            ?.child(interlocutorId)?.setValue(ServerValue.increment(1))
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}