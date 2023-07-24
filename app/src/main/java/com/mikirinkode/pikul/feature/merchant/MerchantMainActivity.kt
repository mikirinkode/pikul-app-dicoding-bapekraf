package com.mikirinkode.pikul.feature.merchant

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ActivityMerchantMainBinding
import com.mikirinkode.pikul.feature.customer.transaction.TransactionFragment
import com.mikirinkode.pikul.feature.customer.transaction.TransactionPagerAdapter
import com.mikirinkode.pikul.utils.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MerchantMainActivity : AppCompatActivity(), TimePickerFragment.DialogTimeListener {

    private val binding: ActivityMerchantMainBinding by lazy {
        ActivityMerchantMainBinding.inflate(layoutInflater)
    }

    companion object {
        var timePickerListener: TimePickerListener? = null
        interface TimePickerListener{
            fun onDialogTimeSet(tag: String?, hourOfDay: Int, minute: Int)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onDialogTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        timePickerListener?.onDialogTimeSet(tag, hourOfDay, minute)
    }
}