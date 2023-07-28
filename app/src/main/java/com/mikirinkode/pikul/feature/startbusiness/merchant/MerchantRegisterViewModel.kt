package com.mikirinkode.pikul.feature.startbusiness.merchant

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
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MerchantRegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {


    fun registerAsMerchant(
        province: String,
        name: String,
        userAvatar: File?
    ): LiveData<PikulResult<Boolean>> {
        val result = MediatorLiveData<PikulResult<Boolean>>()
        result.value = PikulResult.Loading

        val user = auth.currentUser
        val userId = user?.uid
        val role = PikulRole.MERCHANT.toString()

        if (userId != null && userId != null) {
            if (userAvatar == null) {
                val timestamp = DateHelper.getCurrentDateTime()

                val newData = mapOf<String, Any>(
                    FireStoreUtils.TABLE_USER_PROVINCE to province,
                    FireStoreUtils.TABLE_USER_NAME to name,
                    FireStoreUtils.TABLE_USER_UPDATED_AT to timestamp,
                    FireStoreUtils.TABLE_USER_ROLE to role
                )

                // update local saved user data
                updateUserData(
                    name,
                    province,
                    role,
                    timestamp,
                    "",
                )

                // upload new data
                fireStore.collection(FireStoreUtils.TABLE_USER).document(userId)
                    .set(newData, SetOptions.merge())
                    .addOnCompleteListener {
                        result.value = PikulResult.Success(true)
                    }
                    .addOnFailureListener {
                        result.value = PikulResult.Error(it.message.toString())
                    }
            } else {
                val path = "userAvatar_${userId}"
                val sRef: StorageReference = storage.reference.child("users/${userId}").child(path)

                val uri = Uri.fromFile(userAvatar)
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
                            val timestamp = DateHelper.getCurrentDateTime()
                            val newData = mapOf<String, Any>(
                                FireStoreUtils.TABLE_USER_PROVINCE to province,
                                FireStoreUtils.TABLE_USER_NAME to name,
                                FireStoreUtils.TABLE_USER_UPDATED_AT to timestamp,
                                FireStoreUtils.TABLE_USER_ROLE to role,
                                FireStoreUtils.TABLE_USER_AVATAR_URL to uri
                            )

                            // update local saved user data
                            updateUserData(
                                name,
                                province,
                                role,
                                timestamp,
                                uri.toString(),
                            )

                            // upload new data
                            fireStore.collection(FireStoreUtils.TABLE_USER).document(userId)
                                .set(newData, SetOptions.merge())
                                .addOnCompleteListener {
                                    result.value = PikulResult.Success(true)
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

    private fun updateUserData(userName: String, userProvince: String, userRole: String, updatedAt: String, userAvatarUrl: String){
        val oldUser = preferences.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
        val avatar = if (userAvatarUrl == "") oldUser?.avatarUrl else userAvatarUrl
        val newUser = UserAccount(
            avatarUrl = avatar,
            conversationIdList = oldUser?.conversationIdList,
            createdAt = oldUser?.createdAt,
            email = oldUser?.email,
            lastLoginAt = oldUser?.lastLoginAt,
            name = userName,
            oneSignalToken = oldUser?.oneSignalToken,
            province = userProvince,
            role = userRole,
            updatedAt = updatedAt,
            userId = oldUser?.userId,
        )
        preferences.saveObject(LocalPreferenceConstants.USER, newUser)
    }

    companion object {
        private const val TAG = "MerchantRegisterVM"
    }
}
