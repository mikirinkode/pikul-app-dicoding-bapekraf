package com.mikirinkode.pikul.feature.auth.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.ActivityRegisterBinding
import com.mikirinkode.pikul.feature.auth.login.LoginActivity
import com.mikirinkode.pikul.feature.customer.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

// TODO: ADD BACK BUTTON
// TODO: GOOGLE ACCOUNT
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        observeIsLoading()
        onClickAction()
    }

    private fun register() {
        binding.apply {
            val name: String = etName.text.toString().trim()
            val email: String = etEmail.text.toString().trim()
            val password: String = etPassword.text.toString().trim()
            val passwordConf: String = etPasswordConfirmation.text.toString().trim()
            val isValid = checkInputValidation(name, email, password, passwordConf)

            if (isValid) {
                viewModel.registerUser(name, email, password)
                viewModel.isError.observe(this@RegisterActivity) { isError ->
                    if (isError) {
                        viewModel.responseMessage.observe(this@RegisterActivity) {
                            if (it != null) {
                                it.getContentIfNotHandled()?.let { msg ->
                                    Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    } else {
                        // if not error, then
                        viewModel.isLoginSuccess.observe(this@RegisterActivity) { isLoginSuccess ->
                            if (isLoginSuccess) {
                                startActivity(
                                    Intent(
                                        this@RegisterActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finishAffinity()
                                Toast.makeText(this@RegisterActivity, getString(R.string.txt_login_success), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeIsLoading() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading){
                binding.progressBarLogin.visibility = View.VISIBLE
            } else {
                binding.progressBarLogin.visibility = View.GONE
            }
        }
    }

    private fun checkInputValidation(
        name: String,
        email: String,
        password: String,
        passwordConf: String
    ): Boolean {
        binding.apply {
            var isValid = true
            if (TextUtils.isEmpty(name)) {
                etName.error = getString(R.string.empty_name)
                isValid = false
            } else if (name.length < 3) {
                etName.error = getString(R.string.name_too_short)
                isValid = false
            }

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
            } else if (!isValidPassword(password)) {
                etPasswordConfirmation.error = getString(R.string.invalid_password)
                isValid = false
            }

            if (passwordConf != password) {
                etPasswordConfirmation.error = getString(R.string.pass_not_match)
                isValid = false
            }

            return isValid
        }
    }

    private fun isValidPassword(password: CharSequence?): Boolean {
        // password pattern using Regular Expression
        val regex = ("^(?=.*[0-9])"             // password must contain number
                + "(?=.*[a-z])(?=.*[A-Z])"      // password must contain uppercase and lowercase
                + "(?=\\S+$).{8,}$")            // password must not have whitespace and have length more than 8
        val pattern = Pattern.compile(regex)

        return if (password == null) false else pattern.matcher(password).matches()
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressed()
            }
            btnRegister.setOnClickListener {
                register()
            }

            btnGoToLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}