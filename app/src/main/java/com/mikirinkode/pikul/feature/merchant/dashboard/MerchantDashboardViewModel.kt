package com.mikirinkode.pikul.feature.merchant.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.MerchantAgreement
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MerchantDashboardViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun getMerchantData(): LiveData<PikulResult<UserAccount>> {
        val result = MutableLiveData<PikulResult<UserAccount>>()

        val merchantId = auth.currentUser?.uid

        if (merchantId != null) {
            result.postValue(PikulResult.Loading)

            fireStore.collection(FireStoreUtils.TABLE_USER).document(merchantId).get()
                .addOnFailureListener {
                    val errorMessage = it.message ?: "Gagal Mengambil Data"
                    result.postValue(PikulResult.Error(errorMessage))
                }
                .addOnSuccessListener {doc ->
                    val user = doc.toObject(UserAccount::class.java)
                    if (user != null) {
                        result.postValue(PikulResult.Success(user))
                    }
                }
        }
        return result
    }

    fun getAgreement(): MutableLiveData<PikulResult<MerchantAgreement?>> {
        val result = MutableLiveData<PikulResult<MerchantAgreement?>>()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            fireStore.collection(FireStoreUtils.TABLE_MERCHANT_AGREEMENT).whereEqualTo("merchantId", userId).whereEqualTo("active", true).get()
                .addOnFailureListener {} // TODO
                .addOnSuccessListener {documents ->
                    if (documents.isEmpty){
                        result.postValue(PikulResult.Success(null))
                    } else {
                        val agreement = documents.first().toObject(MerchantAgreement::class.java)
                        result.postValue(PikulResult.Success(agreement))
                    }
                }
        }
        return result
    }



    companion object {
        private const val TAG = "MerchantDashboardVM"
    }
}