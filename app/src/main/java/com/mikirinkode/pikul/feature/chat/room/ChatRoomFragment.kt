package com.mikirinkode.pikul.feature.chat.room

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.constants.ConversationType
import com.mikirinkode.pikul.constants.MessageType
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.data.model.chat.ChatMessage
import com.mikirinkode.pikul.databinding.FragmentChatRoomBinding
import com.mikirinkode.pikul.feature.customer.main.MainActivity
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * TODO:
 * allow to send transaction on paid
 * allow to send job asking for
 * allow to send job invitation
 */
@AndroidEntryPoint
class ChatRoomFragment : Fragment(),
    PersonalConversationAdapter.ChatClickListener {

    @Inject
    lateinit var preferences: LocalPreference
    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private val args: ChatRoomFragmentArgs by navArgs()

    private val adapter: PersonalConversationAdapter by lazy {
        PersonalConversationAdapter()
    }

    private val viewModel: ChatRoomViewModel by viewModels()

    private val loggedUser: UserAccount? by lazy {
        preferences?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    private var capturedImage: Uri? = null
    private var currentMessageType = MessageType.TEXT
    private var isScrolledToBottom = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeUserData()
        observeNewMessages()
        onClickAction()
        onAppBatItemSelectedClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initView() {
        binding.apply {
            rvMessages.layoutManager = LinearLayoutManager(requireContext())
            rvMessages.adapter = adapter
            adapter.chatClickListener = this@ChatRoomFragment

            if (loggedUser?.userId != null) {
                adapter.setLoggedUserId(loggedUser?.userId!!)
            }

            rvMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    binding.apply {
                        val layoutManager = rvMessages.layoutManager as LinearLayoutManager
                        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                        val itemCount = layoutManager.itemCount
                        // if the last item is not visible
                        if (lastVisibleItemPosition < itemCount - 1) { // TODO
                            btnScrollToBottom.visibility = View.VISIBLE
                            // show there is new message when user scrolling up
                            val totalUnread = adapter.getTotalUnreadMessageLoggedUser()
                            if (totalUnread > 0) {
                                tvTotalNewMessages.visibility = View.VISIBLE
                                tvTotalNewMessages.text = totalUnread.toString()
                            } else {
                                tvTotalNewMessages.visibility = View.GONE
                            }
                        } else {
                            tvTotalNewMessages.visibility = View.GONE
                            btnScrollToBottom.visibility = View.GONE
                        }
                    }
                }
            })
        }
    }


    private fun observeUserData() {

        viewModel.getUserById(args.interlocutorId).observe(viewLifecycleOwner) { user ->
            binding.apply {
//                interlocutor = user
                tvInterlocutorName.text = user.name
                if (user.avatarUrl != null && user.avatarUrl != "") {
                    Glide.with(requireContext())
                        .load(user.avatarUrl)
                        .into(ivUserAvatar)
                } else {
                    Glide.with(requireContext())
                        .load(R.drawable.ic_default_user_avatar).into(binding.ivUserAvatar)
                }
            }
        }
        // TODO
        viewModel.getUserStatus(args.interlocutorId).observe(viewLifecycleOwner) { userStatus ->
            val isTyping =
                userStatus.typingStatus?.typing == true && userStatus.typingStatus?.typingFor == loggedUser?.userId
            val isOnline = userStatus.onlineStatus?.online == true
            val lastOnlineTimestamp = userStatus.onlineStatus?.lastOnlineTimestamp ?: 0L
            binding.apply {
                if (isTyping) {
                    tvInterlocutorStatus.text = getString(R.string.txt_typing)
                    ivOnlineStatusIndicator.visibility = View.VISIBLE
                } else if (isOnline) { // TODO
                    tvInterlocutorStatus.text = "Online"
                    ivOnlineStatusIndicator.visibility = View.VISIBLE
                } else {
                    ivOnlineStatusIndicator.visibility = View.GONE
                    if (lastOnlineTimestamp != 0L) {
                        tvInterlocutorStatus.text = "Last Online at ${
                            DateHelper.getRegularFormattedDateTimeFromTimestamp(lastOnlineTimestamp)
                        }"
                    }
                }
            }
        }
    }

    private fun observeNewMessages() {
        viewModel.receiveMessage(args.conversationId).observe(viewLifecycleOwner) { messages ->
            adapter.setMessages(messages)
        }
    }

    private fun sendMessage() {
        binding.apply {
            // Get a reference to the InputMethodManager
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // Check if the keyboard is currently open
            if (imm.isAcceptingText) {
                // If the keyboard is open, hide it
                imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
            }

            val message = etMessage.text.toString().trim()
            if (message.isNotBlank()) {

                val senderId = loggedUser?.userId
                val senderName = loggedUser?.name
                val conversationId = args.conversationId
                val interlocutorId = args.interlocutorId

                val isValid: Boolean = senderId != null && senderName != null
                if (isValid) {
                    etMessage.setText("")
                    val isFirstTime: Boolean = adapter.isChatEmpty()
                    val receiverDeviceToken: List<String> =
                        adapter.getReceiverDeviceToken()

                    if (isFirstTime) {
                        if (interlocutorId != null) {
                            viewModel?.createPersonaChatRoom(
                                senderId!!,
                                interlocutorId,
                                conversationId
                            )
//                            Toast.makeText(
//                                requireContext(),
//                                "Chat Room Created",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        }
                    }

                    when (currentMessageType) {
                        MessageType.TEXT -> {
                            viewModel?.sendMessage(
                                conversationId,
                                interlocutorId,
                                message,
                                senderId!!,
                                senderName!!,
                                receiverDeviceToken
                            )
                        }
                        MessageType.IMAGE -> {
//                            if (capturedImage != null) {
//                                val path = ImageHelper.getPathForMessages(
//                                    requireActivity().contentResolver,
//                                    capturedImage!!
//                                )
//                                presenter?.sendMessage(
//                                    message,
//                                    senderId!!,
//                                    senderName!!,
//                                    capturedImage!!,
//                                    path,
//                                    receiverDeviceToken
//                                )
//                                btnAddExtras.visibility = View.VISIBLE
//                                binding.layoutSelectedImage.visibility = View.GONE
//                                capturedImage = null
//                            }
                        }
                        MessageType.VIDEO -> {}
                        MessageType.AUDIO -> {}
                    }
                }
            }
        }
    }

    /**
     * Chat Click Listener
     */
    override fun onImageClick(chat: ChatMessage) {
        binding.apply {
            val action = ChatRoomFragmentDirections.actionOpenImage(
                chat.imageUrl,
                chat.message,
                chat.sendTimestamp,
                chat.senderName
            )
            Navigation.findNavController(binding.root).navigate(action)
        }
    }


    override fun onMessageSelected() {
        Log.e("ChatRoom", "on message selected")
        updateAppBarOnSelectedView()
    }

    override fun onMessageDeselect() {
        Log.e("ChatRoom", "on message deselected")
        updateAppBarOnSelectedView()
    }


    private fun updateAppBarOnSelectedView() {
        binding.apply {
            val totalSelectedMessages = adapter.getTotalSelectedMessages()
            Log.e("ChatRoom", "totalSelectedMessages: ${totalSelectedMessages}")
            if (totalSelectedMessages > 0) {
                appBarLayoutOnItemSelected.visibility = View.VISIBLE
                tvTotalSelectedMessages.text = totalSelectedMessages.toString()

                val selectedMessageSender =
                    adapter.getCurrentSelectedMessage()?.senderId

                if (totalSelectedMessages == 1 && selectedMessageSender == loggedUser?.userId) {
                    btnShowMessageInfo.visibility = View.VISIBLE
                } else {
                    btnShowMessageInfo.visibility = View.GONE
                }
            } else {
                appBarLayoutOnItemSelected.visibility = View.GONE
            }
        }
    }

    private fun onAppBatItemSelectedClickListener() {
        binding.apply {

            btnBackOnItemSelected.setOnClickListener {
                appBarLayoutOnItemSelected.visibility = View.GONE
                adapter.onDeselectAllMessage()
            }

            btnShowMessageInfo.setOnClickListener {
                if (loggedUser != null && loggedUser?.userId != null) {
                    val totalSelectedMessages = adapter.getTotalSelectedMessages()
                    val currentSelectedMessage = adapter.getCurrentSelectedMessage()

                    val participantsId =
                        listOf(loggedUser!!.userId!!, args.interlocutorId).toTypedArray()

                    val conversationType = ConversationType.PERSONAL.toString()

                    val isValid =
                        totalSelectedMessages == 1 && loggedUser?.userId != null && currentSelectedMessage != null
                    if (isValid) {
                        val action = ChatRoomFragmentDirections.actionShowMessageInfo(
                            loggedUser?.userId!!,
                            conversationType,
                            participantsId,
                            currentSelectedMessage!!
                        )
                        Navigation.findNavController(binding.root).navigate(action)
                        adapter.onDeselectAllMessage()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "There is no selected message.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun onClickAction() {
        binding.apply {
            btnBack.setOnClickListener {
                if (args.navigateFrom != null) {
                    requireActivity().finish()
                } else {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finishAffinity()
                }
            }
            btnScrollToBottom.setOnClickListener {
                if (adapter.itemCount != null && adapter.itemCount - 1 > 0) {
                    rvMessages.smoothScrollToPosition(adapter.itemCount - 1)
                    viewModel?.resetTotalUnreadMessage(args.conversationId)
                }
            }

            layoutInterlocutorProfile.setOnClickListener {
//                startActivity(
//                    Intent(requireActivity(), OtherUserProfileActivity::class.java).putExtra(
//                        OtherUserProfileActivity.EXTRA_INTENT_USER_ID,
//                        interlocutor?.userId
//                    )
//                )
            }

            btnAddExtras.setOnClickListener {
                if (layoutSelectExtras.visibility == View.GONE) {
                    layoutSelectExtras.visibility = View.VISIBLE
                } else {
                    layoutSelectExtras.visibility = View.GONE
                }
            }

            btnSend.setOnClickListener {
                layoutSelectExtras.visibility = View.GONE
                sendMessage()
            }

            btnCamera.setOnClickListener {
                if (PermissionHelper.isCameraPermissionGranted(requireContext())) {
//                    presenter?.takePicture() // TODO
                } else {
                    PermissionHelper.requestCameraPermission(requireActivity())
                }
            }

            btnImportFromGallery.setOnClickListener {
                if (PermissionHelper.isReadExternalPermissionGranted(requireContext())) {
                    // TODO: UNIMPLEMENTED

                } else {
                    PermissionHelper.requestReadExternalPermission(requireActivity())
                }
            }

            btnRemoveCapturedImage.setOnClickListener {
                capturedImage = null
                btnAddExtras.visibility = View.VISIBLE
                layoutSelectedImage.visibility = View.GONE
            }
        }
    }
}