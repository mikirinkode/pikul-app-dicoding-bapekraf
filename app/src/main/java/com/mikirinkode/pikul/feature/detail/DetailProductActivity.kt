package com.mikirinkode.pikul.feature.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mikirinkode.pikul.databinding.ActivityDetailProductBinding

class DetailProductActivity : AppCompatActivity() {

    private val binding: ActivityDetailProductBinding by lazy {
        ActivityDetailProductBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}