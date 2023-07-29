package com.mikirinkode.pikul.feature.customer.transaction.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.PikulTransaction
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

    fun getTransactionById(transactionId: String): MutableLiveData<PikulResult<PikulTransaction>> {
        val result = MutableLiveData<PikulResult<PikulTransaction>>()

        result.postValue(PikulResult.Loading)
        fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document(transactionId)
            .get()
            .addOnFailureListener {  } // TODO
            .addOnSuccessListener {
                val transaction = it.toObject(PikulTransaction::class.java)
                if (transaction != null){
                    result.postValue(PikulResult.Success(transaction))
                } else {
                    // TODO
                }
            }
        return result
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}