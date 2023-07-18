package com.mikirinkode.pikul.feature.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.FragmentHomeBinding
import com.mikirinkode.pikul.feature.notification.NotificationActivity
import com.mikirinkode.pikul.feature.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var pref: LocalPreference

    private val viewModel: HomeViewModel by viewModels()
    private val categoryAdapter: CategoryAdapter by lazy {
        CategoryAdapter()
    }
    private val popularItemAdapter: PopularItemAdapter by lazy {
        PopularItemAdapter()
    }
    private val nearbyMerchantAdapter: NearbyMerchantAdapter by lazy {
        NearbyMerchantAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeCategoryList()
        observePopularItemList()
        observeNearbyMerchantList()
        onClickAction()
    }

    private fun initView() {
        val user = pref.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)

        binding.apply {
            tvUserName.text = "Halo, ${user?.name}"

            // init recycler view
            rvCategory.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvCategory.adapter = categoryAdapter

            rvPopularItem.layoutManager = LinearLayoutManager(requireContext())
            rvPopularItem.adapter = popularItemAdapter

            rvMerchantNearby.layoutManager = LinearLayoutManager(requireContext())
            rvMerchantNearby.adapter = nearbyMerchantAdapter
        }
    }

    private fun observeCategoryList() {
        Log.e("HomeFragment", "Observing category list")
        viewModel.getCategoryList().observe(viewLifecycleOwner) { list ->
            Log.e("HomeFragment", "category list: ${list.size}")
            categoryAdapter.setData(list)
        }
    }

    private fun observePopularItemList() {
        Log.e("HomeFragment", "Observing popular item list")
        viewModel.getPopularItemList().observe(viewLifecycleOwner) { list ->
            Log.e("HomeFragment", "popular list: ${list.size}")
            popularItemAdapter.setData(list)
        }
    }

    private fun observeNearbyMerchantList() {
        Log.e("HomeFragment", "Observing nearby merchant list")
        viewModel.getNearbyMerchantList().observe(viewLifecycleOwner) { list ->
            Log.e("HomeFragment", "nearby list: ${list.size}")
            nearbyMerchantAdapter.setData(list)
        }
    }

    private fun onClickAction() {
        binding.apply {
            layoutUserProfile.setOnClickListener {
                startActivity(Intent(requireContext(), ProfileActivity::class.java))
            }

            btnNotification.setOnClickListener {
                startActivity(Intent(requireContext(), NotificationActivity::class.java))
            }
        }
    }
}