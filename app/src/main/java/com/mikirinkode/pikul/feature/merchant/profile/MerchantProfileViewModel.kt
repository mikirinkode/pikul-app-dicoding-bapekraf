package com.mikirinkode.pikul.feature.merchant.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MerchantProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val preferences: LocalPreference
) : ViewModel() {

    fun logout(): LiveData<PikulResult<Boolean>> {
        val result = MediatorLiveData<PikulResult<Boolean>>()

        result.postValue(PikulResult.Loading)
        preferences?.clearSession()
        auth?.signOut()
        result.postValue(PikulResult.Success(true))
        return result
    }

    companion object {
        private const val TAG = "MerchantProfileVM"
    }
}