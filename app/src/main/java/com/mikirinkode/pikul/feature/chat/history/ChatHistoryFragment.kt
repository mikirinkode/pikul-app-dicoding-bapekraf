package com.mikirinkode.pikul.feature.chat.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.FragmentChatHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatHistoryFragment : Fragment() {
    private var _binding: FragmentChatHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatHistoryViewModel by viewModels()

    private val adapter: ChatHistoryAdapter by lazy {
        ChatHistoryAdapter()
    }


    @Inject
    lateinit var preferences: LocalPreference

    private val loggedUser: UserAccount? by lazy {
        preferences?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        checkPermission() // TODO
        initView()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView(){
        binding.apply {
            rvChatHistory.layoutManager = LinearLayoutManager(requireContext())
            rvChatHistory.adapter = adapter
            loggedUser?.userId?.let { adapter.setLoggedUserId(it) }
            observeData()
        }
    }

    private fun observeData() {
        viewModel.receiveMessageHistory().observe(viewLifecycleOwner) { list ->
            Log.e("ChatHistoryFrag", "observeData")
            Log.e("ChatHistoryFrag", "list: ${list.size}")
            adapter.setData(list)
        }
//        viewModel.getUserList().observe(viewLifecycleOwner){ list ->
//            userAdapter.setData(list)
//        }
    }

    private fun onClickAction() {}

}