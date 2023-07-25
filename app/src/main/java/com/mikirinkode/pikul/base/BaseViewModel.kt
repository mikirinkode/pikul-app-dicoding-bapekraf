package com.mikirinkode.pikul.base

import androidx.lifecycle.LiveData
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
class BaseViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val preferences: LocalPreference
) : ViewModel() {

    fun getData(): MutableLiveData<PikulResult<Any>> {
        val result = MutableLiveData<PikulResult<Any>>()
        return result
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}