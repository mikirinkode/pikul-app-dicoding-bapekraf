package com.mikirinkode.pikul.feature.auth.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.databinding.ActivityLoginBinding
import com.mikirinkode.pikul.feature.auth.register.RegisterActivity
import com.mikirinkode.pikul.feature.customer.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var pref: LocalPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkLoggedUser()
        observeIsLoading()
        onClickAction()
    }

    private fun observeIsLoading() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBarLogin.visibility = View.VISIBLE
            } else {
                binding.progressBarLogin.visibility = View.GONE
            }
        }
    }

    private fun goToMainView() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun checkLoggedUser() { // TODO: update
        val isLoggedIn: Boolean? = pref.getBoolean(LocalPreferenceConstants.IS_LOGGED_IN)

        if (isLoggedIn == true) {
            goToMainView()
        }
    }

    private fun login() {

        binding.apply {
            var isValid = true

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = getString(R.string.empty_email)
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = getString(R.string.invalid_email)
                isValid = false
            }

            if (password.isEmpty()) {
                etPassword.error = getString(R.string.empty_password)
                isValid = false
            }

            if (isValid) {
                viewModel.loginUser(email, password)
                viewModel.isError.observe(this@LoginActivity) { isError ->
                    if (isError) {
                        viewModel.responseMessage.observe(this@LoginActivity) {
                            if (it != null) {
                                it.getContentIfNotHandled()?.let { msg ->
                                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        // if not error, then
                        viewModel.isLoginSuccess.observe(this@LoginActivity) { isLoginSuccess ->
                            if (isLoginSuccess) {
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finishAffinity()
                                Toast.makeText(this@LoginActivity, getString(R.string.txt_login_success), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onClickAction() {
        binding.apply {
            btnLogin.setOnClickListener {
                login()
            }

            btnGoogle.setOnClickListener {}

            btnGoToRegister.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                finish()
            }
        }
    }
}