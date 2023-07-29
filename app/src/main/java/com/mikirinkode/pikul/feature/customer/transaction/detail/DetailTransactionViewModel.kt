package com.mikirinkode.pikul.feature.customer.transaction.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailTransactionViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun updateTransactionAlreadyPickedUp(transactionId: String): LiveData<PikulResult<Boolean>> {
        val result = MutableLiveData<PikulResult<Boolean>>()
        result.postValue(PikulResult.Loading)

        val data = mapOf<String, Any>(
            "transactionStatus" to TRANSACTION_STATUS.COMPLETED.toString(),
            "updatedAt" to DateHelper.getCurrentDateTime(),
            "updatedAtTimestamp" to System.currentTimeMillis()
        )
        fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document(transactionId)
            .set(data, SetOptions.merge())
            .addOnFailureListener {
                val errorMessage: String = it.message ?: "Gagal memperbarui status"
                result.postValue(PikulResult.Error(errorMessage))
            }
            .addOnSuccessListener {
                result.postValue(PikulResult.Success(true))
            }
        return result
    }

    fun getTransactionById(transactionId: String): MutableLiveData<PikulResult<PikulTransaction>> {
        val result = MutableLiveData<PikulResult<PikulTransaction>>()

        result.postValue(PikulResult.Loading)
        fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document(transactionId)
            .addSnapshotListener { value, error ->
                if (value != null){
                    val transaction = value.toObject(PikulTransaction::class.java)
                    if (transaction != null) {
                        result.postValue(PikulResult.Success(transaction))
                    } else {
                        // TODO
                    }
                }
            }
//            .get()
//            .addOnFailureListener {  } // TODO
//            .addOnSuccessListener {
//                val transaction = it.toObject(PikulTransaction::class.java)
//                if (transaction != null){
//                    result.postValue(PikulResult.Success(transaction))
//                } else {
//                    // TODO
//                }
//            }
        return result
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}