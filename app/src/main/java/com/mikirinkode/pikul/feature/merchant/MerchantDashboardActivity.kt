package com.mikirinkode.pikul.feature.merchant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.ActivityMerchantDashboardBinding

class MerchantDashboardActivity : AppCompatActivity() {

    private val binding: ActivityMerchantDashboardBinding by lazy {
        ActivityMerchantDashboardBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onClickAction()
    }

    private fun onClickAction() {

    }
}