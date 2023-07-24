package com.mikirinkode.pikul.feature.owner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.ActivityOwnerMainBinding

class OwnerMainActivity : AppCompatActivity() {

    private val binding: ActivityOwnerMainBinding by lazy {
        ActivityOwnerMainBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}