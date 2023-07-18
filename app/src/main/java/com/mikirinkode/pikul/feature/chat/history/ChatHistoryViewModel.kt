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
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.data.model.chat.Conversation
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
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _responseMessage = MutableLiveData<Event<String>>()
    val responseMessage: LiveData<Event<String>> = _responseMessage

    private val conversationsRef = database?.getReference("conversations")

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
            val userRef = fireStore?.collection("users")?.document(userId)
            userRef
                ?.addSnapshotListener { document, error ->

                    val user: UserAccount? = document?.toObject()
                    val idList = arrayListOf<String>()
                    Log.e("ChatHistoryVM", "on success get data")

                    user?.conversationIdList?.forEach { id -> idList.add(id) }
                    Log.e("ChatHistoryVM", "idList: ${idList}")
                    Log.e("ChatHistoryVM", "idList: ${idList.size}")

                    if (idList.isEmpty()) {
//                        mListener.onEmptyConversation() // todo
                    }

                    preferences?.saveObjectsList(
                        LocalPreferenceConstants.CONVERSATION_ID_LIST,
                        idList
                    )

                    user?.conversationIdList?.forEach { conversationId ->
                        val refWithQuery = conversationsRef?.orderByChild("conversationId")
                            ?.equalTo(conversationId)
                        Log.e("ChatHistoryVM", "current conversationId: ${conversationId}")

                        refWithQuery?.keepSynced(true)

                        refWithQuery?.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(conversationSnapshot: DataSnapshot) {

                                for (snapshot in conversationSnapshot.children) {
                                    val conversation = snapshot.getValue(Conversation::class.java)
                        Log.e("ChatHistoryVM", "on conversation data changed")
                                    val firstUserId =
                                        conversation?.participants?.keys?.first().toString()
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
                            }
                        })
                    }

//                    list.postValue(conversations)
                }
        }
//        list.postValue(conversations)
        return list
    }

    companion object {
        private const val TAG = "ChatHistoryViewModel"
    }
}