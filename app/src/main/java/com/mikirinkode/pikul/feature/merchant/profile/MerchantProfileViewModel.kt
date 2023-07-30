package com.mikirinkode.pikul.feature.merchant.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.utils.Event
import com.mikirinkode.pikul.utils.FireStoreUtils
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
    fun getBusinessData(businessId: String): MutableLiveData<PikulResult<Business>> {
        val result = MutableLiveData<PikulResult<Business>>()
        result.postValue(PikulResult.Loading)
        fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).document(businessId).get()
            .addOnFailureListener {
                val errorMessage = it.message ?: "Gagal Mengambil Data"
                result.postValue(PikulResult.Error(errorMessage))
            }
            .addOnSuccessListener { document ->
                val business = document.toObject(Business::class.java)
                if (business != null) {
                    result.postValue(PikulResult.Success(business))
                }
            }
        return result
    }

    fun getTransactionList(): MutableLiveData<PikulResult<List<PikulTransaction>>> {
        val result = MutableLiveData<PikulResult<List<PikulTransaction>>>()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS)
                .whereEqualTo("merchantId", userId)
                .whereEqualTo("transactionStatus", "COMPLETED")
                .get()
                .addOnFailureListener {
                    val errorMessage = it.message ?: "Gagal mengambil data transaksi"
                    result.postValue(PikulResult.Error(errorMessage))
                }
                .addOnSuccessListener {documents ->
                    val list = ArrayList<PikulTransaction>()
                    for (doc in documents) {
                        val pikulTransaction = doc.toObject(PikulTransaction::class.java)
                        list.add(pikulTransaction)
                    }
                    result.postValue(PikulResult.Success(list))
                }
        }

        return result
    }
    companion object {
        private const val TAG = "MerchantProfileVM"
    }
}