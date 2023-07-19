package com.mikirinkode.pikul.feature.startbusiness

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mikirinkode.pikul.databinding.ActivityStartBusinessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartBusinessActivity : AppCompatActivity() {

    private val binding: ActivityStartBusinessBinding by lazy {
        ActivityStartBusinessBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}