package com.mikirinkode.pikul.feature.customer.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.FragmentHomeBinding
import com.mikirinkode.pikul.feature.auth.login.LoginActivity
import com.mikirinkode.pikul.feature.notification.NotificationActivity
import com.mikirinkode.pikul.feature.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private val viewModel: HomeViewModel by viewModels()
    private val categoryAdapter: CategoryAdapter by lazy {
        CategoryAdapter()
    }
    private val popularBusinessAdapter: PopularBusinessAdapter by lazy {
        PopularBusinessAdapter()
    }
    private val nearbyMerchantAdapter: NearbyMerchantAdapter by lazy {
        NearbyMerchantAdapter()
    }

    @Inject
    lateinit var pref: LocalPreference

    private val user: UserAccount? by lazy {
        pref?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
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
        observePopularBusinessList()

        observeCategoryList()
        observePopularItemList()
        observeNearbyMerchantList()
        onClickAction()
    }

    private fun initView() {
        Log.e("HomeFragment", "on initView")
        Log.e("HomeFragment", "pref: ${pref}")
        Log.e("HomeFragment", "user id: ${user?.userId}, userName: ${user?.name}")

        binding.apply {
            if (user?.avatarUrl.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(R.drawable.ic_default_user_avatar)
                    .into(ivUserAvatar)
            } else {
                Glide.with(requireContext())
                    .load(user?.avatarUrl)
                    .placeholder(R.drawable.progress_animation)
                    .into(ivUserAvatar)
            }

            if (user != null) {
                tvUserName.text = "Halo, ${user?.name}"
            } else {
                tvUserName.text = getGreetings()
            }

            // init recycler view
            rvCategory.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvCategory.adapter = categoryAdapter

            rvPopularItem.layoutManager = LinearLayoutManager(requireContext())
            rvPopularItem.adapter = popularBusinessAdapter

            rvMerchantNearby.layoutManager = LinearLayoutManager(requireContext())
            rvMerchantNearby.adapter = nearbyMerchantAdapter
        }
    }

    private fun getGreetings(): String {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)

        return when {
            currentHour < 12 -> "Selamat Pagi"
            currentHour < 15 -> "Selamat Siang"
            currentHour < 18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    private fun observeCategoryList() {
        Log.e("HomeFragment", "Observing category list")
        viewModel.getCategoryList().observe(viewLifecycleOwner) { list ->
            Log.e("HomeFragment", "category list: ${list.size}")
            categoryAdapter.setData(list)
        }
    }

    private fun observePopularBusinessList() {
        viewModel.getPopularBusinessList().observe(viewLifecycleOwner) { result ->
            when (result) {
                is PikulResult.Loading -> {}
                is PikulResult.LoadingWithProgress -> {}
                is PikulResult.Error -> {}
                is PikulResult.Success -> {
                    popularBusinessAdapter.setData(result.data)
                }
            }
        }
    }

    private fun observePopularItemList() {
        Log.e("HomeFragment", "Observing popular item list")
        viewModel.getPopularItemList().observe(viewLifecycleOwner) { list ->
            Log.e("HomeFragment", "popular list: ${list.size}")
//            popularProductAdapter.setData(list)
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
                if (user == null) {
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                } else {
                    startActivity(Intent(requireContext(), ProfileActivity::class.java))
                }
            }

            btnNotification.setOnClickListener {
                startActivity(Intent(requireContext(), NotificationActivity::class.java))
            }
        }
    }
}