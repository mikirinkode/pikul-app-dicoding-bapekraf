package com.mikirinkode.pikul.feature.owner.merchant

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.FragmentMerchantListBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// TODO: UNIMPLEMENTED
@AndroidEntryPoint
class MerchantListFragment : Fragment(), MerchantListAdapter.ClickListener {
    private var _binding: FragmentMerchantListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageMerchantViewModel by viewModels()

    private lateinit var adapter: MerchantListAdapter

    @Inject
    lateinit var pref: LocalPreference

    private val user: UserAccount? by lazy {
        pref?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMerchantListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initRecyclerView()
        onClickAction()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initAdapter(){
        adapter = user?.userId?.let { MerchantListAdapter(it, this) }!!
    }

    private fun initRecyclerView() {
        binding.apply {
            rvMerchants.layoutManager = LinearLayoutManager(requireContext())
            rvMerchants.adapter = adapter
        }
    }

    private fun observeData(){
        viewModel.getAvailableMerchantList().observe(viewLifecycleOwner){ result ->
            when (result) {
                is PikulResult.Loading -> {}
                is PikulResult.LoadingWithProgress -> {} // TODO
                is PikulResult.Error -> {}
                is PikulResult.Success -> {
                    adapter.setData(result.data)
                }
            }
        }
    }

    override fun onInviteClicked(
        businessOwnerId: String,
        merchantId: String,
        conversationId: String
    ) {
        viewModel.sendBusinessInvitation(businessOwnerId, merchantId, conversationId)
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }
        }
    }
}