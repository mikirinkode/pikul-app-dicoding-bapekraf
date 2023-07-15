package com.mikirinkode.pikul.feature.auth.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.pikul.data.local.LocalSharedPref
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.Event
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val preferences: LocalSharedPref
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _isLoginSuccess = MutableLiveData<Boolean>()
    val isLoginSuccess: LiveData<Boolean> = _isLoginSuccess

    private val _responseMessage = MutableLiveData<Event<String>>()
    val responseMessage: LiveData<Event<String>> = _responseMessage


    fun loginUser(email: String, password: String) {
        _isLoading.postValue(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isLoading.postValue(false)
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser

                    if (user != null) {
                        // set last login
                        val lastLogin = hashMapOf(
                            FireStoreUtils.TABLE_USER_LAST_LOGIN to DateHelper.getCurrentDateTime()
                        )
                        // Post Last Login Data to Collection
                        fireStore.collection(FireStoreUtils.TABLE_USER)
                            .document(user.uid)
                            .set(lastLogin, SetOptions.merge())

                        // get user data and save to local pref
                        val userRef =
                            fireStore.collection(FireStoreUtils.TABLE_USER).document(user.uid)

                        userRef.get()
                            .addOnSuccessListener { document ->
                                val userAccount: UserAccount? = document.toObject()

                                if (userAccount != null) {
                                    _isLoginSuccess.postValue(true)
                                    _isError.postValue(false)
                                    preferences.startSession(userAccount)
                                } else {
                                    _isError.postValue(true)
                                    _responseMessage.postValue(Event(task.exception?.message.toString()))
                                }

                            }.addOnFailureListener {
                                _isError.postValue(true)
                                _responseMessage.postValue(Event(task.exception?.message.toString()))
                            }
                    }
                } else {
                    _isLoading.postValue(false)
                    _isError.postValue(true)
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "signInWithEmail:failure", task.exception)
                    _responseMessage.postValue(Event(task.exception?.message.toString()))
                }
            }
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}