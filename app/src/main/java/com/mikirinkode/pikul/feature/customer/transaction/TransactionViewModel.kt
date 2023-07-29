package com.mikirinkode.pikul.feature.customer.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.constants.PAYMENT_STATUS
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.utils.DateHelper
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


    // TODO: SEND NOTIFICATION
    fun onPaymentSuccessReceived(transactionId: String) {
        val data = mapOf<String, Any>(
            "alreadyPaid" to true, // TODO: ADD TO Firestrore Utils
            "paidAt" to DateHelper.getCurrentDateTime(),
            "paymentStatus" to PAYMENT_STATUS.ALREADY_PAID.toString(),
            "transactionStatus" to TRANSACTION_STATUS.WAITING_FOR_MERCHANT.toString(),
            "updatedAt" to DateHelper.getCurrentDateTime(),
            "updatedAtTimestamp" to System.currentTimeMillis()
        )
        fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document(transactionId)
            .set(data, SetOptions.merge())
    }

    fun getTransactionById(transactionId: String): MutableLiveData<PikulResult<PikulTransaction>> {
        val result = MutableLiveData<PikulResult<PikulTransaction>>()

        result.postValue(PikulResult.Loading)
        fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document(transactionId)
            .addSnapshotListener { value, error ->
                if (value != null) {
                    val transaction = value.toObject(PikulTransaction::class.java)
                    if (transaction != null) {
                        result.postValue(PikulResult.Success(transaction))
                    } else {
                        // TODO
                    }
                }
            }
        return result
    }

    fun getCompleteTransactionList(): MutableLiveData<PikulResult<List<PikulTransaction>>> {
        val result = MutableLiveData<PikulResult<List<PikulTransaction>>>()

        val userId = auth.currentUser?.uid
        val filteredTransaction = listOf<String>(
            TRANSACTION_STATUS.COMPLETED.toString(),
            TRANSACTION_STATUS.CANCELLED.toString(),
        )

        if (userId != null) {
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS)
                .whereEqualTo("customerId", userId)
                .whereIn("transactionStatus", filteredTransaction)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        val errorMessage = error.message ?: "Gagal mengambil data transaksi"
                        result.postValue(PikulResult.Error(errorMessage))
                    }

                    if (value == null) {

                    } else {
                        val list = ArrayList<PikulTransaction>()
                        for (doc in value) {
                            val pikulTransaction = doc.toObject(PikulTransaction::class.java)
                            list.add(pikulTransaction)
                        }
                        result.postValue(PikulResult.Success(list))
                    }
                }
        }

        return result
    }


    fun getOnGoingTransactionList(): MutableLiveData<PikulResult<List<PikulTransaction>>> {
        val result = MutableLiveData<PikulResult<List<PikulTransaction>>>()

        val userId = auth.currentUser?.uid
        val filteredTransaction = listOf<String>(
            TRANSACTION_STATUS.WAITING_FOR_PAYMENT.toString(),
            TRANSACTION_STATUS.WAITING_FOR_MERCHANT.toString(),
            TRANSACTION_STATUS.ON_PROCESS_BY_MERCHANT.toString(),
            TRANSACTION_STATUS.READY_TO_PICK_UP.toString(),
        )

        if (userId != null) {
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS)
                .whereEqualTo("customerId", userId)
                .whereIn("transactionStatus", filteredTransaction)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        val errorMessage = error.message ?: "Gagal mengambil data transaksi"
                        result.postValue(PikulResult.Error(errorMessage))
                    }

                    if (value == null) {

                    } else {
                        val list = ArrayList<PikulTransaction>()
                        for (doc in value) {
                            val pikulTransaction = doc.toObject(PikulTransaction::class.java)
                            list.add(pikulTransaction)
                        }
                        result.postValue(PikulResult.Success(list))
                    }
                }
        }

        return result
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}