package com.mikirinkode.pikul.feature.auth.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mikirinkode.pikul.constants.PikulRole
import com.mikirinkode.pikul.data.local.LocalSharedPref
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.Event
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
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

    fun registerUser(name: String, email: String, password: String) {
        _isLoading.postValue(true)

        // try to create new user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e(TAG, "on task success")
                    val firebaseUser: FirebaseUser? = task.result.user

                    if (firebaseUser != null) {
                        Log.e(TAG, "on firebase user not null")
                        // try to add new user document to fireStore
                        // create user entity for fireStore
                        val loggedUser = auth.currentUser

                        if (loggedUser != null) {
                            Log.e(TAG, "on logged user not null")
                            val avatarUrl =
                                if (loggedUser.photoUrl == null) "" else loggedUser.photoUrl.toString()
                            val timestamp = DateHelper.getCurrentDateTime()

                            val documentRef = fireStore.collection(FireStoreUtils.TABLE_USER)
                                .document(loggedUser.uid)

                            // create user entity for local preference
                            val user = UserAccount(
                                userId = loggedUser.uid,
                                email = email,
                                name = name,
                                avatarUrl = avatarUrl,
                                createdAt = timestamp,
                                lastLoginAt = timestamp,
                                updatedAt = timestamp,
                                role = PikulRole.CUSTOMER.toString()
                            )

                            documentRef
                                .set(user)
                                .addOnSuccessListener {
                                    Log.e(TAG, "on success post to Firestore")
                                    _isLoading.postValue(false)
                                    _isError.postValue(false)
                                    _isLoginSuccess.postValue(true)

                                    // save user info to preferences
                                    preferences.startSession(user)

                                }
                                .addOnFailureListener {
                                    Log.e(TAG, "on failed post to FireStore")
                                    Log.e(TAG, "on failed post to FireStore: ${it.message}")
                                    // failed to add new user document to fireStore
                                    _isLoading.postValue(false)
                                    _isError.postValue(true)
                                    _responseMessage.postValue(Event(it.message.toString()))
                                }
                        }
                    }
                } else {
                    // failed to create new user
                    _isLoading.postValue(false)
                    _isError.postValue(true)
                    _responseMessage.postValue(Event(task.exception?.message.toString()))
                    Log.e(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    companion object {
        private const val TAG = "RegisterViewModel"
    }
}