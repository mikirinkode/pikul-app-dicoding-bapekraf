package com.mikirinkode.pikul.feature.payment

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
class MidtransViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun onPaymentFailed(transactionId: String) {
        val data = mapOf<String, Any>(
            "alreadyPaid" to false, // TODO: ADD TO Firestrore Utils
            "paymentStatus" to PAYMENT_STATUS.FAILED.toString(),
            "transactionStatus" to TRANSACTION_STATUS.CANCELLED.toString(),
            "updatedAt" to DateHelper.getCurrentDateTime(),
            "updatedAtTimestamp" to System.currentTimeMillis()
        )
        fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document(transactionId)
            .set(data, SetOptions.merge())
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}