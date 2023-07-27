package com.mikirinkode.pikul.feature.customer.transaction

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
class TransactionViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun getTransactionList(): MutableLiveData<PikulResult<List<PikulTransaction>>> {
        val result = MutableLiveData<PikulResult<List<PikulTransaction>>>()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS)
                .whereEqualTo("customerId", userId).get()
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
        private const val TAG = "BaseViewModel"
    }
}