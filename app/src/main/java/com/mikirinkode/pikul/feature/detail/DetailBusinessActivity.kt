package com.mikirinkode.pikul.feature.detail

import android.content.Intent
import android.content.Intent.EXTRA_INTENT
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ActivityDetailBusinessBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity
import com.mikirinkode.pikul.feature.profile.OtherUserProfileActivity
import com.mikirinkode.pikul.utils.MoneyHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailBusinessActivity : AppCompatActivity()  {

    private val binding: ActivityDetailBusinessBinding by lazy {
        ActivityDetailBusinessBinding.inflate(layoutInflater)
    }


    companion object {
        const val EXTRA_INTENT_BUSINESS_ID = "EXTRA_INTENT_BUSINESS_ID"
        const val EXTRA_INTENT_MERCHANT_ID = "EXTRA_INTENT_MERCHANT_ID"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        handleIntent()
    }


    private fun handleIntent() {
        val extras = intent.extras
        val businessId = extras?.getString(EXTRA_INTENT_BUSINESS_ID)
        val merchantId = extras?.getString(EXTRA_INTENT_MERCHANT_ID)
        Log.e("DetailActivity", "busienessId: $businessId")
        if (businessId != null) {
            setupNavigation(businessId, merchantId)
        }
    }

    private fun setupNavigation(businessId: String, merchantId: String?) {
        val navController = findNavController(R.id.navHostDetailBusiness)
        val bundle = Bundle()
        bundle.putString("businessId", businessId)
        bundle.putString("merchantId", merchantId)
        navController.setGraph(R.navigation.detail_business_navigation, bundle)
    }


    private fun onClickAction(){
        binding.apply {

        }
    }
}