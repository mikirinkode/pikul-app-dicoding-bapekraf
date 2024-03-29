package com.mikirinkode.pikul.feature.owner.merchant

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.constants.Constants
import com.mikirinkode.pikul.constants.MessageType
import com.mikirinkode.pikul.constants.NOTIFICATION_TYPE
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.MerchantAgreement
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.data.model.chat.ChatMessage
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.FireStoreUtils
import com.onesignal.OneSignal
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ManageMerchantViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val database: FirebaseDatabase,
    private val preferences: LocalPreference
) : ViewModel() {

    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")


    fun updateJobVacancy(status: Boolean): MutableLiveData<PikulResult<Boolean>> {
        val result = MutableLiveData<PikulResult<Boolean>>()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val update = mapOf<String, Any>(
                "openJobVacancy" to status
            )
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).document(userId)
                .set(update, SetOptions.merge())
                .addOnSuccessListener {
                    result.postValue(PikulResult.Success(true))
                }
        }
        return result
    }

    fun sendBusinessInvitation(
        businessOwnerId: String,
        merchantId: String,
        conversationId: String
    ) {
        // create the invitation
        fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).document(businessOwnerId).get()
            .addOnSuccessListener {
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

                //create invitation data
                val businessData = it.toObject(Business::class.java)
                val ref = fireStore.collection(FireStoreUtils.TABLE_BUSINESS_INVITATIONS).document()
                val businessName = businessData?.businessName ?: ""
                val businessAddress = businessData?.businessAddress ?: ""
                val businessPhotoUrl = businessData?.businessPhoto ?: ""

                val invitationData = mapOf<String, String>(
                    "invitationId" to ref.id,
                    "businessOwnerId" to businessOwnerId,
                    "merchantId" to merchantId,
                    "businessName" to businessName,
                    "businessPhotoUrl" to businessPhotoUrl,
                    "businessAddress" to businessAddress,
                    "createdAt" to DateHelper.getCurrentDateTime()
                )

                ref.set(invitationData) // TODO: add listener

                // create the message
                val newMessageKey =
                    conversationsRef?.child(conversationId)?.child("messages")?.push()?.key

                if (newMessageKey != null) {
                    val chatMessage = ChatMessage(
                        messageId = newMessageKey,
                        message = "Undangan Bisnis", // TODO
                        sendTimestamp = timeStamp,
                        type = MessageType.BUSINESS_INVITATION.toString(),
                        senderId = businessOwnerId,
                        senderName = businessName, // TODO
                        businessInvitationData = invitationData
                    )

                    val updateLastMessage = mapOf(
                        "lastMessage" to chatMessage
                    )

                    // update total unread messages
                    updateTotalUnreadMessages(conversationId, merchantId)

                    // push last message
                    conversationsRef?.child(conversationId)?.updateChildren(updateLastMessage)

                    // push message
                    messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)

                    // post notification
                    // post notification
                    fireStore.collection(FireStoreUtils.TABLE_USER)
                        .document(merchantId)
                        .get()
                        .addOnSuccessListener {
                            val user = it.toObject(UserAccount::class.java)
                            if (user != null) {
                                if (user.oneSignalToken != null || user.oneSignalToken != "") {
                                    val receiverDeviceTokenList =
                                        listOf<String>(user.oneSignalToken?:"")

                                    postNotification(
                                        conversationId,
                                        merchantId,
                                        businessName,
                                        "Undangan Bisnis",
                                        receiverDeviceTokenList
                                    )
                                }
                            }
                        }

                    // reset total unread messages
                    resetTotalUnreadMessage(businessOwnerId, conversationId)
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


    fun getMerchantPartnerList(): LiveData<PikulResult<List<UserAccount>>> {
        val result = MutableLiveData<PikulResult<List<UserAccount>>>()

        val businessId = auth.currentUser?.uid
        if (businessId != null) {
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_USER)
                .whereEqualTo("role", "MERCHANT")
                .whereEqualTo("haveBusinessAgreement", true)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    val merchantList = ArrayList<UserAccount>()
                    for (doc in userSnapshot) {
                        val user = doc.toObject(UserAccount::class.java)
                        merchantList.add(user)
                    }

                    fireStore.collection(FireStoreUtils.TABLE_MERCHANT_AGREEMENT)
                        .whereEqualTo("businessPartnerId", businessId)
                        .get()
                        .addOnFailureListener { } // TODO
                        .addOnSuccessListener { agreementSnapshot ->
                            val resultList = ArrayList<UserAccount>()

                            for (doc in agreementSnapshot) {
                                val agreement = doc.toObject(MerchantAgreement::class.java)
                                val merchant =
                                    merchantList.firstOrNull { it.userId == agreement.merchantId }
                                if (merchant != null) {
                                    resultList.add(merchant)
                                }
                            }
                            result.postValue(PikulResult.Success(resultList))
                        }
                }
        }
        return result
    }

    fun getAvailableMerchantList(): LiveData<PikulResult<List<UserAccount>>> {
        val result = MutableLiveData<PikulResult<List<UserAccount>>>()

        result.postValue(PikulResult.Loading)
        Log.e(TAG, "getMerchantList called")

        fireStore.collection(FireStoreUtils.TABLE_USER)
            .whereEqualTo("role", "MERCHANT")
            .whereEqualTo("haveBusinessAgreement", false)
            .get()
            .addOnFailureListener {
                val errorMessage = it.message ?: "Gagal Mengambil Data"
                result.postValue(PikulResult.Error(errorMessage))
                Log.e(TAG, "onFalied")
                Log.e(TAG, "errorMessage: $errorMessage")
            }
            .addOnSuccessListener { docs ->
                Log.e(TAG, "onSuccess")
                Log.e(TAG, "docs: ${docs.size()}")
                val list = ArrayList<UserAccount>()
                for (doc in docs) {
                    val user = doc.toObject(UserAccount::class.java)
                    list.add(user)
                }
                result.postValue(PikulResult.Success(list))
            }
        return result
    }

    // TODO: ADD LISTENER DETACH AND DUPLICATE WITH OWNER DASHBOARD
    fun getBusinessData(): LiveData<PikulResult<Business>> {
        Log.e(TAG, "getBusinessData")
        val result = MutableLiveData<PikulResult<Business>>()
        result.postValue(PikulResult.Loading)

        val userId = auth.currentUser?.uid
        Log.e(TAG, "userId: ${userId}")

        if (userId != null) {
            fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).document(userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.e(TAG, "Current data: ${snapshot.data}")
                        val businessData: Business? = snapshot.toObject(Business::class.java)
                        Log.e(TAG, "businessId: ${businessData?.businessId}")

                        if (businessData != null) {
                            result.postValue(PikulResult.Success(businessData))
                        }
                    } else {
                        Log.e(TAG, "Current data: null")
                    }

                }

        }

        return result
    }

    companion object {
        private const val TAG = "ManageMerchantVM"
    }
}