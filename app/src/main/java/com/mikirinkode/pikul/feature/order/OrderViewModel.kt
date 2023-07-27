package com.mikirinkode.pikul.feature.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.constants.Constants
import com.mikirinkode.pikul.constants.PAYMENT_STATUS
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.*
import com.mikirinkode.pikul.data.network.ApiClient
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun getData(): MutableLiveData<PikulResult<Any>> {
        val result = MutableLiveData<PikulResult<Any>>()
        return result
    }

    private fun createPaymentUrl(paymentId: String, totalBilling: Float) {

        val data = SnapTransactionRequest(
            TransactionDetails(paymentId, totalBilling.toLong()),
            CreditCard(true)
        )
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.getApiService(Constants.MIDTRANS_MAKE_TRANSACTION_URL)
                .makeTransaction(
                    "Basic U0ItTWlkLXNlcnZlci04RjdJV2NtQ1Y5YnVHRnpaWTd6Y3B5cGo=",
                    data
                ) // TODO: CHANGE

            Log.e(TAG, "is succesful ${response.isSuccessful}")
            Log.e(TAG, "data ${response.body()?.redirectUrl}")
            Log.e(TAG, "data ${response.body()?.token}")
            Log.e(TAG, "data ${response.body()?.errorMessages}")

        }

    }

    fun createTransaction(
        transactionTotalItem: Int,
        transactionTotalBilling: Float,
        pickupAddress: String,
        pickupCoordinates: String,
        businessId: String,
        businessName: String,
        merchantId: String,
        merchantName: String,
    ): LiveData<PikulResult<String>> {
        val result = MutableLiveData<PikulResult<String>>()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val productIdWithName = mapOf<String, String>()
            val productIdWithPrice = mapOf<String, Float>()
            val productIdWithAmount = mapOf<String, Int>()

            val ref = fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document()

            result.postValue(PikulResult.Loading)
            CoroutineScope(Dispatchers.IO).launch {
                val data = SnapTransactionRequest(
                    TransactionDetails(ref.id, transactionTotalBilling.toLong()),
                    CreditCard(true)
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val response = ApiClient.getApiService(Constants.MIDTRANS_MAKE_TRANSACTION_URL)
                        .makeTransaction(
                            "Basic U0ItTWlkLXNlcnZlci04RjdJV2NtQ1Y5YnVHRnpaWTd6Y3B5cGo=",
                            data
                        ) // TODO: CHANGE

                    if (response.isSuccessful) {
                        val paymentUrl = response.body()?.redirectUrl


                        val transaction = Transaction(
                            transactionId = ref.id,
                            alreadyPaid = false,
                            paymentStatus = PAYMENT_STATUS.WAITING_FOR_PAYMENT.toString(),
                            paymentUrl = paymentUrl,
                            paidAt = null,
                            transactionStatus = TRANSACTION_STATUS.WAITING_FOR_PAYMENT.toString(),
                            totalItem = transactionTotalItem,
                            totalBilling = transactionTotalBilling,
                            pickupAddress = pickupAddress,
                            pickupCoordinates = pickupCoordinates,

                            productNames = productIdWithName,
                            productPrices = productIdWithPrice,
                            productAmounts = productIdWithAmount,

                            customerId = userId,

                            businessId = businessId,
                            businessName = businessName,

                            merchantId = merchantId,
                            merchantName = merchantName,

                            createdTimestamp = System.currentTimeMillis(),
                            createdAt = DateHelper.getCurrentDateTime(),
                            updatedAt = null,
                        )

                        ref.set(transaction)
                            .addOnFailureListener {
                                val errorMessage: String = it.message ?: "Gagal membuat transaksi"
                                result.postValue(PikulResult.Error(errorMessage))
                            }
                            .addOnSuccessListener {
                                if (paymentUrl != null) {
                                    result.postValue(PikulResult.Success(paymentUrl))
                                } else {

                                    result.postValue(PikulResult.Error("Gagal membuat transaksi"))
                                }
                            }
                    }
                }
            }
        }
        return result
    }

    companion object {
        private const val TAG = "OrderViewModel"
    }
}