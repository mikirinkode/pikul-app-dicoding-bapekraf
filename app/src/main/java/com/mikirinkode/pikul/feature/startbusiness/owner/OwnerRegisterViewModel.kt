package com.mikirinkode.pikul.feature.startbusiness.owner

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mikirinkode.pikul.constants.PikulRole
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class OwnerRegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {


    fun registerAsOwner(
        businessName: String,
        businessEmail: String,
        businessPhoneNumber: String,
        businessProvince: String,
        businessAddress: String,
        businessPhoto: File?,
    ): LiveData<PikulResult<Boolean>> {
        val result = MediatorLiveData<PikulResult<Boolean>>()
        result.value = PikulResult.Loading

        val user = auth.currentUser
        val userId = user?.uid

        if (userId != null && userId != null) {
            val role = PikulRole.OWNER.toString()
            val timestamp = DateHelper.getCurrentDateTime()
            val newUserData = mapOf<String, Any>(
                FireStoreUtils.TABLE_USER_UPDATED_AT to timestamp,
                FireStoreUtils.TABLE_USER_ROLE to role
            )

            // update local saved user data
            updateUserData(
                role,
                timestamp,
            ) // todo: province not updated

            if (businessPhoto == null) {
                val businessData = Business(
                    ownerId = userId,
                    businessId = userId,
                    businessName = businessName,
                    businessEmail = businessEmail,
                    businessPhoneNumber = businessPhoneNumber,
                    businessProvince = businessProvince,
                    businessAddress = businessAddress,
                    businessPhoto = "",
                    rating = 0.0
                )


                // add new owner business data
                fireStore.collection(FireStoreUtils.TABLE_BUSINESS).document(userId)
                    .set(businessData)
                    .addOnSuccessListener {
                        // upload new user data
                        fireStore.collection(FireStoreUtils.TABLE_USER).document(userId)
                            .set(newUserData, SetOptions.merge())
                            .addOnSuccessListener {
                                result.value = PikulResult.Success(true)
                            }
                            .addOnFailureListener {
                                result.value = PikulResult.Error(it.message.toString())
                            }
                    }
                    .addOnFailureListener {
                        result.value = PikulResult.Error(it.message.toString())
                    }
            } else {
                // if business photo is not empty
                val path = "businessPhoto_${userId}"
                val sRef: StorageReference =
                    storage.reference.child("users/${userId}/business").child(path)


                val uri = Uri.fromFile(businessPhoto)
                // upload file to firebase storage
                sRef.putFile(uri)
                    .addOnProgressListener { taskSnapshot ->
                        // Update the progress bar as the upload progresses
                        val progress =
                            (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                        result.value = PikulResult.LoadingWithProgress(progress)
                    }
                    .addOnSuccessListener { uploadSnapshot ->
                        uploadSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                            val businessData = Business(
                                ownerId = userId,
                                businessId = userId,
                                businessName = businessName,
                                businessEmail = businessEmail,
                                businessPhoneNumber = businessPhoneNumber,
                                businessProvince = businessProvince,
                                businessAddress = businessAddress,
                                businessPhoto = uri.toString(),
                                rating = 0.0
                            )
                            // add new owner business data
                            fireStore.collection(FireStoreUtils.TABLE_BUSINESS).document(userId)
                                .set(businessData)
                                .addOnSuccessListener {
                                    // upload new user data
                                    fireStore.collection(FireStoreUtils.TABLE_USER).document(userId)
                                        .set(newUserData, SetOptions.merge())
                                        .addOnSuccessListener {
                                            result.value = PikulResult.Success(true)
                                        }
                                        .addOnFailureListener {
                                            result.value = PikulResult.Error(it.message.toString())
                                        }
                                }
                                .addOnFailureListener {
                                    result.value = PikulResult.Error(it.message.toString())
                                }
                        }
                    }
                    .addOnFailureListener {
                        result.value = PikulResult.Error(it.message.toString())
                    }
            }
        }
        return result
    }

    private fun updateUserData(userRole: String, updatedAt: String) {
        val oldUser = preferences.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
        val newUser = UserAccount(
            avatarUrl = oldUser?.avatarUrl,
            conversationIdList = oldUser?.conversationIdList,
            createdAt = oldUser?.createdAt,
            email = oldUser?.email,
            lastLoginAt = oldUser?.lastLoginAt,
            name = oldUser?.name,
            oneSignalToken = oldUser?.oneSignalToken,
            province = oldUser?.province,
            role = userRole,
            updatedAt = updatedAt,
            userId = oldUser?.userId,
        )
        preferences.saveObject(LocalPreferenceConstants.USER, newUser)
    }

    companion object {
        private const val TAG = "OwnerRegisterVM"
    }
}