package com.mikirinkode.pikul.feature.chat.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.data.model.chat.Conversation
import com.mikirinkode.pikul.data.model.chat.UserRTDB
import com.mikirinkode.pikul.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val database: FirebaseDatabase,
    private val preferences: LocalPreference
) : ViewModel() {

    private val conversationsRef = database?.getReference("conversations")
    private val usersRef = database?.getReference("users")

    private suspend fun getUserById(userId: String): UserAccount? {
        val querySnapshot = fireStore?.collection("users")
            ?.document(userId)
            ?.get()?.await()

        val userDoc = querySnapshot
        return userDoc?.toObject(UserAccount::class.java)
    }

    fun getUserList(): LiveData<List<UserAccount>> {
        val currentUser = auth?.currentUser
        val userList = MutableLiveData<List<UserAccount>>()

        fireStore?.collection("users")
            ?.whereNotEqualTo("userId", currentUser?.uid)
            ?.get()
            ?.addOnSuccessListener { documentList ->
                val newList = ArrayList<UserAccount>()

                for (document in documentList) {
                    if (document != null) {
                        val userAccount: UserAccount = document.toObject()
                        newList.add(userAccount)
                    }
                }
                userList.postValue(newList)
//                mListener.onGetAllUserDataSuccess(userList)
            }
            ?.addOnFailureListener {
                // TODO: on fail
            }
        return userList
    }

    fun receiveMessageHistory(): MutableLiveData<List<Conversation>> {
        Log.e("ChatHistoryVM", "ReceiveMessageHistory")
        val list = MutableLiveData<List<Conversation>>()
        val currentUser =
            preferences?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
        Log.e("ChatHistoryVM", "currentUser uid: ${currentUser?.userId}")

        val conversations = mutableListOf<Conversation>()
        currentUser?.userId?.let { userId ->
            usersRef?.child(userId)?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    val user = userSnapshot.getValue(UserRTDB::class.java)
                    Log.e("ConversationListHelper", "user: ${user}")
                    Log.e("ConversationListHelper", "user online : ${user?.onlineStatus?.online}")
                    Log.e("ConversationListHelper", "user last online timestamp: ${user?.onlineStatus?.lastOnlineTimestamp}")

                    // TODO: SHOULD SAVE ON LOCAL?
//                    val idList = arrayListOf<String>()
//                    user?.conversationIdList?.forEach { (id, _) -> idList.add(id) }
//                    pref?.saveObjectsList(
//                        PreferenceConstant.CONVERSATION_ID_LIST,
//                        idList
//                    )

                    user?.conversationIdList?.forEach { (conversationId, _) ->
                        val refWithQuery = conversationsRef?.orderByChild("conversationId")
                            ?.equalTo(conversationId)
                        Log.e("ChatHistoryVM", "current conversationId: ${conversationId}")

                        refWithQuery?.keepSynced(true)

                        refWithQuery?.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(conversationSnapshot: DataSnapshot) {

                                for (snapshot in conversationSnapshot.children) {
                                    val conversation = snapshot.getValue(Conversation::class.java)
                                    val firstUserId = conversation?.participants?.keys?.first().toString()
                                    val secondUserId =
                                        conversation?.participants?.keys?.last().toString()
                                    val interlocutorId =
                                        if (firstUserId == userId) secondUserId else firstUserId
                                    Log.e("ChatHistoryVM", "keys: ${conversation?.participants?.keys}")
                                    Log.e("ChatHistoryVM", "interlocutor id: $interlocutorId")

                                    if (interlocutorId != "null") {
                                        // Get user data by interlocutor ID
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val interlocutorUser = getUserById(interlocutorId)
                                            // Check if the interlocutorUser is not null
                                            if (interlocutorUser != null) {
                                                // Add the user data to the conversation object
                                                conversation?.interlocutor = interlocutorUser
                                                // Add the conversation object to the conversations list
                                                if (conversation != null) {
                                                    val oldConversation =
                                                        conversations.find { it.conversationId == conversation.conversationId }
                                                    conversations.remove(oldConversation)
                                                    conversations.add(conversation)

//                                                    mListener.onDataChangeReceived(conversations)
                                                    list.postValue(conversations)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
//                                TODO("Not yet implemented")
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
                }
            })
        }

//        currentUser?.userId?.let { userId ->
//            if (!userId.isNullOrBlank()){
//                val userRef = fireStore?.collection("users")?.document(userId)
//                userRef
//                    ?.addSnapshotListener { document, error ->
//
//                        val user: UserAccount? = document?.toObject()
//                        val idList = arrayListOf<String>()
//                        Log.e("ChatHistoryVM", "on success get data")
//
//                        user?.conversationIdList?.forEach { id -> idList.add(id) }
//                        Log.e("ChatHistoryVM", "idList: ${idList}")
//                        Log.e("ChatHistoryVM", "idList: ${idList.size}")
//
//                        if (idList.isEmpty()) {
////                        mListener.onEmptyConversation() // todo
//                        }
//
//                        preferences?.saveObjectsList(
//                            LocalPreferenceConstants.CONVERSATION_ID_LIST,
//                            idList
//                        )
//
//                        user?.conversationIdList?.forEach { conversationId ->
//                            val refWithQuery = conversationsRef?.orderByChild("conversationId")
//                                ?.equalTo(conversationId)
//                            Log.e("ChatHistoryVM", "current conversationId: ${conversationId}")
//
//                            refWithQuery?.keepSynced(true)
//
//                            refWithQuery?.addValueEventListener(object : ValueEventListener {
//                                override fun onDataChange(conversationSnapshot: DataSnapshot) {
//
//                                    for (snapshot in conversationSnapshot.children) {
//                                        val conversation = snapshot.getValue(Conversation::class.java)
//                                        Log.e("ChatHistoryVM", "on conversation data changed")
//                                        val firstUserId =
//                                            conversation?.participants?.keys?.first().toString()
//                                        val secondUserId =
//                                            conversation?.participants?.keys?.last().toString()
//                                        val interlocutorId =
//                                            if (firstUserId == userId) secondUserId else firstUserId
//                                        Log.e("ChatHistoryVM", "keys: ${conversation?.participants?.keys}")
//                                        Log.e("ChatHistoryVM", "interlocutor id: $interlocutorId")
//
//                                        if (interlocutorId != "null") {
//                                            // Get user data by interlocutor ID
//                                            CoroutineScope(Dispatchers.Main).launch {
//                                                val interlocutorUser = getUserById(interlocutorId)
//                                                // Check if the interlocutorUser is not null
//                                                if (interlocutorUser != null) {
//                                                    // Add the user data to the conversation object
//                                                    conversation?.interlocutor = interlocutorUser
//                                                    // Add the conversation object to the conversations list
//                                                    if (conversation != null) {
//                                                        val oldConversation =
//                                                            conversations.find { it.conversationId == conversation.conversationId }
//                                                        conversations.remove(oldConversation)
//                                                        conversations.add(conversation)
//
////                                                    mListener.onDataChangeReceived(conversations)
//                                                        list.postValue(conversations)
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//
//                                override fun onCancelled(error: DatabaseError) {
//                                }
//                            })
//                        }
//
////                    list.postValue(conversations)
//                    }
//            }
//        }
//        list.postValue(conversations)
        return list
    }


//    fun receiveMessageHistory(): LiveData<PikulResult<List<Conversation>>> {
//        val result = MutableLiveData<PikulResult<List<Conversation>>>()
//        Log.e("ChatHistoryVM", "ReceiveMessageHistory")
//        val list = MutableLiveData<List<Conversation>>()
//        val currentUser =
//            preferences?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
//        Log.e("ChatHistoryVM", "currentUser uid: ${currentUser?.userId}")
//
//        val conversations = mutableListOf<Conversation>()
//        val userId = currentUser?.userId
//        if (userId != null){
//            result.postValue(PikulResult.Loading)
//            usersRef?.child(userId!!)?.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(userSnapshot: DataSnapshot) {
//                    val user = userSnapshot.getValue(UserRTDB::class.java)
//                    Log.e("ConversationListHelper", "user: ${user}")
//                    Log.e("ConversationListHelper", "user online : ${user?.onlineStatus?.online}")
//                    Log.e("ConversationListHelper", "user last online timestamp: ${user?.onlineStatus?.lastOnlineTimestamp}")
//
//                    Log.e("ConversationListHelper", "is empty?: ${(user?.conversationIdList?.isEmpty())}")
//                    // TODO: SHOULD SAVE ON LOCAL?
////                    val idList = arrayListOf<String>()
////                    user?.conversationIdList?.forEach { (id, _) -> idList.add(id) }
////                    pref?.saveObjectsList(
////                        PreferenceConstant.CONVERSATION_ID_LIST,
////                        idList
////                    )
//                    if (user == null){
//                        result.postValue(PikulResult.Success(emptyList()))
//                    }
//                    if (user?.conversationIdList?.isEmpty() == true){
//                        result.postValue(PikulResult.Success(emptyList()))
//                    }
//
//                    user?.conversationIdList?.forEach { (conversationId, _) ->
//                        val refWithQuery = conversationsRef?.orderByChild("conversationId")
//                            ?.equalTo(conversationId)
//                        Log.e("ChatHistoryVM", "current conversationId: ${conversationId}")
//
//                        refWithQuery?.keepSynced(true)
//
//                        refWithQuery?.addValueEventListener(object : ValueEventListener {
//                            override fun onDataChange(conversationSnapshot: DataSnapshot) {
//                                if (conversationSnapshot.children.count() <= 0){
//                                    result.postValue(PikulResult.Success(emptyList()))
//                                }
//
//                                for (snapshot in conversationSnapshot.children) {
//
//                                    val conversation = snapshot.getValue(Conversation::class.java)
//                                    val firstUserId = conversation?.participants?.keys?.first().toString()
//                                    val secondUserId =
//                                        conversation?.participants?.keys?.last().toString()
//                                    val interlocutorId =
//                                        if (firstUserId == userId) secondUserId else firstUserId
//                                    Log.e("ChatHistoryVM", "keys: ${conversation?.participants?.keys}")
//                                    Log.e("ChatHistoryVM", "interlocutor id: $interlocutorId")
//
//                                    if (interlocutorId != "null") {
//                                        // Get user data by interlocutor ID
//                                        CoroutineScope(Dispatchers.Main).launch {
//                                            val interlocutorUser = getUserById(interlocutorId)
//                                            // Check if the interlocutorUser is not null
//                                            if (interlocutorUser != null) {
//                                                // Add the user data to the conversation object
//                                                conversation?.interlocutor = interlocutorUser
//                                                // Add the conversation object to the conversations list
//                                                if (conversation != null) {
//                                                    val oldConversation =
//                                                        conversations.find { it.conversationId == conversation.conversationId }
//                                                    conversations.remove(oldConversation)
//                                                    conversations.add(conversation)
//
////                                                    mListener.onDataChangeReceived(conversations)
//                                                    result.postValue(PikulResult.Success(conversations))
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
////                                TODO("Not yet implemented")
//                            }
//                        })
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
////                    TODO("Not yet implemented")
//                }
//            })
//        }
//        return result
//    }
    companion object {
        private const val TAG = "ChatHistoryViewModel"
    }
}