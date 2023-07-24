package com.mikirinkode.pikul.feature.merchant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mikirinkode.pikul.databinding.ActivityMerchantMainBinding

class MerchantMainActivity : AppCompatActivity() {

    private val binding: ActivityMerchantMainBinding by lazy {
        ActivityMerchantMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onClickAction()
    }

    private fun onClickAction() {

    }
}