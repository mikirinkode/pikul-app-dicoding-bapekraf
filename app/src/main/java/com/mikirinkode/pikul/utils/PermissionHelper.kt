package com.mikirinkode.pikul.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

/**
 * Class to handle Permissions
 */
object PermissionHelper {
    const val CAMERA_REQUEST_PERMISSION_CODE = 9001
    const val READ_EXTERNAL_REQUEST_PERMISSION_CODE = 9002
    const val NOTIFICATION_REQUEST_PERMISSION_CODE = 9003
    const val LOCATION_REQUEST_PERMISSION_CODE = 9004

    /**
     * NOTIFICATION
     */
    fun requestNotificationPermission(activity: Activity){
        val requestPermissions = mutableListOf<String>()

        if (!isNotificationPermissionGranted(activity)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                requestPermissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (requestPermissions.isNotEmpty()){
            ActivityCompat.requestPermissions(activity, requestPermissions.toTypedArray(), NOTIFICATION_REQUEST_PERMISSION_CODE)
        }
    }

    fun isNotificationPermissionGranted(context: Context): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.VIBRATE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    /**
     * CAMERA
     */
    fun requestCameraPermission(activity: Activity){
        val requestPermissions = mutableListOf<String>()

        if (!isCameraPermissionGranted(activity)){
            requestPermissions.add(android.Manifest.permission.CAMERA)
        }

        if (requestPermissions.isNotEmpty()){
            ActivityCompat.requestPermissions(activity, requestPermissions.toTypedArray(), CAMERA_REQUEST_PERMISSION_CODE)
        }
    }

    fun isCameraPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * READ EXTERNAL
     */
    fun requestReadExternalPermission(activity: Activity){
        val requestPermissions = mutableListOf<String>()

        if (!isCameraPermissionGranted(activity)){
            requestPermissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (requestPermissions.isNotEmpty()){
            ActivityCompat.requestPermissions(activity, requestPermissions.toTypedArray(), READ_EXTERNAL_REQUEST_PERMISSION_CODE)
        }
    }

    fun isReadExternalPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * LOCATION
     */
    fun isLocationPermissionGranted(context: Context): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation && coarseLocation
    }
    fun requestLocationPermission(activity: Activity) {
        // check the permissions
        val requestPermissions = mutableListOf<String>()
        if (!isLocationPermissionGranted(activity)) {
            // if permissions are not granted
            requestPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (requestPermissions.isNotEmpty()) {
            // request the permission
            ActivityCompat.requestPermissions(
                activity,
                requestPermissions.toTypedArray(),
                LOCATION_REQUEST_PERMISSION_CODE
            )
        }
    }
}