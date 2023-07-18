package com.mikirinkode.pikul.feature.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.FragmentChatHistoryBinding
import com.mikirinkode.pikul.databinding.FragmentMessageInfoBinding
import com.mikirinkode.pikul.utils.DateHelper
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MessageInfoFragment : Fragment() {
    private var _binding: FragmentMessageInfoBinding? = null
    private val binding get() = _binding!!
    private val args: MessageInfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMessageInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        onActionClick()

    }
    private fun initView() {
        binding.apply {


            val conversationType = args.conversationType
            val message = args.chatMessage

            // show message read by status
            layoutPersonalMessageInfo.visibility = View.VISIBLE
            if (message.sendTimestamp != 0L){
                val deliveredTimestamp = message.sendTimestamp
                tvDeliveredTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(deliveredTimestamp)
            } else {
                tvDeliveredTimestamp.text = "-"
            }

            if (message.beenReadBy.isNotEmpty()){
                val readTimestamp = message.beenReadBy.entries.first().value
                tvReadTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(readTimestamp)
            } else {
                tvReadTimestamp.text = "-"
            }

            // show message card
            if (message != null){
                if (message.senderId == args.loggedUserId){
                    layoutItemMessage.layoutInterlocutorMessage.visibility = View.GONE
                    layoutItemMessage.layoutLoggedUserMessage.visibility = View.VISIBLE
                    layoutItemMessage.apply {
                        tvloggedUserMessage.text = message.message
                        tvloggedUserTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(message.sendTimestamp)
                    }
                } else {
                    layoutItemMessage.layoutInterlocutorMessage.visibility = View.VISIBLE
                    layoutItemMessage.layoutLoggedUserMessage.visibility = View.GONE
                    layoutItemMessage.apply {
                        tvInterlocutorMessage.text = message.message
                        tvInterlocutorTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(message.sendTimestamp)
                    }
                }

                if (message.type == MessageType.IMAGE.toString() && message.imageUrl != null && message.imageUrl != ""){
                    if (message.senderId == args.loggedUserId){
                        layoutItemMessage.ivloggedUserExtraImage.visibility = View.VISIBLE
                        Glide.with(requireContext())
                            .load(message.imageUrl)
                            .into(layoutItemMessage.ivloggedUserExtraImage)
                    } else {
                        layoutItemMessage.ivInterlocutorExtraImage.visibility = View.VISIBLE
                        Glide.with(requireContext())
                            .load(message.imageUrl)
                            .into(layoutItemMessage.ivInterlocutorExtraImage)
                    }
                }
            }
        }
    }

    private fun onActionClick(){
        binding.apply {
            binding.topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}