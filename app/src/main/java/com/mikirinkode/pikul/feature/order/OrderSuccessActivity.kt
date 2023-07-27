package com.mikirinkode.pikul.feature.order

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mikirinkode.pikul.databinding.ActivityOrderSuccessBinding
import com.mikirinkode.pikul.feature.customer.main.MainActivity

class OrderSuccessActivity : AppCompatActivity() {

    private val binding: ActivityOrderSuccessBinding by lazy {
        ActivityOrderSuccessBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }
}