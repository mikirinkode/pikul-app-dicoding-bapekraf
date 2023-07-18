package com.mikirinkode.pikul.feature.chat.room

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.data.model.chat.ChatMessage
import com.mikirinkode.pikul.data.model.chat.UserStatus
import com.mikirinkode.pikul.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val database: FirebaseDatabase,
    private val preferences: LocalPreference
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _responseMessage = MutableLiveData<Event<String>>()
    val responseMessage: LiveData<Event<String>> = _responseMessage

    
    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")
    private val usersRef = database?.getReference("users")
    private val loggedUser = preferences?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)


//    private val receiveListener = object : ValueEventListener {
//        override fun onDataChange(dataSnapshot: DataSnapshot) {
//            val messages = mutableListOf<ChatMessage>()
//            val theLatestMessage =
//                dataSnapshot.children.lastOrNull()?.getValue(ChatMessage::class.java)
//
//            for (snapshot in dataSnapshot.children) {
//                val chatMessage = snapshot.getValue(ChatMessage::class.java)
//
//                if (chatMessage != null) {
//                    if (loggedUser?.userId != null && chatMessage.senderId != loggedUser.userId) {
//                        if (!chatMessage.beenReadBy.containsKey(loggedUser.userId!!)) {
//
//                            val timeStamp = System.currentTimeMillis()
//                            updateMessageReadTime(
//                                timeStamp,
//                                loggedUser.userId!!,
//                                snapshot?.key ?: "",
//                                conversationId
//                            )
//
//                            if (chatMessage.messageId == theLatestMessage?.messageId) {
//                                updateLastMessageReadTime(
//                                    loggedUser.userId!!, timeStamp, conversationId
//                                )
//                            }
//                        }
//                    }
//                    messages.add(chatMessage)
//                }
//            }
//            val sortedMessages = messages.sortedBy { it.sendTimestamp }
//            mListener.onMessagesReceived(sortedMessages)
//        }
//
//        override fun onCancelled(error: DatabaseError) {
//            // TODO: UNIMPLEMENTED
//        }
//    }

    fun receiveMessage(conversationId: String): LiveData<List<ChatMessage>> {
        val data = MutableLiveData<List<ChatMessage>>()

        val ref = conversationId.let { messagesRef?.child(it) }
        ref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val messages = mutableListOf<ChatMessage>()
                val theLatestMessage =
                    dataSnapshot.children.lastOrNull()?.getValue(ChatMessage::class.java)

                for (snapshot in dataSnapshot.children) {
                    val chatMessage = snapshot.getValue(ChatMessage::class.java)

                    if (chatMessage != null) {
                        if (loggedUser?.userId != null && chatMessage.senderId != loggedUser.userId) {
                            if (!chatMessage.beenReadBy.containsKey(loggedUser.userId!!)) {

                                val timeStamp = System.currentTimeMillis()
                                updateMessageReadTime(
                                    timeStamp,
                                    loggedUser.userId!!,
                                    snapshot?.key ?: "",
                                    conversationId
                                )

                                if (chatMessage.messageId == theLatestMessage?.messageId) {
                                    updateLastMessageReadTime(
                                        loggedUser.userId!!, timeStamp, conversationId
                                    )
                                }
                            }
                        }
                        messages.add(chatMessage)
                    }
                }
                val sortedMessages = messages.sortedBy { it.sendTimestamp }
//                mListener.onMessagesReceived(sortedMessages) // TODO
                data.postValue(sortedMessages)
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO: UNIMPLEMENTED
            }})
        return data
    }

//    fun deactivateListener(conversationId: String) {
//        messagesRef?.child(conversationId)?.removeEventListener(receiveListener)
//    }


    fun createPersonaChatRoom(userId: String, anotherUserId: String, conversationId: String) {
        // add conversation id to user firestore collection
        val userRef = fireStore?.collection("users")?.document(userId)
        userRef?.update("conversationIdList", FieldValue.arrayUnion(conversationId))
        val anotherUserRef = fireStore?.collection("users")?.document(anotherUserId)
        anotherUserRef?.update("conversationIdList", FieldValue.arrayUnion(conversationId))


        // create conversation object on realtime database
        val timeStamp = System.currentTimeMillis()
        val participants = mapOf(
            userId to mapOf(
                "joinedAt" to timeStamp
            ),
            anotherUserId to mapOf(
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

        val newConversationList = ArrayList<String>()
        loggedUser?.conversationIdList?.let { newConversationList.addAll(it) }
        newConversationList.add(conversationId)
        val newUserData = UserAccount(
            userId = loggedUser?.userId,
            email = loggedUser?.email,
            name = loggedUser?.name,
            avatarUrl = loggedUser?.avatarUrl,
            createdAt = loggedUser?.createdAt,
            lastLoginAt = loggedUser?.lastLoginAt,
            updatedAt = loggedUser?.updatedAt,
            conversationIdList = newConversationList
        )

        preferences?.saveObject(LocalPreferenceConstants.USER, newUserData)
    }

    fun sendMessage(
        conversationId: String,
        interlocutorId: String,
        message: String,
        senderId: String,
        senderName: String,
        receiverDeviceTokenList: List<String>
    ) {
        Log.e("ChatRoomVM", "sendMessage called")
        Log.e("ChatRoomVM", "database: ${database}")
        val timeStamp = System.currentTimeMillis()
        val newMessageKey = conversationsRef?.child(conversationId)?.child("messages")?.push()?.key
        Log.e("ChatRoomVM", "new message key: ${newMessageKey}")
        if (newMessageKey != null) {
            val chatMessage = ChatMessage(
                messageId = newMessageKey,
                message = message,
                sendTimestamp = timeStamp,
                type = MessageType.TEXT.toString(),
                senderId = senderId,
                senderName = senderName
            )

            val updateLastMessage = mapOf(
                "lastMessage" to chatMessage
            )

            // update total unread messages
            updateTotalUnreadMessages(conversationId, interlocutorId)

            // push last message
            conversationsRef?.child(conversationId)?.updateChildren(updateLastMessage)

            // push message
            messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)

            // post notification
//            postNotification(senderName, message, receiverDeviceTokenList)

            // reset total unread messages
            resetTotalUnreadMessage(conversationId)
        }

    }

    // Reset the total unread message
    fun resetTotalUnreadMessage(conversationId: String) {
        loggedUser?.userId?.let {
            conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")?.child(
                it
            )?.setValue(0)
        }
    }

    private fun updateTotalUnreadMessages(conversationId: String, interlocutorId: String) {
        conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")
            ?.child(interlocutorId)?.setValue(ServerValue.increment(1))
    }

    // TODO
//    private fun postNotification(
//        senderName: String,
//        message: String,
//        receiverDeviceTokenList: List<String>
//    ) {
//        val receivers = JSONArray(receiverDeviceTokenList)
//        val customData = JSONObject().apply {
//            put("conversationId", conversationId)
//            put("conversationType", ConversationType.PERSONAL.toString())
//        }
//        val notificationJson = JSONObject().apply {
//            put("app_id", Constants.ONE_SIGNAL_APP_ID)
//            put("include_player_ids", receivers)
//            put("contents", JSONObject().put("en", message))
//            put("headings", JSONObject().put("en", senderName))
//            put("data", customData)
//        }
//
//        OneSignal.postNotification(
//            notificationJson,
//            object : OneSignal.PostNotificationResponseHandler {
//                override fun onSuccess(response: JSONObject?) {
//                    // Notification sent successfully
//                }
//
//                override fun onFailure(response: JSONObject?) {
//                    // Failed to send notification
//                }
//            })
//    }

    private fun updateMessageReadTime(timeStamp: Long, userId: String, messageId: String, conversationId: String) {
        messagesRef?.child(conversationId)?.child(messageId)?.child("beenReadBy")?.child(userId)
            ?.setValue(timeStamp)
    }

    private fun updateLastMessageReadTime(userId: String, timeStamp: Long, conversationId: String) {
        conversationsRef?.child(conversationId)?.child("lastMessage")?.child("beenReadBy")
            ?.child(userId)?.setValue(timeStamp)
    }


    fun getUserById(userId: String): LiveData<UserAccount> {
        Log.e("ChatRoomVM", "getUserById")
        Log.e("ChatRoomVM", "user id: ${userId}")
        val data = MutableLiveData<UserAccount>()
        fireStore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user: UserAccount? = document.toObject()
                if (user != null) {
                    data.postValue(user!!)
//                    mListener.onInterlocutorDataReceived(user) // TODO
                }
            }
        return data
    }

    fun getUserStatus(userId: String): LiveData<UserStatus> {
        val data = MutableLiveData<UserStatus>()
        usersRef?.child(userId)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(UserStatus::class.java)
                if (status != null) {
                    data.postValue(status!!)
//                    mListener?.onUserStatusReceived(status) // TODO
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return data
    }
    
    companion object {
        private const val TAG = "ChatRoomViewModel"
    }
}