package com.mikirinkode.pikul.feature.chat.room

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.ActivityChatRoomBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatRoomActivity : AppCompatActivity() {
    private val binding: ActivityChatRoomBinding by lazy {
        ActivityChatRoomBinding.inflate(layoutInflater)
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val GALLERY_REQUEST_CODE = 2
        const val EXTRA_INTENT_INTERLOCUTOR_ID = "intent_interlocutor_id"
        const val EXTRA_INTENT_CONVERSATION_ID = "intent_conversation_id"
//        const val EXTRA_INTENT_CONVERSATION_TYPE = "intent_conversation_type"

        const val BUNDLE_CONVERSATION_ID = "conversationId"

        //        const val BUNDLE_CONVERSATION_TYPE = "conversationType"
        const val BUNDLE_INTERLOCUTOR_ID = "interlocutorId"
        const val BUNDLE_NAVIGATE_FROM = "navigateFrom"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // handle intent
        handleIntent()
    }

    private fun handleIntent() {
        // data from previous activity
        val interlocutorId = intent?.getStringExtra(EXTRA_INTENT_INTERLOCUTOR_ID)
        val conversationId = intent.getStringExtra(EXTRA_INTENT_CONVERSATION_ID)
//        val conversationType = intent.getStringExtra(EXTRA_INTENT_CONVERSATION_TYPE)
        
        setupNavigation(conversationId, interlocutorId, "AnotherActivity")
    }

    private fun setupNavigation(
        conversationId: String?,
        interlocutorId: String?,
        navigateFrom: String?
    ) {
        val navController = findNavController(R.id.navHostChatRoom)

        Log.e("ChatRoomActivity", "setup navigation")
        Log.e("ChatRoomActivity", "conversation id: ${conversationId}")
//        Log.e("ChatRoomActivity", "conversation type: ${conversationType}")
        Log.e("ChatRoomActivity", "interlocutor id: ${interlocutorId}")

                if (conversationId != null && interlocutorId != null) {
                    val bundle = Bundle()
                    bundle.putString(BUNDLE_CONVERSATION_ID, conversationId)
                    bundle.putString(BUNDLE_INTERLOCUTOR_ID, interlocutorId)
                    bundle.putString(BUNDLE_NAVIGATE_FROM, navigateFrom)
                    navController.setGraph(R.navigation.personal_chat_room_navigation, bundle)
                }

    }
}