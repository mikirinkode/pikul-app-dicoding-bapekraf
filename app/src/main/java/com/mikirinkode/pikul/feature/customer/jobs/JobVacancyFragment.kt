package com.mikirinkode.pikul.feature.customer.jobs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.FragmentJobVacancyBinding
import com.mikirinkode.pikul.databinding.FragmentMerchantRegisterBinding
import com.mikirinkode.pikul.feature.owner.merchant.MerchantListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


// TODO: UNIMPLEMENTED
@AndroidEntryPoint
class JobVacancyFragment : Fragment(), JobVacancyAdapter.ClickListener {
    private var _binding: FragmentJobVacancyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JobVacancyViewModel by viewModels()

    private lateinit var adapter: JobVacancyAdapter

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
        _binding = FragmentJobVacancyBinding.inflate(inflater, container, false)
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
        Log.e("JobVacancyFrag", "user role: ${user?.role}")
            adapter = JobVacancyAdapter(user?.userId, user?.role, this, pref)
    }

    private fun initRecyclerView() {
        binding.apply {
            rvJobVacancies.layoutManager = LinearLayoutManager(requireContext())
            rvJobVacancies.adapter = adapter
        }
    }


    private fun observeData(){
        viewModel.getBusinessList().observe(viewLifecycleOwner){result ->
            when (result) {
                is PikulResult.Loading -> {
                    binding.layoutLoading.visibility = View.VISIBLE
                }
                is PikulResult.LoadingWithProgress -> {}
                is PikulResult.Error -> {
                    Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is PikulResult.Success -> {
                    binding.layoutLoading.visibility = View.GONE
                    adapter.setData(result.data)
                }
            }
        }
    }

    override fun onApplyClicked(
        businessOwnerId: String,
        merchantId: String,
        conversationId: String
    ) {
        viewModel.sendBusinessApplication(businessOwnerId, merchantId, conversationId)
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }
        }
    }
}