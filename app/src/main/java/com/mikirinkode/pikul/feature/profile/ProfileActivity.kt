package com.mikirinkode.pikul.feature.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.constants.PikulRole
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.MAIN_VIEW
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ActivityProfileBinding
import com.mikirinkode.pikul.feature.auth.login.LoginActivity
import com.mikirinkode.pikul.feature.customer.main.MainActivity
import com.mikirinkode.pikul.feature.merchant.MerchantMainActivity
import com.mikirinkode.pikul.feature.owner.OwnerMainActivity
import com.mikirinkode.pikul.feature.startbusiness.StartBusinessActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var preferences: LocalPreference

    private val user: UserAccount? by lazy {
        preferences?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    private val viewModel: ProfileViewModel by viewModels()

    companion object {
        private const val BTN_START_SELLING = "Mulai Berjualan"
        private const val BTN_OPEN_BUSINESS_VIEW = "Buka Tampilan Penjual"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        onClickAction()
    }

    private fun navigateToBusinessView() {
        val userRole = user?.role
        if (userRole == PikulRole.OWNER.toString()) {
            startActivity(Intent(this@ProfileActivity, OwnerMainActivity::class.java))
            finishAffinity()
        } else if (userRole == PikulRole.MERCHANT.toString()) {
            startActivity(Intent(this@ProfileActivity, MerchantMainActivity::class.java))
            finishAffinity()
        }
    }

    private fun initView() {
        binding.apply {
            if (user == null) {
                layoutUserProfile.visibility = View.GONE
            } else {
                layoutUserProfile.visibility = View.VISIBLE
                tvUserName.text = user?.name
                tvEmail.text = user?.email
                tvDummyRole.text = user?.role
                tvRole.text = user?.role

                if (user?.role == PikulRole.CUSTOMER.toString()) {
                    btnOpenBusinessView.text = BTN_START_SELLING
                } else {
                    btnOpenBusinessView.text = BTN_OPEN_BUSINESS_VIEW
                }

                if (user?.avatarUrl.isNullOrBlank()) {
                    Glide.with(this@ProfileActivity)
                        .load(R.drawable.ic_default_user_avatar)
                        .into(ivUserAvatar)
                } else {
                    Glide.with(this@ProfileActivity)
                        .load(user?.avatarUrl)
                        .into(ivUserAvatar)
                }
            }
        }
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressed()
            }

            btnLogout.setOnClickListener {
                viewModel.logout().observe(this@ProfileActivity){ result ->
                    when (result) {
                        is PikulResult.Loading -> {} // TODO
                        is PikulResult.LoadingWithProgress -> {}
                        is PikulResult.Error -> {}
                        is PikulResult.Success -> {
                            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                            finishAffinity()
                        }
                    }
                }
            }

            btnOpenBusinessView.setOnClickListener {
                val textOnButton = btnOpenBusinessView.text

                if (textOnButton == BTN_OPEN_BUSINESS_VIEW) {
                    preferences.saveString(
                        LocalPreferenceConstants.SELECTED_MAIN_VIEW,
                        MAIN_VIEW.BUSINESS_VIEW.toString()
                    )
                    navigateToBusinessView()
                } else {
                    startActivity(Intent(this@ProfileActivity, StartBusinessActivity::class.java))
                }

            }
        }
    }
}