package com.mikirinkode.pikul.feature.owner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.ActivityOwnerMainBinding
import com.mikirinkode.pikul.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OwnerMainActivity : AppCompatActivity() {

    private val binding: ActivityOwnerMainBinding by lazy {
        ActivityOwnerMainBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.e("MapsFragment", "onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (requestCode == PermissionHelper.NOTIFICATION_REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                for (result in grantResults) {
                    Toast.makeText(this, "Ijin Notifikasi Diberikan", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}