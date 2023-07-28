package com.mikirinkode.pikul.feature.customer.main

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.constants.PikulRole
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.MAIN_VIEW
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ActivityMainBinding
import com.mikirinkode.pikul.databinding.SideNavHeaderBinding
import com.mikirinkode.pikul.feature.auth.login.LoginActivity
import com.mikirinkode.pikul.feature.merchant.MerchantMainActivity
import com.mikirinkode.pikul.feature.owner.OwnerMainActivity
import com.mikirinkode.pikul.feature.startbusiness.StartBusinessActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var preferences: LocalPreference

    private val user: UserAccount? by lazy {
        preferences?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkLatestView()
    }

    private fun checkLatestView() {
        val latestView = preferences?.getString(LocalPreferenceConstants.SELECTED_MAIN_VIEW)
        if (latestView == MAIN_VIEW.BUSINESS_VIEW.toString()) {
            navigateToBusinessView()
        } else if (latestView == MAIN_VIEW.CUSTOMER_VIEW.toString()) {
            // NOTHING TO DO
            initNavigation()
        } else { // TODO: SOME TIMES ERROR MAKE BOTTOM NAVIGATION NOT WORKING
            initNavigation()
        }
    }

    private fun navigateToBusinessView(){
        val userRole = user?.role
        if (userRole == PikulRole.OWNER.toString()){
            startActivity(Intent(this@MainActivity, OwnerMainActivity::class.java))
            finishAffinity()
        } else if (userRole == PikulRole.MERCHANT.toString()){
            startActivity(Intent(this@MainActivity, MerchantMainActivity::class.java))
            finishAffinity()
        }
    }



    private fun initNavigation() {
        val navController = findNavController(R.id.nav_host_fragment_activity_home)

        binding.apply {
            val bottomNavView: BottomNavigationView = bottomNavView
            bottomNavView.background = null

            bottomNavView.setupWithNavController(navController)
        }
    }




}