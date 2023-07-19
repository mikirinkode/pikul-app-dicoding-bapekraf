package com.mikirinkode.pikul.feature.customer.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ActivityMainBinding
import com.mikirinkode.pikul.databinding.SideNavHeaderBinding
import com.mikirinkode.pikul.feature.auth.login.LoginActivity
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

        initView()
        initNavigation()
        onClickAction()
    }

    private fun initView() {
        val sideNavBinding = SideNavHeaderBinding.bind(binding.sideNavView.getHeaderView(0))

//        val user = pref.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)

        sideNavBinding.apply {
            if (user == null){
                layoutUserProfile.visibility = View.GONE
                btnLogin.visibility = View.VISIBLE
            } else {
                layoutUserProfile.visibility = View.VISIBLE
                btnLogin.visibility = View.GONE
                tvUserName.text = user?.name

                if (user?.avatarUrl.isNullOrBlank()){
                    Glide.with(this@MainActivity)
                        .load(R.drawable.ic_default_user_avatar)
                        .into(ivUserAvatar)
                } else {
                    Glide.with(this@MainActivity)
                        .load(user?.avatarUrl)
                        .into(ivUserAvatar)
                }
            }
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

    override fun onBackPressed() {
        binding.apply {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun onClickAction(){
        val sideNavBinding = SideNavHeaderBinding.bind(binding.sideNavView.getHeaderView(0))
        sideNavBinding.apply {
            btnLogin.setOnClickListener {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
            btnStartSelling.setOnClickListener {
                startActivity(Intent(this@MainActivity, StartBusinessActivity::class.java))
            }
        }
    }
}